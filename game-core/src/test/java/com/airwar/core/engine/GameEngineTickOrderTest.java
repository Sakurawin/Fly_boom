package com.airwar.core.engine;

import com.airwar.core.difficulty.DifficultyLevel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameEngineTickOrderTest {

    @Test
    void tickFollowsFixedOrder() {
        List<String> events = new ArrayList<>();
        GameEngine engine = GameEngine.create(DifficultyLevel.EASY);
        engine.setDebugHook(events::add);

        engine.tick(40);

        assertEquals(List.of("input", "update", "collision", "spawn", "cleanup"), events);
    }
}
