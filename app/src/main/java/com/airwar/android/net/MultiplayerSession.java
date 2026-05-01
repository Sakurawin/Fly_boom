package com.airwar.android.net;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import hitsz.aircraftwar.backend.AircraftWar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public final class MultiplayerSession {
    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }

    public interface Listener {
        void onSessionUpdated(Snapshot snapshot);
    }

    // 会话快照是页面层唯一需要依赖的联机状态视图，避免 Activity 直接操作底层 socket。
    public record Snapshot(
            String baseUrl,
            String roomId,
            String username,
            ConnectionState connectionState,
            String lastError,
            AircraftWar.Room room,
            List<AircraftWar.RoomPlayerScore> scores,
            boolean roomFinished,
            @Nullable AircraftWar.RoomResult result
    ) {
    }

    private static final MultiplayerSession INSTANCE = new MultiplayerSession();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Set<Listener> listeners = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final OkHttpClient webSocketClient = new OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build();
    private final Object stateLock = new Object();

    private String baseUrl = NetworkConfig.DEFAULT_BASE_URL;
    private String roomId = "";
    private String username = "";
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private String lastError = "";
    private AircraftWar.Room room = AircraftWar.Room.getDefaultInstance();
    private List<AircraftWar.RoomPlayerScore> scores = List.of();
    private boolean roomFinished;
    private AircraftWar.RoomResult result;
    private WebSocket webSocket;
    private ScheduledFuture<?> heartbeatFuture;
    private long heartbeatSequence;

    private MultiplayerSession() {
    }

    public static MultiplayerSession getInstance() {
        return INSTANCE;
    }

    // 进入房间前先写入联机会话的基础身份信息，后续 HTTP 和 WS 都基于这一份配置工作。
    public void configure(String baseUrl, String roomId, String username) {
        synchronized (stateLock) {
            this.baseUrl = NetworkConfig.normalizeBaseUrl(baseUrl);
            this.roomId = roomId == null ? "" : roomId;
            this.username = username == null ? "" : username;
            this.lastError = "";
        }
        notifyListeners();
    }

    public void applyRoomState(AircraftWar.Room room, List<AircraftWar.RoomPlayerScore> scores, boolean roomFinished, @Nullable AircraftWar.RoomResult result) {
        synchronized (stateLock) {
            if (room != null) {
                this.room = room;
            }
            this.scores = List.copyOf(scores == null ? List.of() : scores);
            this.roomFinished = roomFinished;
            this.result = result;
        }
        notifyListeners();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
        mainHandler.post(() -> listener.onSessionUpdated(snapshot()));
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public Snapshot snapshot() {
        synchronized (stateLock) {
            return new Snapshot(baseUrl, roomId, username, connectionState, lastError, room, List.copyOf(scores), roomFinished, result);
        }
    }

    public void connectIfNeeded() {
        synchronized (stateLock) {
            if (roomId.isEmpty() || username.isEmpty()) {
                return;
            }
            if (connectionState == ConnectionState.CONNECTED || connectionState == ConnectionState.CONNECTING) {
                return;
            }
            connectionState = ConnectionState.CONNECTING;
            lastError = "";
        }
        notifyListeners();

        // WebSocket 只负责对战中的实时事件；连接成功后马上进入心跳保活。
        Request request = new Request.Builder()
                .url(NetworkConfig.wsUrl(snapshot().baseUrl(), snapshot().roomId(), snapshot().username()))
                .build();
        webSocket = webSocketClient.newWebSocket(request, new SessionWebSocketListener());
    }

    public void disconnect() {
        stopHeartbeat();
        synchronized (stateLock) {
            connectionState = ConnectionState.DISCONNECTED;
            lastError = "";
        }
        if (webSocket != null) {
            webSocket.close(1000, "client close");
            webSocket = null;
        }
        notifyListeners();
    }

    // 主动离开房间时清掉本地缓存，避免返回菜单后还残留上一局的比分和结果。
    public void clearRoomContext() {
        stopHeartbeat();
        synchronized (stateLock) {
            roomId = "";
            connectionState = ConnectionState.DISCONNECTED;
            lastError = "";
            room = AircraftWar.Room.getDefaultInstance();
            scores = List.of();
            roomFinished = false;
            result = null;
            heartbeatSequence = 0L;
        }
        if (webSocket != null) {
            webSocket.close(1000, "leave room");
            webSocket = null;
        }
        notifyListeners();
    }

    public void sendDefeatEvent(AircraftWar.EnemyType enemyType, int scoreDelta) {
        // 击败事件必须包进 WsMessage，不能直接发送裸事件体。
        AircraftWar.PlayerDefeatEvent event = AircraftWar.PlayerDefeatEvent.newBuilder()
                .setRoomId(snapshot().roomId())
                .setUsername(snapshot().username())
                .setEnemyType(enemyType)
                .setScoreDelta(scoreDelta)
                .setClientEventId(UUID.randomUUID().toString())
                .build();
        sendWsMessage(AircraftWar.WsMessage.newBuilder().setPlayerDefeatEvent(event).build());
    }

    public void sendGameOverEvent(int finalScore, String reason) {
        AircraftWar.PlayerGameOverEvent event = AircraftWar.PlayerGameOverEvent.newBuilder()
                .setRoomId(snapshot().roomId())
                .setUsername(snapshot().username())
                .setFinalScore(finalScore)
                .setReason(reason == null ? "" : reason)
                .build();
        sendWsMessage(AircraftWar.WsMessage.newBuilder().setPlayerGameOverEvent(event).build());
    }

    private void startHeartbeat() {
        stopHeartbeat();
        heartbeatFuture = scheduler.scheduleAtFixedRate(() -> {
            Snapshot current = snapshot();
            if (current.connectionState() != ConnectionState.CONNECTED || current.roomId().isEmpty() || current.username().isEmpty()) {
                return;
            }
            long nextSequence;
            synchronized (stateLock) {
                heartbeatSequence += 1L;
                nextSequence = heartbeatSequence;
            }
            // 心跳序号用于排查丢包、乱序和网络抖动。
            AircraftWar.PlayerHeartbeatEvent event = AircraftWar.PlayerHeartbeatEvent.newBuilder()
                    .setRoomId(current.roomId())
                    .setUsername(current.username())
                    .setClientSentAt(System.currentTimeMillis())
                    .setSequence(nextSequence)
                    .build();
            sendWsMessage(AircraftWar.WsMessage.newBuilder().setPlayerHeartbeatEvent(event).build());
        }, 0L, 3L, TimeUnit.SECONDS);
    }

    private void stopHeartbeat() {
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(true);
            heartbeatFuture = null;
        }
    }

    private void sendWsMessage(AircraftWar.WsMessage message) {
        WebSocket socket = webSocket;
        if (socket == null) {
            return;
        }
        socket.send(okio.ByteString.of(message.toByteArray()));
    }

    private void notifyListeners() {
        Snapshot snapshot = snapshot();
        for (Listener listener : new ArrayList<>(listeners)) {
            mainHandler.post(() -> listener.onSessionUpdated(snapshot));
        }
    }

    private final class SessionWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            synchronized (stateLock) {
                connectionState = ConnectionState.CONNECTED;
                lastError = "";
            }
            startHeartbeat();
            notifyListeners();
        }

        @Override
        public void onMessage(WebSocket webSocket, okio.ByteString bytes) {
            try {
                AircraftWar.WsMessage message = AircraftWar.WsMessage.parseFrom(bytes.toByteArray());
                handleMessage(message);
            } catch (InvalidProtocolBufferException e) {
                synchronized (stateLock) {
                    lastError = "WS 消息解析失败: " + e.getMessage();
                }
                notifyListeners();
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            synchronized (stateLock) {
                connectionState = ConnectionState.DISCONNECTED;
                lastError = reason == null ? "" : reason;
            }
            MultiplayerSession.this.webSocket = null;
            stopHeartbeat();
            notifyListeners();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            synchronized (stateLock) {
                connectionState = ConnectionState.ERROR;
                lastError = t == null ? "网络连接失败" : t.getMessage();
            }
            MultiplayerSession.this.webSocket = null;
            stopHeartbeat();
            notifyListeners();
        }
    }

    private void handleMessage(AircraftWar.WsMessage message) {
        synchronized (stateLock) {
            switch (message.getPayloadCase()) {
                case SCORE_BROADCAST -> {
                    // 比分广播可以在房间未整体结束时持续更新，即使当前玩家已经掉线结束。
                    AircraftWar.ScoreBroadcast broadcast = message.getScoreBroadcast();
                    scores = List.copyOf(broadcast.getScoresList());
                }
                case GAME_FINISHED_BROADCAST -> {
                    // 房间最终完成后保存结果，供结束页和排行榜跳转使用。
                    AircraftWar.GameFinishedBroadcast broadcast = message.getGameFinishedBroadcast();
                    roomFinished = broadcast.getFinished();
                    if (broadcast.hasResult()) {
                        result = broadcast.getResult();
                    }
                }
                default -> {
                }
            }
        }
        notifyListeners();
    }
}
