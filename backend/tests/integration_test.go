package tests

import (
	"bytes"
	"database/sql"
	"net/http"
	"net/http/httptest"
	"net/url"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"github.com/gorilla/websocket"
	_ "modernc.org/sqlite"
	"google.golang.org/protobuf/proto"

	"github.com/acc1111/aircraft-war-hitsz/backend/internal/app"
	pb "github.com/acc1111/aircraft-war-hitsz/backend/proto"
)

func TestRoomLifecycleScoreResultAndLeaderboard(t *testing.T) {
	server := newFileDBTestServer(t, "full-lifecycle.sqlite")
	defer server.Close()

	createResp := postProto(t, server.URL, "/rooms/create", &pb.CreateRoomRequest{Username: "alice"}, &pb.CreateRoomResponse{}).(*pb.CreateRoomResponse)
	roomID := createResp.Room.RoomId
	postProto(t, server.URL, "/rooms/join", &pb.JoinRoomRequest{RoomId: roomID, Username: "bob"}, &pb.JoinRoomResponse{})
	postProto(t, server.URL, "/rooms/ready", &pb.ReadyRoomRequest{RoomId: roomID, Username: "alice"}, &pb.ReadyRoomResponse{})
	readyResp := postProto(t, server.URL, "/rooms/ready", &pb.ReadyRoomRequest{RoomId: roomID, Username: "bob"}, &pb.ReadyRoomResponse{}).(*pb.ReadyRoomResponse)
	if got := readyResp.Room.Status; got != pb.RoomStatus_ROOM_STATUS_READY {
		t.Fatalf("room status after ready = %v, want READY", got)
	}
	startResp := postProto(t, server.URL, "/rooms/start", &pb.StartGameRequest{RoomId: roomID, Username: "alice"}, &pb.StartGameResponse{}).(*pb.StartGameResponse)
	if !startResp.Started {
		t.Fatal("expected room to start")
	}

	aliceWS := openWS(t, server.URL, roomID, "alice")
	defer aliceWS.Close()
	bobWS := openWS(t, server.URL, roomID, "bob")
	defer bobWS.Close()

	writeProtoWS(t, aliceWS, wrapHeartbeat(roomID, "alice", 1))
	writeProtoWS(t, bobWS, wrapHeartbeat(roomID, "bob", 1))
	writeProtoWS(t, aliceWS, wrapDefeat(roomID, "alice", pb.EnemyType_ENEMY_TYPE_BOSS, "a-1"))

	broadcastA := readScoreBroadcast(t, aliceWS)
	broadcastB := readScoreBroadcast(t, bobWS)
	assertScore(t, broadcastA, "alice", 50, false, pb.PlayerFinishReason_PLAYER_FINISH_REASON_UNSPECIFIED)
	assertScore(t, broadcastB, "alice", 50, false, pb.PlayerFinishReason_PLAYER_FINISH_REASON_UNSPECIFIED)

	writeProtoWS(t, aliceWS, wrapGameOver(roomID, "alice", 50, "done"))
	broadcastAfterA := readScoreBroadcast(t, bobWS)
	assertScore(t, broadcastAfterA, "alice", 50, true, pb.PlayerFinishReason_PLAYER_FINISH_REASON_NORMAL)

	writeProtoWS(t, bobWS, wrapDefeat(roomID, "bob", pb.EnemyType_ENEMY_TYPE_ELITE, "b-1"))
	readScoreBroadcast(t, aliceWS)
	readScoreBroadcast(t, bobWS)

	writeProtoWS(t, bobWS, wrapGameOver(roomID, "bob", 20, "done"))
	finishedA := readGameFinished(t, aliceWS)
	finishedB := readGameFinished(t, bobWS)
	if finishedA.Result.WinnerUsername != "alice" || finishedB.Result.WinnerUsername != "alice" {
		t.Fatalf("winner mismatch: %q %q", finishedA.Result.WinnerUsername, finishedB.Result.WinnerUsername)
	}

	resultResp := postProto(t, server.URL, "/rooms/result", &pb.GetRoomResultRequest{RoomId: roomID, Username: "alice"}, &pb.GetRoomResultResponse{}).(*pb.GetRoomResultResponse)
	if got := resultResp.Result.SelfResult; got != pb.GameResultType_GAME_RESULT_WIN {
		t.Fatalf("alice self result = %v, want WIN", got)
	}
	if got := resultResp.Result.PlayerAFinishReason; got != pb.PlayerFinishReason_PLAYER_FINISH_REASON_NORMAL {
		t.Fatalf("player a finish reason = %v, want NORMAL", got)
	}
	if got := resultResp.Result.PlayerBFinishReason; got != pb.PlayerFinishReason_PLAYER_FINISH_REASON_NORMAL {
		t.Fatalf("player b finish reason = %v, want NORMAL", got)
	}
	leaderboard := postProto(t, server.URL, "/leaderboard", &pb.GetLeaderboardRequest{Limit: 10}, &pb.GetLeaderboardResponse{}).(*pb.GetLeaderboardResponse)
	if len(leaderboard.Entries) != 2 {
		t.Fatalf("leaderboard size = %d, want 2", len(leaderboard.Entries))
	}
	if leaderboard.Entries[0].Username != "alice" || leaderboard.Entries[0].BestScore != 50 || leaderboard.Entries[0].WinCount != 1 || leaderboard.Entries[0].GameCount != 1 {
		t.Fatalf("unexpected top leaderboard entry: %+v", leaderboard.Entries[0])
	}

	// 直接校验 SQLite 持久化结果，确保完整对局结束后房间结果和排行榜都已落库。
	db := openSQLiteForAssert(t, server.dbPath)
	defer db.Close()

	var storedWinner string
	var playerAScore, playerBScore int32
	if err := db.QueryRow(`SELECT winner_username, player_a_score, player_b_score FROM room_results WHERE room_id = ?`, roomID).
		Scan(&storedWinner, &playerAScore, &playerBScore); err != nil {
		t.Fatalf("query room_results: %v", err)
	}
	if storedWinner != "alice" || playerAScore != 50 || playerBScore != 20 {
		t.Fatalf("unexpected stored room result: winner=%q a=%d b=%d", storedWinner, playerAScore, playerBScore)
	}

	entries := loadLeaderboardRows(t, db)
	if len(entries) != 2 {
		t.Fatalf("stored leaderboard size = %d, want 2", len(entries))
	}
	if entries[0].Username != "alice" || entries[0].BestScore != 50 || entries[0].WinCount != 1 || entries[0].GameCount != 1 {
		t.Fatalf("unexpected stored top leaderboard entry: %+v", entries[0])
	}
}

