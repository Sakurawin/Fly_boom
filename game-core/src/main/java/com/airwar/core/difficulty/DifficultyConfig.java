package com.airwar.core.difficulty;

public record DifficultyConfig(int enemyMaxNumber, int cycleDurationMs, int bossScoreThreshold) {

    public DifficultyConfig {
        if (enemyMaxNumber <= 0) {
            throw new IllegalArgumentException("enemyMaxNumber must be positive");
        }
        if (cycleDurationMs <= 0) {
            throw new IllegalArgumentException("cycleDurationMs must be positive");
        }
        if (bossScoreThreshold <= 0) {
            throw new IllegalArgumentException("bossScoreThreshold must be positive");
        }
    }

    public static DifficultyConfig of(DifficultyLevel level) {
        if (level == null) {
            throw new IllegalArgumentException("level must not be null");
        }
        return switch (level) {
            case EASY -> new DifficultyConfig(5, 600, 200);
            case NORMAL -> new DifficultyConfig(6, 520, 220);
            case HARD -> new DifficultyConfig(7, 460, 250);
        };
    }
}
