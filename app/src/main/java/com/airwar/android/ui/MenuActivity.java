package com.airwar.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airwar.android.R;
import com.airwar.core.difficulty.DifficultyConfig;
import com.airwar.core.difficulty.DifficultyLevel;

public class MenuActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "com.airwar.android.extra.DIFFICULTY";
    public static final String DIFFICULTY_EASY = "easy";
    public static final String DIFFICULTY_NORMAL = "normal";
    public static final String DIFFICULTY_HARD = "hard";

    private String selectedDifficulty = DIFFICULTY_NORMAL;
    private TextView balanceTable;
    private ImageView difficultyPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button easyButton = findViewById(R.id.button_difficulty_easy);
        Button normalButton = findViewById(R.id.button_difficulty_normal);
        Button hardButton = findViewById(R.id.button_difficulty_hard);
        Button startButton = findViewById(R.id.button_start);
        balanceTable = findViewById(R.id.text_balance_table);
        difficultyPreview = findViewById(R.id.image_difficulty_preview);
        TextView headerTitle = findViewById(R.id.comp_top_header_title);

        headerTitle.setText(getString(R.string.menu_title));
        bindBottomNav();

        easyButton.setOnClickListener(v -> {
            selectedDifficulty = DIFFICULTY_EASY;
            updateDifficultySelection(easyButton, normalButton, hardButton, DifficultyLevel.EASY);
        });

        normalButton.setOnClickListener(v -> {
            selectedDifficulty = DIFFICULTY_NORMAL;
            updateDifficultySelection(normalButton, easyButton, hardButton, DifficultyLevel.NORMAL);
        });

        hardButton.setOnClickListener(v -> {
            selectedDifficulty = DIFFICULTY_HARD;
            updateDifficultySelection(hardButton, easyButton, normalButton, DifficultyLevel.HARD);
        });

        updateDifficultySelection(normalButton, easyButton, hardButton, DifficultyLevel.NORMAL);

        startButton.setOnClickListener(v -> {
            Intent gameIntent = new Intent(this, GameActivity.class);
            gameIntent.putExtra(EXTRA_DIFFICULTY, selectedDifficulty);
            startActivity(gameIntent);
        });
    }

    private void updateDifficultySelection(Button selected, Button otherOne, Button otherTwo, DifficultyLevel level) {
        selected.setSelected(true);
        otherOne.setSelected(false);
        otherTwo.setSelected(false);

        DifficultyConfig cfg = DifficultyConfig.of(level);
        int previewRes = switch (level) {
            case EASY -> R.drawable.bg2;
            case NORMAL -> R.drawable.bg3;
            case HARD -> R.drawable.bg5;
        };
        difficultyPreview.setImageResource(previewRes);

        balanceTable.setText(getString(
                R.string.menu_balance_template,
                cfg.enemySpawnIntervalMs(),
                cfg.enemyShootIntervalMs(),
                cfg.mobEnemyHp(),
                cfg.enemyBulletDamage(),
                cfg.enemyCollisionDamage(),
                cfg.propDropChancePercent()
        ));
    }

    private void bindBottomNav() {
        TextView homeLabel = findViewById(R.id.nav_home_label);
        TextView rankingLabel = findViewById(R.id.nav_ranking_label);

        homeLabel.setTextColor(ContextCompat.getColor(this, R.color.ui2_accent));
        rankingLabel.setTextColor(ContextCompat.getColor(this, R.color.ui2_body));

        findViewById(R.id.nav_home).setOnClickListener(v -> {
        });

        findViewById(R.id.nav_ranking).setOnClickListener(v -> {
            Intent leaderboardIntent = new Intent(this, LeaderboardActivity.class);
            leaderboardIntent.putExtra(LeaderboardActivity.EXTRA_DIFFICULTY, selectedDifficulty);
            startActivity(leaderboardIntent);
        });
    }
}
