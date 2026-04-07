package com.airwar.android.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.airwar.android.R;
import com.airwar.android.audio.AndroidAudioManager;

public class GameActivity extends AppCompatActivity {
    public static final String EXTRA_DIFFICULTY = MenuActivity.EXTRA_DIFFICULTY;

    private AndroidAudioManager audioManager;
    private String difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_game);

        difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        audioManager = new AndroidAudioManager(this);
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
}
