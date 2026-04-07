package com.airwar.core.engine;

import com.airwar.core.difficulty.DifficultyLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEngineBattleSnapshotTest {

    @Test
    void snapshotContainsBattleFieldsWithSafeDefaults() {
        GameEngine engine = GameEngine.create(DifficultyLevel.NORMAL);

        GameStateSnapshot snapshot = engine.getSnapshot();

        assertEquals(0, snapshot.score());
        assertEquals(100, snapshot.heroHp());
        assertFalse(snapshot.bossActive());
        assertTrue(snapshot.heroBullets().isEmpty());
        assertTrue(snapshot.enemyBullets().isEmpty());
        assertTrue(snapshot.enemies().isEmpty());
    }

    @Test
    void tickingProducesBulletsAndEnemies() {
        GameEngine engine = GameEngine.create(DifficultyLevel.NORMAL);

        for (int i = 0; i < 30; i++) {
            engine.tick(40);
        }

        GameStateSnapshot snapshot = engine.getSnapshot();
        assertTrue(snapshot.heroBullets().size() > 0);
        assertTrue(snapshot.enemies().size() > 0);
        assertTrue(snapshot.heroShotEvents() > 0);
    }

    @Test
    void bossActivationFollowsThreshold() {
        GameEngine engine = GameEngine.create(DifficultyLevel.NORMAL);
        engine.setScore(220);
        engine.tick(40);

        GameStateSnapshot snapshot = engine.getSnapshot();
        assertTrue(snapshot.bossActive());
        assertEquals(1, snapshot.bossCount());
    }
}