func TestDisconnectFreezesScoreAndStateRecovery(t *testing.T) {
	server := newTestServer(t)
	defer server.Close()

	createResp := postProto(t, server.URL, "/rooms/create", &pb.CreateRoomRequest{Username: "alice"}, &pb.CreateRoomResponse{}).(*pb.CreateRoomResponse)
	roomID := createResp.Room.RoomId
	postProto(t, server.URL, "/rooms/join", &pb.JoinRoomRequest{RoomId: roomID, Username: "bob"}, &pb.JoinRoomResponse{})
	postProto(t, server.URL, "/rooms/ready", &pb.ReadyRoomRequest{RoomId: roomID, Username: "alice"}, &pb.ReadyRoomResponse{})
	postProto(t, server.URL, "/rooms/ready", &pb.ReadyRoomRequest{RoomId: roomID, Username: "bob"}, &pb.ReadyRoomResponse{})
	postProto(t, server.URL, "/rooms/start", &pb.StartGameRequest{RoomId: roomID, Username: "alice"}, &pb.StartGameResponse{})

	aliceWS := openWS(t, server.URL, roomID, "alice")
	defer aliceWS.Close()
	bobWS := openWS(t, server.URL, roomID, "bob")
	defer bobWS.Close()

	writeProtoWS(t, aliceWS, wrapDefeat(roomID, "alice", pb.EnemyType_ENEMY_TYPE_MOB, "a-1"))
	readScoreBroadcast(t, aliceWS)
	readScoreBroadcast(t, bobWS)

	writeProtoWS(t, bobWS, wrapHeartbeat(roomID, "bob", 1))
	time.Sleep(60 * time.Millisecond)
	writeProtoWS(t, bobWS, wrapHeartbeat(roomID, "bob", 2))
	time.Sleep(90 * time.Millisecond)

	disconnectBroadcast := readScoreBroadcast(t, bobWS)
	assertScore(t, disconnectBroadcast, "alice", 10, true, pb.PlayerFinishReason_PLAYER_FINISH_REASON_DISCONNECTED)

	writeProtoWS(t, aliceWS, wrapDefeat(roomID, "alice", pb.EnemyType_ENEMY_TYPE_BOSS, "a-2"))
	writeProtoWS(t, bobWS, wrapDefeat(roomID, "bob", pb.EnemyType_ENEMY_TYPE_BOSS, "b-1"))
	continueBroadcast := readScoreBroadcast(t, bobWS)
	assertScore(t, continueBroadcast, "alice", 10, true, pb.PlayerFinishReason_PLAYER_FINISH_REASON_DISCONNECTED)
	assertScore(t, continueBroadcast, "bob", 50, false, pb.PlayerFinishReason_PLAYER_FINISH_REASON_UNSPECIFIED)

	stateResp := postProto(t, server.URL, "/rooms/state", &pb.GetRoomStateRequest{RoomId: roomID, Username: "alice"}, &pb.GetRoomStateResponse{}).(*pb.GetRoomStateResponse)
	if stateResp.RoomFinished {
		t.Fatal("room should not be finished while bob is still playing")
	}
	if stateResp.Result != nil {
		t.Fatal("room state should not include final result before room finished")
	}
	assertScore(t, &pb.ScoreBroadcast{Scores: stateResp.Scores}, "alice", 10, true, pb.PlayerFinishReason_PLAYER_FINISH_REASON_DISCONNECTED)

	writeProtoWS(t, bobWS, wrapGameOver(roomID, "bob", 50, "done"))
	finished := readGameFinished(t, bobWS)
	if finished.Result.WinnerUsername != "bob" {
		t.Fatalf("winner = %q, want bob", finished.Result.WinnerUsername)
	}
	resultResp := postProto(t, server.URL, "/rooms/result", &pb.GetRoomResultRequest{RoomId: roomID, Username: "alice"}, &pb.GetRoomResultResponse{}).(*pb.GetRoomResultResponse)
	if got := resultResp.Result.PlayerAFinishReason; got == pb.PlayerFinishReason_PLAYER_FINISH_REASON_UNSPECIFIED {
		t.Fatal("expected disconnect finish reason to be recorded")
	}
	finalState := postProto(t, server.URL, "/rooms/state", &pb.GetRoomStateRequest{RoomId: roomID, Username: "alice"}, &pb.GetRoomStateResponse{}).(*pb.GetRoomStateResponse)
	if !finalState.RoomFinished {
		t.Fatal("room should be finished after both players end")
	}
	if finalState.Result == nil {
		t.Fatal("finished room state should include final result")
	}
	leaderboard := postProto(t, server.URL, "/leaderboard", &pb.GetLeaderboardRequest{Limit: 10}, &pb.GetLeaderboardResponse{}).(*pb.GetLeaderboardResponse)
	if leaderboard.Entries[0].Username != "bob" || leaderboard.Entries[1].Username != "alice" {
		t.Fatalf("unexpected leaderboard order after disconnect: %+v", leaderboard.Entries)
	}
}

