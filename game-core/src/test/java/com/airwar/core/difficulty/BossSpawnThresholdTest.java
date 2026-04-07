package com.airwar.core.difficulty;

import com.airwar.core.engine.GameEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BossSpawnThresholdTest {

    @Test
    void bossSpawnsWhenScoreHitsConfiguredThreshold() {
        GameEngine engine = GameEngine.create(DifficultyLevel.NORMAL);
        int threshold = DifficultyConfig.of(DifficultyLevel.NORMAL).bossScoreThreshold();

        engine.setScore(threshold - 1);
        engine.tick(40);
        assertEquals(0, engine.getSnapshot().bossCount());

        engine.setScore(threshold);
        engine.tick(40);
        assertEquals(1, engine.getSnapshot().bossCount());
    }

    @Test
    void difficultyProfilesMatchDesignValues() {
        assertEquals(5, DifficultyConfig.of(DifficultyLevel.EASY).enemyMaxNumber());
        assertEquals(600, DifficultyConfig.of(DifficultyLevel.EASY).cycleDurationMs());
        assertEquals(200, DifficultyConfig.of(DifficultyLevel.EASY).bossScoreThreshold());

        assertEquals(6, DifficultyConfig.of(DifficultyLevel.NORMAL).enemyMaxNumber());
        assertEquals(520, DifficultyConfig.of(DifficultyLevel.NORMAL).cycleDurationMs());
        assertEquals(220, DifficultyConfig.of(DifficultyLevel.NORMAL).bossScoreThreshold());

        assertEquals(7, DifficultyConfig.of(DifficultyLevel.HARD).enemyMaxNumber());
        assertEquals(460, DifficultyConfig.of(DifficultyLevel.HARD).cycleDurationMs());
        assertEquals(250, DifficultyConfig.of(DifficultyLevel.HARD).bossScoreThreshold());
    }

    @Test
    void repeatedTicksAtSameThresholdDoNotSpawnDuplicateBosses() {
        GameEngine engine = GameEngine.create(DifficultyLevel.NORMAL);
        int threshold = DifficultyConfig.of(DifficultyLevel.NORMAL).bossScoreThreshold();

        engine.setScore(threshold);
        engine.tick(40);
        engine.tick(40);

        assertEquals(1, engine.getSnapshot().bossCount());
    }

    @Test
    void scoreRegressionThenReturnDoesNotSpawnDuplicateBoss() {
        GameEngine engine = GameEngine.create(DifficultyLevel.NORMAL);
        int threshold = DifficultyConfig.of(DifficultyLevel.NORMAL).bossScoreThreshold();

        engine.setScore(threshold);
        engine.tick(40);
        engine.setScore(threshold - 1);
        engine.tick(40);
        engine.setScore(threshold);
        engine.tick(40);

        assertEquals(1, engine.getSnapshot().bossCount());
    }

    @Test
    void scoreJumpAcrossThresholdSpawnsBossOnce() {
        GameEngine engine = GameEngine.create(DifficultyLevel.NORMAL);
        int threshold = DifficultyConfig.of(DifficultyLevel.NORMAL).bossScoreThreshold();

        engine.setScore(threshold - 1);
        engine.tick(40);
        engine.setScore((threshold * 2) - 1);
        engine.tick(40);

        assertEquals(1, engine.getSnapshot().bossCount());
    }
}
