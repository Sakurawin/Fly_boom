package com.airwar.android.ui;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class RandomPilotNameGeneratorTest {

    @Test
    public void generateUsesExpectedReadableFormat() {
        RandomPilotNameGenerator generator = new RandomPilotNameGenerator(() -> 1712475600123L);

        String name = generator.generate();

        assertTrue(name.startsWith("飞行员-"));
        assertTrue(name.matches("飞行员-[A-Z0-9]{3}-[0-9]{8}(-[0-9]+)?"));
    }

    @Test
    public void generateAddsCounterWhenTimestampSame() {
        RandomPilotNameGenerator generator = new RandomPilotNameGenerator(() -> 1712475600123L);

        String first = generator.generate();
        String second = generator.generate();

        assertNotEquals(first, second);
        assertTrue(second.matches(".*-1$"));
    }
}
