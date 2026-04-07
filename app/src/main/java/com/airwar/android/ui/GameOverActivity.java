package com.airwar.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airwar.android.R;
import com.airwar.android.storage.AndroidScoreDao;
import com.airwar.android.storage.GameScore;

public class GameOverActivity extends AppCompatActivity {
    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_DURATION_SEC = "extra_duration_sec";
    public static final String EXTRA_DIFFICULTY = "extra_difficulty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int durationSec = getIntent().getIntExtra(EXTRA_DURATION_SEC, 0);
        String difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);

        TextView scoreText = findViewById(R.id.game_over_score);
        TextView durationText = findViewById(R.id.game_over_duration);
        EditText nameInput = findViewById(R.id.game_over_name_input);
        Button submitButton = findViewById(R.id.game_over_submit_button);

        scoreText.setText(getString(R.string.game_over_score_format, score));
        durationText.setText(getString(R.string.game_over_duration_format, durationSec));

        submitButton.setOnClickListener(v -> {
            String inputName = nameInput.getText() == null ? "" : nameInput.getText().toString().trim();
            String playerName = inputName.isEmpty() ? getString(R.string.default_player_name) : inputName;

            AndroidScoreDao dao = new AndroidScoreDao(this);
            dao.appendScore(new GameScore(score, playerName, durationSec, difficulty));

            Intent leaderboardIntent = new Intent(this, LeaderboardActivity.class);
            leaderboardIntent.putExtra(LeaderboardActivity.EXTRA_DIFFICULTY, difficulty);
            startActivity(leaderboardIntent);
            finish();
        });
    }
}
