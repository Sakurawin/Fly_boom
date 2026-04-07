package com.airwar.android.ui;

import android.content.pm.ActivityInfo;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_game);

        difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        audioManager = new AndroidAudioManager(this);

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
    }
}