type dbBackedServer struct {
	*httptest.Server
	dbPath string
}

func newTestServer(t *testing.T) *dbBackedServer {
	t.Helper()
	return newConfiguredTestServer(t, ":memory:")
}

func newFileDBTestServer(t *testing.T, fileName string) *dbBackedServer {
	t.Helper()
	dbPath := filepath.Join(t.TempDir(), fileName)
	// 显式删除旧数据库文件，避免重复运行时复用上一次残留数据。
	if err := os.Remove(dbPath); err != nil && !os.IsNotExist(err) {
		t.Fatalf("remove stale db: %v", err)
	}
	return newConfiguredTestServer(t, dbPath)
}

func newConfiguredTestServer(t *testing.T, dbPath string) *dbBackedServer {
	t.Helper()
	backend, err := app.NewServer(app.Config{
		DBPath:            dbPath,
		HeartbeatTimeout:  90 * time.Millisecond,
		HeartbeatRecheck:  30 * time.Millisecond,
		HeartbeatScanTick: 10 * time.Millisecond,
	})
	if err != nil {
		t.Fatalf("new server: %v", err)
	}
	httpServer := httptest.NewServer(backend.Handler())
	t.Cleanup(func() {
		httpServer.Close()
		_ = backend.Close()
	})
	return &dbBackedServer{Server: httpServer, dbPath: dbPath}
}

