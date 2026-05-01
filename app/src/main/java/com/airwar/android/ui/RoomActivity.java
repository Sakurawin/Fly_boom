package com.airwar.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airwar.android.R;
import com.airwar.android.net.MultiplayerApi;
import com.airwar.android.net.MultiplayerSession;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hitsz.aircraftwar.backend.AircraftWar;

public class RoomActivity extends AppCompatActivity implements MultiplayerSession.Listener {
    public static final String EXTRA_DIFFICULTY = MenuActivity.EXTRA_DIFFICULTY;

    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final MultiplayerApi multiplayerApi = new MultiplayerApi();

    private TextView roomCodeView;
    private TextView roomOwnerView;
    private TextView roomStatusView;
    private TextView roomConnectionView;
    private Button readyButton;
    private Button syncButton;
    private Button startButton;
    private String selectedDifficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        selectedDifficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        if (selectedDifficulty == null || selectedDifficulty.isEmpty()) {
            selectedDifficulty = MenuActivity.DIFFICULTY_NORMAL;
        }

        MultiplayerSession.Snapshot snapshot = MultiplayerSession.getInstance().snapshot();
        if (snapshot.roomId().isEmpty() || snapshot.username().isEmpty()) {
            String savedBaseUrl = LocalMultiplayerPrefs.getBaseUrl(this);
            String savedUsername = LocalMultiplayerPrefs.getUsername(this);
            String savedRoomId = LocalMultiplayerPrefs.getRoomId(this);
            if (savedRoomId.isEmpty() || savedUsername.isEmpty()) {
                finishToMenu();
                return;
            }
            MultiplayerSession.getInstance().configure(savedBaseUrl, savedRoomId, savedUsername);
        }

        TextView headerTitle = findViewById(R.id.room_header);
        roomCodeView = findViewById(R.id.room_code_value);
        roomOwnerView = findViewById(R.id.room_owner_value);
        roomStatusView = findViewById(R.id.room_status_value);
        roomConnectionView = findViewById(R.id.room_connection_value);
        readyButton = findViewById(R.id.button_room_ready);
        syncButton = findViewById(R.id.button_room_sync);
        startButton = findViewById(R.id.button_room_start);

        headerTitle.setText(getString(R.string.multiplayer_room_title));
        bindBottomNav();

        readyButton.setOnClickListener(v -> readyRoom());
        syncButton.setOnClickListener(v -> syncRoomState());
        startButton.setOnClickListener(v -> startGame());

