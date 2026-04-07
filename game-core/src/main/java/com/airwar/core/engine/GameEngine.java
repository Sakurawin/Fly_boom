package com.airwar.core.engine;

import com.airwar.core.difficulty.DifficultyConfig;
import com.airwar.core.difficulty.DifficultyLevel;
import com.airwar.core.config.GameConstants;
import com.airwar.core.effect.EffectScheduler;
import com.airwar.core.effect.TimedEffect;
import com.airwar.core.model.aircraft.HeroAircraft;
import com.airwar.core.spawn.EnemySpawner;
import com.airwar.core.strategy.CircularShootStrategy;
import com.airwar.core.strategy.StraightShootStrategy;

import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public final class GameEngine {

    private static final Consumer<String> NOOP_HOOK = ignored -> { };

    private final EffectScheduler effectScheduler;
    private final EnemySpawner enemySpawner;
    private final HeroAircraft hero;
    private final Random random = new Random(20260407L);
    private final List<EntityState> heroBullets = new ArrayList<>();
    private final List<EntityState> enemyBullets = new ArrayList<>();
    private final List<EntityState> enemies = new ArrayList<>();

    private Consumer<String> debugHook = NOOP_HOOK;
    private int score;
    private int heroTargetX = GameConstants.LOGICAL_WIDTH / 2;
    private int heroTargetY = GameConstants.LOGICAL_HEIGHT - 140;
    private String heroShootMode = "STRAIGHT";
    private int heroHp = GameConstants.HERO_MAX_HP;
    private boolean gameOver;
    private boolean bossActive;

    private long heroShootCooldownMs;
    private long enemySpawnCooldownMs;
    private long enemyShootCooldownMs;
    private int heroShotEvents;
    private int hitEvents;
    private int explosionEvents;
    private int supplyEvents;
    private int gameOverEvents;

    private GameEngine(DifficultyLevel level) {
        DifficultyLevel safeLevel = Objects.requireNonNull(level, "level must not be null");
        DifficultyConfig config = DifficultyConfig.of(safeLevel);
        this.effectScheduler = new EffectScheduler();
        this.enemySpawner = new EnemySpawner(config);
        this.hero = new HeroAircraft(heroTargetX, heroTargetY, 0, 0, GameConstants.HERO_MAX_HP);
    }

    public static GameEngine create(DifficultyLevel level) {
        return new GameEngine(level);
    }

    public void setDebugHook(Consumer<String> hook) {
        this.debugHook = hook == null ? NOOP_HOOK : hook;
    }

    public void tick(long deltaMs) {
        if (deltaMs < 0) {
            throw new IllegalArgumentException("deltaMs must be non-negative");
        }

        phase("input");
        updateHeroAnchor();
        phase("update");
        updateBattle(deltaMs);
        phase("collision");
        handleCollisions();

        phase("spawn");
        int previousBossCount = enemySpawner.getBossCount();
        enemySpawner.update(score);
        if (enemySpawner.getBossCount() > previousBossCount) {
            spawnBoss();
        }

        phase("cleanup");
        cleanupEntities();

        effectScheduler.update(deltaMs);
    }

    public GameStateSnapshot getSnapshot() {
        return new GameStateSnapshot(
                enemySpawner.getBossCount(),
                heroTargetX,
                heroTargetY,
                score,
                heroHp,
                bossActive,
                heroShotEvents,
                hitEvents,
                explosionEvents,
                supplyEvents,
                gameOverEvents,
                mapToSnapshot(heroBullets),
                mapToSnapshot(enemyBullets),
                mapToSnapshot(enemies)
        );
    }

    public void setScore(int score) {
        if (score < 0) {
            throw new IllegalArgumentException("score must be non-negative");
        }
        this.score = score;
    }

    public void setHeroTarget(int x, int y) {
        this.heroTargetX = x;
        this.heroTargetY = y;
    }

    public void debugGrantSuperBullet(long durationMs) {
        if (durationMs <= 0L) {
            throw new IllegalArgumentException("durationMs must be positive");
        }
        effectScheduler.add(new TimedEffect(
                durationMs,
                () -> {
                    hero.setShootStrategy(new CircularShootStrategy(12, 7));
                    heroShootMode = "CIRCULAR";
                },
                () -> {
                    hero.setShootStrategy(new StraightShootStrategy(1));
                    heroShootMode = "STRAIGHT";
                }
        ));
    }

    public String getHeroShootMode() {
        return heroShootMode;
    }

    private void phase(String name) {
        debugHook.accept(name);
    }

    private void updateHeroAnchor() {
        hero.setLocation(heroTargetX, heroTargetY);
    }

    private void updateBattle(long deltaMs) {
        if (gameOver) {
            return;
        }

        heroShootCooldownMs += deltaMs;
        enemySpawnCooldownMs += deltaMs;
        enemyShootCooldownMs += deltaMs;

        if (heroShootCooldownMs >= 220L) {
            spawnHeroBullet();
            heroShootCooldownMs = 0L;
        }

        if (enemySpawnCooldownMs >= 650L) {
            spawnMobEnemy();
            enemySpawnCooldownMs = 0L;
        }

        if (enemyShootCooldownMs >= 800L) {
            spawnEnemyBullets();
            enemyShootCooldownMs = 0L;
        }

        for (EntityState bullet : heroBullets) {
            bullet.y += bullet.vy;
        }
        for (EntityState bullet : enemyBullets) {
            bullet.y += bullet.vy;
        }
        for (EntityState enemy : enemies) {
            enemy.y += enemy.vy;
        }
    }

    private void handleCollisions() {
        for (EntityState heroBullet : heroBullets) {
            if (!heroBullet.active) {
                continue;
            }
            for (EntityState enemy : enemies) {
                if (!enemy.active) {
                    continue;
                }
                if (collides(heroBullet, enemy, 26)) {
                    heroBullet.active = false;
                    enemy.hp -= 1;
                    hitEvents++;
                    if (enemy.hp <= 0) {
                        enemy.active = false;
                        score += enemy.type.equals("boss") ? 100 : 20;
                        explosionEvents++;
                        if (enemy.type.equals("boss")) {
                            bossActive = false;
                        }
                    }
                    break;
                }
            }
        }

        for (EntityState enemyBullet : enemyBullets) {
            if (!enemyBullet.active) {
                continue;
            }
            if (Math.abs(enemyBullet.x - heroTargetX) < 24 && Math.abs(enemyBullet.y - heroTargetY) < 24) {
                enemyBullet.active = false;
                heroHp = Math.max(0, heroHp - 5);
                hitEvents++;
                if (heroHp == 0 && !gameOver) {
                    gameOver = true;
                    gameOverEvents++;
                }
            }
        }
    }

    private void cleanupEntities() {
        heroBullets.removeIf(b -> !b.active || b.y < -20 || b.y > GameConstants.LOGICAL_HEIGHT + 20);
        enemyBullets.removeIf(b -> !b.active || b.y < -20 || b.y > GameConstants.LOGICAL_HEIGHT + 20);
        enemies.removeIf(e -> !e.active || e.y > GameConstants.LOGICAL_HEIGHT + 60);
        bossActive = enemies.stream().anyMatch(e -> e.active && e.type.equals("boss"));
    }

    private void spawnHeroBullet() {
        heroBullets.add(new EntityState(heroTargetX, heroTargetY - 26, 0, -14, 1, "hero_bullet"));
        heroShotEvents++;
    }

    private void spawnMobEnemy() {
        int x = 40 + random.nextInt(Math.max(1, GameConstants.LOGICAL_WIDTH - 80));
        enemies.add(new EntityState(x, -30, 0, 4, 2, "mob"));
    }

    private void spawnBoss() {
        enemies.add(new EntityState(GameConstants.LOGICAL_WIDTH / 2, 90, 0, 1, 16, "boss"));
        bossActive = true;
    }

    private void spawnEnemyBullets() {
        for (EntityState enemy : enemies) {
            if (!enemy.active) {
                continue;
            }
            enemyBullets.add(new EntityState(enemy.x, enemy.y + 20, 0, 8, 1, "enemy_bullet"));
        }
    }

    private static boolean collides(EntityState a, EntityState b, int threshold) {
        return Math.abs(a.x - b.x) <= threshold && Math.abs(a.y - b.y) <= threshold;
    }

    private static List<GameStateSnapshot.EntitySnapshot> mapToSnapshot(List<EntityState> states) {
        List<GameStateSnapshot.EntitySnapshot> list = new ArrayList<>(states.size());
        for (EntityState state : states) {
            if (state.active) {
                list.add(new GameStateSnapshot.EntitySnapshot(state.x, state.y, state.type));
            }
        }
        return list;
    }

    private static final class EntityState {
        private int x;
        private int y;
        private final int vx;
        private final int vy;
        private int hp;
        private final String type;
        private boolean active = true;

        private EntityState(int x, int y, int vx, int vy, int hp, String type) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.hp = hp;
            this.type = type;
        }
    }
}
