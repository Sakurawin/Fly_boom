package com.airwar.android.storage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScoreCsvSerializerTest {
    @Test
    public void roundTripPreservesBasicFields() {
        ScoreCsvSerializer serializer = new ScoreCsvSerializer();
        GameScore input = new GameScore(1200, "Alice", 88);

        String line = serializer.serialize(input);
        GameScore output = serializer.deserialize(line);

        assertEquals(1200, output.getScore());
        assertEquals("Alice", output.getName());
        assertEquals(88, output.getDurationSec());
    }

    @Test
    public void roundTripEscapesCommaAndBackslashInName() {
        ScoreCsvSerializer serializer = new ScoreCsvSerializer();
        GameScore input = new GameScore(42, "A\\,B,C\\D", 13);

        String line = serializer.serialize(input);
        GameScore output = serializer.deserialize(line);

        assertEquals(42, output.getScore());
        assertEquals("A\\,B,C\\D", output.getName());
        assertEquals(13, output.getDurationSec());
    }
}
