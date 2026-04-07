package com.airwar.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

        TextView title = findViewById(R.id.comp_top_header_title);
        TextView emptyText = findViewById(R.id.leaderboard_empty_text);
        LinearLayout top3Container = findViewById(R.id.leaderboard_top3_container);
        LinearLayout listContainer = findViewById(R.id.leaderboard_list_container);
        Button backToMenuButton = findViewById(R.id.leaderboard_back_menu_button);
        String difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        if (difficulty == null || difficulty.trim().isEmpty()) {
            difficulty = MenuActivity.DIFFICULTY_NORMAL;
        }
        title.setText(getString(R.string.leaderboard_title_with_difficulty, readableDifficulty(difficulty)));
        bindBottomNav();

        backToMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        AndroidScoreDao dao = new AndroidScoreDao(this);
        List<GameScore> scores = dao.readScoresSortedByDifficulty(difficulty);

        if (scores.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            return;
        }

        emptyText.setVisibility(View.GONE);
        top3Container.removeAllViews();
        listContainer.removeAllViews();

        for (int i = 0; i < scores.size(); i++) {
            GameScore gameScore = scores.get(i);
            int rank = i + 1;
            if (rank <= 3) {
                top3Container.addView(createTopRankCard(rank, gameScore));
            } else {
                View row = getLayoutInflater().inflate(R.layout.comp_rank_row, listContainer, false);
                TextView rankView = row.findViewById(R.id.rank_index);
                ImageView avatarView = row.findViewById(R.id.rank_avatar);
                TextView nameView = row.findViewById(R.id.rank_name);
                TextView scoreView = row.findViewById(R.id.rank_score);

                rankView.setText(String.format(Locale.ROOT, "%02d", rank));
                avatarView.setImageResource(PilotAvatarRegistry.drawableFor(gameScore.getAvatarId()));
                nameView.setText(gameScore.getName());
                scoreView.setText(String.valueOf(gameScore.getScore()));
                listContainer.addView(row);
            }
        }
    }

    private void bindBottomNav() {
        TextView homeLabel = findViewById(R.id.nav_home_label);
        TextView rankingLabel = findViewById(R.id.nav_ranking_label);

        homeLabel.setTextColor(ContextCompat.getColor(this, R.color.ui2_body));
        rankingLabel.setTextColor(ContextCompat.getColor(this, R.color.ui2_accent));

        findViewById(R.id.nav_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.nav_ranking).setOnClickListener(v -> {
        });
    }

    private View createTopRankCard(int rank, GameScore score) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackgroundResource(R.drawable.ui2_panel_glass);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(8);
        card.setLayoutParams(cardParams);

        TextView rankView = new TextView(this);
        rankView.setText(String.valueOf(rank));
        rankView.setTextColor(ContextCompat.getColor(this, R.color.ui2_accent));
        rankView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        rankView.setTypeface(rankView.getTypeface(), android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams rankParams = new LinearLayout.LayoutParams(dp(26), LinearLayout.LayoutParams.WRAP_CONTENT);
        rankView.setLayoutParams(rankParams);

        ImageView avatar = new ImageView(this);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(44), dp(44));
        avatarParams.setMarginEnd(dp(12));
        avatar.setLayoutParams(avatarParams);
        avatar.setBackgroundResource(R.drawable.ui2_avatar_border_selected);
        avatar.setPadding(dp(2), dp(2), dp(2), dp(2));
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setImageResource(PilotAvatarRegistry.drawableFor(score.getAvatarId()));

        LinearLayout textWrap = new LinearLayout(this);
        textWrap.setOrientation(LinearLayout.VERTICAL);
        textWrap.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = new TextView(this);
        name.setText(score.getName());
        name.setTextColor(ContextCompat.getColor(this, R.color.ui2_title));
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        name.setTypeface(name.getTypeface(), android.graphics.Typeface.BOLD);

        TextView duration = new TextView(this);
        duration.setText(getString(R.string.game_over_duration_format, score.getDurationSec()));
        duration.setTextColor(ContextCompat.getColor(this, R.color.ui2_muted));
        duration.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        textWrap.addView(name);
        textWrap.addView(duration);

        TextView scoreText = new TextView(this);
        scoreText.setText(String.valueOf(score.getScore()));
        scoreText.setTextColor(ContextCompat.getColor(this, R.color.ui2_accent));
        scoreText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        scoreText.setTypeface(scoreText.getTypeface(), android.graphics.Typeface.BOLD);

        card.addView(rankView);
        card.addView(avatar);
        card.addView(textWrap);
        card.addView(scoreText);
        return card;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }

    private String readableDifficulty(String difficulty) {
        return switch (difficulty.toLowerCase(Locale.ROOT)) {
            case MenuActivity.DIFFICULTY_EASY -> getString(R.string.difficulty_label_easy);
            case MenuActivity.DIFFICULTY_HARD -> getString(R.string.difficulty_label_hard);
            default -> getString(R.string.difficulty_label_normal);
        };
    }
}
