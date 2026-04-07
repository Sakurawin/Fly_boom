package com.airwar.android.ui;

import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airwar.android.R;
import com.airwar.android.audio.AndroidAudioManager;
import com.airwar.android.view.GameSurfaceView;
import com.airwar.core.engine.GameStateSnapshot;

public class GameActivity extends AppCompatActivity {
    public static final String EXTRA_DIFFICULTY = MenuActivity.EXTRA_DIFFICULTY;

    private AndroidAudioManager audioManager;
    private String difficulty;
    private TextView hudScore;
    private TextView hudHp;
    private long gameStartMs;
    private int lastGameOverEvents;
    private boolean gameOverNavigated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_game);

        difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        audioManager = new AndroidAudioManager(this);
        gameStartMs = System.currentTimeMillis();

        hudScore = findViewById(R.id.hud_score);
        hudHp = findViewById(R.id.hud_hp);

        GameSurfaceView gameSurfaceView = findViewById(R.id.game_surface);
        gameSurfaceView.setDifficulty(difficulty);
        gameSurfaceView.setAudioManager(audioManager);
        gameSurfaceView.setSnapshotListener(this::updateHud);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (audioManager != null) {
            audioManager.playBgmGame();
        }
    }

    @Override
    protected void onPause() {
        if (audioManager != null) {
            audioManager.stopAllBgm();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (audioManager != null) {
            audioManager.release();
        }
        super.onDestroy();
    }

    private void updateHud(GameStateSnapshot snapshot) {
        if (hudScore != null) {
            hudScore.setText(getString(R.string.hud_score_value, snapshot.score()));
        }
        if (hudHp != null) {
            hudHp.setText(getString(R.string.hud_hp_value, snapshot.heroHp()));
        }

        if (!gameOverNavigated && snapshot.gameOverEvents() > lastGameOverEvents) {
            gameOverNavigated = true;
            int durationSec = (int) Math.max(1L, (System.currentTimeMillis() - gameStartMs) / 1000L);
            Intent intent = new Intent(this, GameOverActivity.class);
            intent.putExtra(GameOverActivity.EXTRA_SCORE, snapshot.score());
            intent.putExtra(GameOverActivity.EXTRA_DURATION_SEC, durationSec);
            intent.putExtra(GameOverActivity.EXTRA_DIFFICULTY, difficulty == null ? MenuActivity.DIFFICULTY_NORMAL : difficulty);
            startActivity(intent);
            finish();
        }
        lastGameOverEvents = snapshot.gameOverEvents();
    }
}