type leaderboardRow struct {
	Username  string
	BestScore int32
	WinCount  int32
	GameCount int32
}

func openSQLiteForAssert(t *testing.T, dbPath string) *sql.DB {
	t.Helper()
	db, err := sql.Open("sqlite", dbPath)
	if err != nil {
		t.Fatalf("open sqlite db: %v", err)
	}
	return db
}

func loadLeaderboardRows(t *testing.T, db *sql.DB) []leaderboardRow {
	t.Helper()
	rows, err := db.Query(`SELECT username, best_score, win_count, game_count FROM leaderboard ORDER BY best_score DESC, updated_at ASC, username ASC`)
	if err != nil {
		t.Fatalf("query leaderboard: %v", err)
	}
	defer rows.Close()
	var entries []leaderboardRow
	for rows.Next() {
		var entry leaderboardRow
		if err := rows.Scan(&entry.Username, &entry.BestScore, &entry.WinCount, &entry.GameCount); err != nil {
			t.Fatalf("scan leaderboard row: %v", err)
		}
		entries = append(entries, entry)
	}
	return entries
}

func postProto(t *testing.T, baseURL, path string, req proto.Message, response proto.Message) proto.Message {
	t.Helper()
	payload, err := proto.Marshal(req)
	if err != nil {
		t.Fatalf("marshal request: %v", err)
	}
	httpResp, err := http.Post(baseURL+path, "application/x-protobuf", bytes.NewReader(payload))
	if err != nil {
		t.Fatalf("post %s: %v", path, err)
	}
	defer httpResp.Body.Close()
	if httpResp.StatusCode != http.StatusOK {
		body := new(bytes.Buffer)
		_, _ = body.ReadFrom(httpResp.Body)
		t.Fatalf("post %s status=%d body=%s", path, httpResp.StatusCode, body.String())
	}
	body := new(bytes.Buffer)
	_, _ = body.ReadFrom(httpResp.Body)
	if err := proto.Unmarshal(body.Bytes(), response); err != nil {
		t.Fatalf("unmarshal response: %v", err)
	}
	return response
}

func openWS(t *testing.T, baseURL, roomID, username string) *websocket.Conn {
	t.Helper()
	wsURL := "ws" + strings.TrimPrefix(baseURL, "http") + "/ws?room_id=" + url.QueryEscape(roomID) + "&username=" + url.QueryEscape(username)
	conn, _, err := websocket.DefaultDialer.Dial(wsURL, nil)
	if err != nil {
		t.Fatalf("dial ws: %v", err)
	}
	_ = conn.SetReadDeadline(time.Now().Add(2 * time.Second))
	return conn
}