        MultiplayerSession.getInstance().addListener(this);
        renderSnapshot(MultiplayerSession.getInstance().snapshot());
        syncRoomState();
    }

    @Override
    protected void onDestroy() {
        MultiplayerSession.getInstance().removeListener(this);
        ioExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public void onSessionUpdated(MultiplayerSession.Snapshot snapshot) {
        renderSnapshot(snapshot);
    }

    private void renderSnapshot(MultiplayerSession.Snapshot snapshot) {
        AircraftWar.Room room = snapshot.room();
        roomCodeView.setText(snapshot.roomId().isEmpty() ? "-" : snapshot.roomId());
        roomOwnerView.setText(snapshot.username().isEmpty() ? "-" : snapshot.username());
        roomStatusView.setText(readableRoomStatus(room == null ? null : room.getStatus()));
        roomConnectionView.setText(readableConnectionStatus(snapshot.connectionState()));

        boolean hasRoom = !snapshot.roomId().isEmpty();
        boolean isHost = isHost(snapshot);
        readyButton.setEnabled(hasRoom);
        syncButton.setEnabled(hasRoom);
        startButton.setEnabled(hasRoom && isHost);
    }

    private boolean isHost(MultiplayerSession.Snapshot snapshot) {
        AircraftWar.Room room = snapshot.room();
        if (room == null) {
            return false;
        }
        for (AircraftWar.Player player : room.getPlayersList()) {
            if (player.getUsername().equals(snapshot.username())) {
                return player.getIsHost();
            }
        }
        return false;
    }

    private void bindBottomNav() {
        TextView homeLabel = findViewById(R.id.nav_home_label);
        TextView rankingLabel = findViewById(R.id.nav_ranking_label);

        homeLabel.setTextColor(ContextCompat.getColor(this, R.color.ui2_accent));
        rankingLabel.setTextColor(ContextCompat.getColor(this, R.color.ui2_body));

        findViewById(R.id.nav_home).setOnClickListener(v -> {
            MultiplayerSession.getInstance().clearRoomContext();
            LocalMultiplayerPrefs.clearRoomId(this);
            finishToMenu();
        });

        findViewById(R.id.nav_ranking).setOnClickListener(v -> {
            Intent leaderboardIntent = new Intent(this, LeaderboardActivity.class);
            startActivity(leaderboardIntent);
        });
    }

    private void readyRoom() {
        MultiplayerSession.Snapshot snapshot = MultiplayerSession.getInstance().snapshot();
        runRoomRequest(
                () -> multiplayerApi.readyRoom(snapshot.baseUrl(), snapshot.roomId(), snapshot.username()),
                response -> MultiplayerSession.getInstance().applyRoomState(response.getRoom(), java.util.List.of(), false, null)
        );
    }

    private void syncRoomState() {
        MultiplayerSession.Snapshot snapshot = MultiplayerSession.getInstance().snapshot();
        runRoomRequest(
                () -> multiplayerApi.getRoomState(snapshot.baseUrl(), snapshot.roomId(), snapshot.username()),
                response -> MultiplayerSession.getInstance().applyRoomState(
                        response.getRoom(),
                        response.getScoresList(),
                        response.getRoomFinished(),
                        response.hasResult() ? response.getResult() : null
                )
        );
    }

    private void startGame() {
        MultiplayerSession.Snapshot snapshot = MultiplayerSession.getInstance().snapshot();
        runRoomRequest(
                () -> multiplayerApi.startGame(snapshot.baseUrl(), snapshot.roomId(), snapshot.username()),
                response -> {
                    if (!response.getStarted()) {
                        toast(getString(R.string.multiplayer_error_start_not_ready));
                        return;
                    }
                    MultiplayerSession.getInstance().applyRoomState(response.getRoom(), java.util.List.of(), false, null);
                    Intent gameIntent = new Intent(this, GameActivity.class);
                    gameIntent.putExtra(EXTRA_DIFFICULTY, selectedDifficulty);
                    startActivity(gameIntent);
                }
        );
    }

    private <T> void runRoomRequest(RoomCall<T> call, RoomResultHandler<T> onSuccess) {
        setRoomActionEnabled(false);
        ioExecutor.execute(() -> {
            try {
                T result = call.call();
                UiExecutor.run(this, () -> {
                    onSuccess.handle(result);
                    setRoomActionEnabled(true);
                    toast(getString(R.string.multiplayer_action_success));
                });
            } catch (IOException e) {
                UiExecutor.run(this, () -> {
                    setRoomActionEnabled(true);
                    toast(e.getMessage() == null ? "网络请求失败" : e.getMessage());
                });
            }
        });
    }

    private void setRoomActionEnabled(boolean enabled) {
        readyButton.setEnabled(enabled);
        syncButton.setEnabled(enabled);
        startButton.setEnabled(enabled && isHost(MultiplayerSession.getInstance().snapshot()));
    }

    private String readableRoomStatus(AircraftWar.RoomStatus status) {
        if (status == null) {
            return getString(R.string.multiplayer_room_status_unknown);
        }
        return switch (status) {
            case ROOM_STATUS_WAITING -> getString(R.string.multiplayer_room_waiting);
            case ROOM_STATUS_FULL -> getString(R.string.multiplayer_room_full);
            case ROOM_STATUS_READY -> getString(R.string.multiplayer_room_ready);
            case ROOM_STATUS_PLAYING -> getString(R.string.multiplayer_room_playing);
            case ROOM_STATUS_FINISHED -> getString(R.string.multiplayer_room_finished);
            default -> getString(R.string.multiplayer_room_status_unknown);
        };
    }

    private String readableConnectionStatus(MultiplayerSession.ConnectionState state) {
        return switch (state) {
            case CONNECTED -> getString(R.string.multiplayer_connection_connected);
            case CONNECTING -> getString(R.string.multiplayer_connection_connecting);
            case ERROR -> getString(R.string.multiplayer_connection_error);
            default -> getString(R.string.multiplayer_connection_disconnected);
        };
    }

    private void finishToMenu() {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @FunctionalInterface
    private interface RoomCall<T> {
        T call() throws IOException;
    }

    @FunctionalInterface
    private interface RoomResultHandler<T> {
        void handle(T result);
    }
}
