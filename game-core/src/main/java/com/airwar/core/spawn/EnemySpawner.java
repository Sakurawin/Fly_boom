package com.airwar.core.spawn;

import com.airwar.core.difficulty.DifficultyConfig;

import java.util.Objects;

public final class EnemySpawner {

    private final int bossScoreThreshold;
    private int bossCount;
    private int maxSpawnBucket;

    public EnemySpawner(DifficultyConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.bossScoreThreshold = config.bossScoreThreshold();
    }

    public void update(int score) {
        if (score <= 0 || bossScoreThreshold <= 0) {
            return;
        }
        int currentBucket = score / bossScoreThreshold;
        if (currentBucket > maxSpawnBucket) {
            bossCount += (currentBucket - maxSpawnBucket);
            maxSpawnBucket = currentBucket;
        }
    }

    public int getBossCount() {
        return bossCount;
    }
}
