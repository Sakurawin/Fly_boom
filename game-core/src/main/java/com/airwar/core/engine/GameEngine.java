package com.airwar.core.engine;

import com.airwar.core.difficulty.DifficultyConfig;
import com.airwar.core.difficulty.DifficultyLevel;
import com.airwar.core.effect.EffectScheduler;
import com.airwar.core.spawn.EnemySpawner;

import java.util.Objects;
import java.util.function.Consumer;

public final class GameEngine {

    private static final Consumer<String> NOOP_HOOK = ignored -> { };

    private final EffectScheduler effectScheduler;
    private final EnemySpawner enemySpawner;

    private Consumer<String> debugHook = NOOP_HOOK;
    private int score;

    private GameEngine(DifficultyLevel level) {
        DifficultyLevel safeLevel = Objects.requireNonNull(level, "level must not be null");
        DifficultyConfig config = DifficultyConfig.of(safeLevel);
        this.effectScheduler = new EffectScheduler();
        this.enemySpawner = new EnemySpawner(config);
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
        return new GameStateSnapshot(enemySpawner.getBossCount());
    }

    public void setScore(int score) {
        if (score < 0) {
            throw new IllegalArgumentException("score must be non-negative");
        }
        this.score = score;
    }

    private void phase(String name) {
        debugHook.accept(name);
    }
}
