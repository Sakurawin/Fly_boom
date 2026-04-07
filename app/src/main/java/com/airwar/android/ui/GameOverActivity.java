package com.airwar.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airwar.android.R;
import com.airwar.android.storage.AndroidScoreDao;
import com.airwar.android.storage.GameScore;

public class GameOverActivity extends AppCompatActivity {
    public static final String EXTRA_SCORE = "extra_score";
    public static final String EXTRA_DURATION_SEC = "extra_duration_sec";
    public static final String EXTRA_DIFFICULTY = "extra_difficulty";

    private final RandomPilotNameGenerator nameGenerator = new RandomPilotNameGenerator();
    private String selectedAvatarId = PilotAvatarRegistry.DEFAULT_AVATAR_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int durationSec = getIntent().getIntExtra(EXTRA_DURATION_SEC, 0);
        String difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);

        TextView headerTitle = findViewById(R.id.comp_top_header_title);
        TextView scoreText = findViewById(R.id.game_over_score);
        TextView durationText = findViewById(R.id.game_over_duration);
        EditText nameInput = findViewById(R.id.game_over_name_input);
        LinearLayout avatarContainer = findViewById(R.id.game_over_avatar_container);
        Button submitButton = findViewById(R.id.game_over_submit_button);

        headerTitle.setText(getString(R.string.game_over_title));
        bindBottomNav();

        scoreText.setText(getString(R.string.game_over_score_format, score));
        durationText.setText(getString(R.string.game_over_duration_format, durationSec));
        nameInput.setText(nameGenerator.generate());

        buildAvatarSelector(avatarContainer);

        submitButton.setOnClickListener(v -> {
            String inputName = nameInput.getText() == null ? "" : nameInput.getText().toString().trim();
            String playerName = inputName.isEmpty() ? nameGenerator.generate() : inputName;

            AndroidScoreDao dao = new AndroidScoreDao(this);
            dao.appendScore(new GameScore(score, playerName, durationSec, difficulty, selectedAvatarId));

            Intent leaderboardIntent = new Intent(this, LeaderboardActivity.class);
            leaderboardIntent.putExtra(LeaderboardActivity.EXTRA_DIFFICULTY, difficulty);
            startActivity(leaderboardIntent);
            finish();
        });
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

    private void buildAvatarSelector(LinearLayout container) {
        container.removeAllViews();
        for (int i = 0; i < PilotAvatarRegistry.IDS.length; i++) {
            String avatarId = PilotAvatarRegistry.IDS[i];
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(56), dp(56));
            params.setMarginEnd(dp(10));
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundResource(R.drawable.ui2_avatar_border);
            imageView.setImageResource(PilotAvatarRegistry.drawableFor(avatarId));
            imageView.setTag(avatarId);
            imageView.setPadding(dp(2), dp(2), dp(2), dp(2));
            imageView.setOnClickListener(v -> {
                selectedAvatarId = avatarId;
                refreshAvatarSelection(container);
            });
            container.addView(imageView);

            if (i == 0) {
                selectedAvatarId = avatarId;
            }
        }
        refreshAvatarSelection(container);
    }

    private void refreshAvatarSelection(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            ImageView imageView = (ImageView) container.getChildAt(i);
            Object tag = imageView.getTag();
            String avatarId = tag == null ? "" : tag.toString();
            imageView.setBackgroundResource(
                    avatarId.equals(selectedAvatarId)
                            ? R.drawable.ui2_avatar_border_selected
                            : R.drawable.ui2_avatar_border
            );
        }
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}
