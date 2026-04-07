package com.airwar.android.storage;

import java.util.Objects;

public class GameScore {
    private final int score;
    private final String name;
    private final int durationSec;

    public GameScore(int score, String name, int durationSec) {
        if (score < 0) {
            throw new IllegalArgumentException("score must be non-negative");
        }
        if (durationSec < 0) {
            throw new IllegalArgumentException("durationSec must be non-negative");
        }
        this.score = score;
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.durationSec = durationSec;
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public int getDurationSec() {
        return durationSec;
    }
}