func writeProtoWS(t *testing.T, conn *websocket.Conn, msg proto.Message) {
	t.Helper()
	payload, err := proto.Marshal(msg)
	if err != nil {
		t.Fatalf("marshal ws msg: %v", err)
	}
	if err := conn.WriteMessage(websocket.BinaryMessage, payload); err != nil {
		t.Fatalf("write ws msg: %v", err)
	}
}

func wrapHeartbeat(roomID, username string, sequence int64) *pb.WsMessage {
	return &pb.WsMessage{Payload: &pb.WsMessage_PlayerHeartbeatEvent{PlayerHeartbeatEvent: &pb.PlayerHeartbeatEvent{RoomId: roomID, Username: username, Sequence: sequence}}}
}

func wrapDefeat(roomID, username string, enemyType pb.EnemyType, eventID string) *pb.WsMessage {
	return &pb.WsMessage{Payload: &pb.WsMessage_PlayerDefeatEvent{PlayerDefeatEvent: &pb.PlayerDefeatEvent{RoomId: roomID, Username: username, EnemyType: enemyType, ClientEventId: eventID}}}
}

func wrapGameOver(roomID, username string, finalScore int32, reason string) *pb.WsMessage {
	return &pb.WsMessage{Payload: &pb.WsMessage_PlayerGameOverEvent{PlayerGameOverEvent: &pb.PlayerGameOverEvent{RoomId: roomID, Username: username, FinalScore: finalScore, Reason: reason}}}
}

func readScoreBroadcast(t *testing.T, conn *websocket.Conn) *pb.ScoreBroadcast {
	t.Helper()
	msgType, payload, err := conn.ReadMessage()
	if err != nil {
		t.Fatalf("read score broadcast: %v", err)
	}
	if msgType != websocket.BinaryMessage {
		t.Fatalf("message type = %d, want binary", msgType)
	}
	frame := &pb.WsMessage{}
	if err := proto.Unmarshal(payload, frame); err != nil {
		t.Fatalf("unmarshal ws frame: %v", err)
	}
	msg, ok := frame.Payload.(*pb.WsMessage_ScoreBroadcast)
	if !ok {
		t.Fatalf("payload type = %T, want score broadcast", frame.Payload)
	}
	return msg.ScoreBroadcast
}

func readGameFinished(t *testing.T, conn *websocket.Conn) *pb.GameFinishedBroadcast {
	t.Helper()
	for {
		msgType, payload, err := conn.ReadMessage()
		if err != nil {
			t.Fatalf("read game finished: %v", err)
		}
		if msgType != websocket.BinaryMessage {
			continue
		}
		frame := &pb.WsMessage{}
		if err := proto.Unmarshal(payload, frame); err != nil {
			t.Fatalf("unmarshal ws frame: %v", err)
		}
		switch msg := frame.Payload.(type) {
		case *pb.WsMessage_ScoreBroadcast:
			continue
		case *pb.WsMessage_GameFinishedBroadcast:
			return msg.GameFinishedBroadcast
		default:
			t.Fatalf("payload type = %T, want game finished", frame.Payload)
		}
	}
}

func assertScore(t *testing.T, broadcast *pb.ScoreBroadcast, username string, wantScore int32, wantFinished bool, wantReason pb.PlayerFinishReason) {
	t.Helper()
	for _, score := range broadcast.Scores {
		if score.Username != username {
			continue
		}
		if score.Score != wantScore || score.Finished != wantFinished || score.FinishReason != wantReason {
			t.Fatalf("score[%s] = %+v, want score=%d finished=%v reason=%v", username, score, wantScore, wantFinished, wantReason)
		}
		return
	}
	t.Fatalf("score for %s not found", username)
}
