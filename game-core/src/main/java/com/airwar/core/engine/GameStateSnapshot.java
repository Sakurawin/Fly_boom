package com.airwar.core.engine;

import java.util.List;

public record GameStateSnapshot(
        int bossCount,
        int heroTargetX,
        int heroTargetY,
        int score,
        int heroHp,
        boolean bossActive,
        int heroShotEvents,
        int hitEvents,
        int explosionEvents,
        int supplyEvents,
        int gameOverEvents,
        List<EntitySnapshot> heroBullets,
        List<EntitySnapshot> enemyBullets,
        List<EntitySnapshot> enemies
) {

    public GameStateSnapshot {
        heroBullets = List.copyOf(heroBullets);
        enemyBullets = List.copyOf(enemyBullets);
        enemies = List.copyOf(enemies);
    }

    public record EntitySnapshot(int x, int y, String type) {
    }
}
