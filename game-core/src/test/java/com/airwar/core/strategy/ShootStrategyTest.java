package com.airwar.core.strategy;

import com.airwar.core.model.bullet.BaseBullet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShootStrategyTest {

    @Test
    void circularStrategyCreatesExpectedBulletCount() {
        ShootStrategy strategy = new CircularShootStrategy(12, 7);
        List<BaseBullet> bullets = strategy.shoot(100, 100, 0, 0, 30, -1, true);
        assertEquals(12, bullets.size());
    }

    @Test
    void straightStrategyUsesConfiguredShootNum() {
        ShootStrategy strategy = new StraightShootStrategy(3);
        List<BaseBullet> bullets = strategy.shoot(100, 100, 0, 0, 25, -1, true);
        assertEquals(3, bullets.size());
        assertEquals(25, bullets.get(0).getPower());
    }

    @Test
    void scatterStrategyUsesConfiguredShootNum() {
        ShootStrategy strategy = new ScatterShootStrategy(5, 2);
        List<BaseBullet> bullets = strategy.shoot(100, 100, 0, 0, 15, 1, false);
        assertEquals(5, bullets.size());
    }
}
