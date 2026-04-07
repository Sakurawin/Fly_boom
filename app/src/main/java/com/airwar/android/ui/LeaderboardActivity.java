package com.airwar.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airwar.android.R;
import com.airwar.android.storage.AndroidScoreDao;
import com.airwar.android.storage.GameScore;

import java.util.List;
import java.util.Locale;

public class LeaderboardActivity extends AppCompatActivity {
    public static final String EXTRA_DIFFICULTY = "extra_difficulty";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        TextView title = findViewById(R.id.leaderboard_title);
        TextView content = findViewById(R.id.leaderboard_content);
        Button backToMenuButton = findViewById(R.id.leaderboard_back_menu_button);
        String difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        if (difficulty == null || difficulty.isBlank()) {
            difficulty = MenuActivity.DIFFICULTY_NORMAL;
        }
        title.setText(getString(R.string.leaderboard_title_with_difficulty, readableDifficulty(difficulty)));

        backToMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        AndroidScoreDao dao = new AndroidScoreDao(this);
        List<GameScore> scores = dao.readScoresSortedByDifficulty(difficulty);

        if (scores.isEmpty()) {
            content.setText(getString(R.string.leaderboard_empty));
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < scores.size(); i++) {
            GameScore gameScore = scores.get(i);
            builder.append(getString(
                    R.string.leaderboard_line_format,
                    i + 1,
                    gameScore.getName(),
                    gameScore.getScore(),
                    gameScore.getDurationSec()
            ));
            if (i < scores.size() - 1) {
                builder.append('\n');
            }
        }
        content.setText(builder.toString());
    }

    private String readableDifficulty(String difficulty) {
        return switch (difficulty.toLowerCase(Locale.ROOT)) {
            case MenuActivity.DIFFICULTY_EASY -> getString(R.string.difficulty_label_easy);
            case MenuActivity.DIFFICULTY_HARD -> getString(R.string.difficulty_label_hard);
            default -> getString(R.string.difficulty_label_normal);
        };
    }
}
