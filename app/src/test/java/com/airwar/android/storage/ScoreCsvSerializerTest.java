package com.airwar.android.storage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScoreCsvSerializerTest {
    @Test
    public void roundTripPreservesBasicFields() {
        ScoreCsvSerializer serializer = new ScoreCsvSerializer();
        GameScore input = new GameScore(1200, "Alice", 88, "hard", "pilot_03");

        String line = serializer.serialize(input);
        GameScore output = serializer.deserialize(line);

        assertEquals(1200, output.getScore());
        assertEquals("Alice", output.getName());
        assertEquals(88, output.getDurationSec());
        assertEquals("hard", output.getDifficulty());
        assertEquals("pilot_03", output.getAvatarId());
    }

    @Test
    public void roundTripEscapesCommaAndBackslashInName() {
        ScoreCsvSerializer serializer = new ScoreCsvSerializer();
        GameScore input = new GameScore(42, "A\\,B,C\\D", 13, "easy", "pilot_02");

        String line = serializer.serialize(input);
        GameScore output = serializer.deserialize(line);

        assertEquals(42, output.getScore());
        assertEquals("A\\,B,C\\D", output.getName());
        assertEquals(13, output.getDurationSec());
        assertEquals("easy", output.getDifficulty());
        assertEquals("pilot_02", output.getAvatarId());
    }

    @Test
    public void deserializeLegacyThreeFieldLineDefaultsToNormalDifficultyAndAvatar() {
        ScoreCsvSerializer serializer = new ScoreCsvSerializer();
        GameScore output = serializer.deserialize("100,Tom,12");

        assertEquals(100, output.getScore());
        assertEquals("Tom", output.getName());
        assertEquals(12, output.getDurationSec());
        assertEquals("normal", output.getDifficulty());
        assertEquals("default", output.getAvatarId());
    }

    @Test
    public void deserializeLegacyFourFieldLineDefaultsAvatarId() {
        ScoreCsvSerializer serializer = new ScoreCsvSerializer();
        GameScore output = serializer.deserialize("100,Tom,12,normal");

        assertEquals(100, output.getScore());
        assertEquals("Tom", output.getName());
        assertEquals(12, output.getDurationSec());
        assertEquals("normal", output.getDifficulty());
        assertEquals("default", output.getAvatarId());
    }
}
