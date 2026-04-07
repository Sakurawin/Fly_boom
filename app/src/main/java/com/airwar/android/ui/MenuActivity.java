package com.airwar.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.airwar.android.R;

public class MenuActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "com.airwar.android.extra.DIFFICULTY";
    public static final String DIFFICULTY_EASY = "easy";
    public static final String DIFFICULTY_NORMAL = "normal";
    public static final String DIFFICULTY_HARD = "hard";

    private String selectedDifficulty = DIFFICULTY_NORMAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button easyButton = findViewById(R.id.button_difficulty_easy);
        Button normalButton = findViewById(R.id.button_difficulty_normal);
        Button hardButton = findViewById(R.id.button_difficulty_hard);
        Button startButton = findViewById(R.id.button_start);

        easyButton.setOnClickListener(v -> {
            selectedDifficulty = DIFFICULTY_EASY;
            updateDifficultySelection(easyButton, normalButton, hardButton);
        });

        normalButton.setOnClickListener(v -> {
            selectedDifficulty = DIFFICULTY_NORMAL;
            updateDifficultySelection(normalButton, easyButton, hardButton);
        });

        hardButton.setOnClickListener(v -> {
            selectedDifficulty = DIFFICULTY_HARD;
            updateDifficultySelection(hardButton, easyButton, normalButton);
        });

        updateDifficultySelection(normalButton, easyButton, hardButton);

        startButton.setOnClickListener(v -> {
            Intent gameIntent = new Intent(this, GameActivity.class);
            gameIntent.putExtra(EXTRA_DIFFICULTY, selectedDifficulty);
            startActivity(gameIntent);
        });
    }

    private void updateDifficultySelection(Button selected, Button otherOne, Button otherTwo) {
        selected.setEnabled(false);
        otherOne.setEnabled(true);
        otherTwo.setEnabled(true);
    }
}
