package com.airwar.android.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airwar.android.R;
import com.airwar.android.storage.AndroidScoreDao;
import com.airwar.android.storage.GameScore;

import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        TextView content = findViewById(R.id.leaderboard_content);
        AndroidScoreDao dao = new AndroidScoreDao(this);
        List<GameScore> scores = dao.readScoresSorted();

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
}
