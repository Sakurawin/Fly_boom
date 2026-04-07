package com.airwar.core.difficulty;

public record DifficultyConfig(
        int enemyMaxNumber,
        int cycleDurationMs,
        int bossScoreThreshold,
        int enemySpawnIntervalMs,
        int enemyShootIntervalMs,
        int mobEnemyHp,
        int enemyBulletDamage,
        int enemyCollisionDamage,
        int propDropChancePercent
) {

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
        if (enemySpawnIntervalMs <= 0) {
            throw new IllegalArgumentException("enemySpawnIntervalMs must be positive");
        }
        if (enemyShootIntervalMs <= 0) {
            throw new IllegalArgumentException("enemyShootIntervalMs must be positive");
        }
        if (mobEnemyHp <= 0) {
            throw new IllegalArgumentException("mobEnemyHp must be positive");
        }
        if (enemyBulletDamage <= 0) {
            throw new IllegalArgumentException("enemyBulletDamage must be positive");
        }
        if (enemyCollisionDamage <= 0) {
            throw new IllegalArgumentException("enemyCollisionDamage must be positive");
        }
        if (propDropChancePercent < 0 || propDropChancePercent > 100) {
            throw new IllegalArgumentException("propDropChancePercent must be in [0,100]");
        }
    }

    public static DifficultyConfig of(DifficultyLevel level) {
        if (level == null) {
            throw new IllegalArgumentException("level must not be null");
        }
        return switch (level) {
            case EASY -> new DifficultyConfig(5, 600, 200, 760, 920, 1, 4, 10, 45);
            case NORMAL -> new DifficultyConfig(6, 520, 220, 650, 800, 2, 5, 15, 35);
            case HARD -> new DifficultyConfig(7, 460, 250, 540, 700, 3, 7, 20, 25);
        };
    }
}
