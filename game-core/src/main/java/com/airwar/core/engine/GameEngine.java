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

import java.util.Objects;
import java.util.function.Consumer;

public final class GameEngine {

    private static final Consumer<String> NOOP_HOOK = ignored -> { };

    private final EffectScheduler effectScheduler;
    private final EnemySpawner enemySpawner;
    private final HeroAircraft hero;

    private Consumer<String> debugHook = NOOP_HOOK;
    private int score;
    private int heroTargetX = GameConstants.LOGICAL_WIDTH / 2;
    private int heroTargetY = GameConstants.LOGICAL_HEIGHT - 140;
    private String heroShootMode = "STRAIGHT";

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
        phase("update");
        phase("collision");

        phase("spawn");
        enemySpawner.update(score);

        phase("cleanup");

        effectScheduler.update(deltaMs);
    }

    public GameStateSnapshot getSnapshot() {
        return new GameStateSnapshot(enemySpawner.getBossCount(), heroTargetX, heroTargetY);
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
}
