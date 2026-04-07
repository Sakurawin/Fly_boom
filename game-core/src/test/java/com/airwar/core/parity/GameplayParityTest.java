package com.airwar.core.parity;

import com.airwar.core.difficulty.DifficultyLevel;
import com.airwar.core.engine.GameEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameplayParityTest {

    @Test
    void superBulletEffectRollsBackAfterDuration() {
        GameEngine engine = GameEngine.create(DifficultyLevel.NORMAL);

        engine.debugGrantSuperBullet(3000);
        engine.tick(1);
        assertEquals("CIRCULAR", engine.getHeroShootMode());

        engine.tick(2999);
        assertEquals("STRAIGHT", engine.getHeroShootMode());
    }
}
