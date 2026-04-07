package com.airwar.android.storage;

import java.util.Objects;

public class GameScore {
    private final int score;
    private final String name;
    private final int durationSec;
    private final String difficulty;

    public GameScore(int score, String name, int durationSec, String difficulty) {
        if (score < 0) {
            throw new IllegalArgumentException("score must be non-negative");
        }
        if (durationSec < 0) {
            throw new IllegalArgumentException("durationSec must be non-negative");
        }
        this.score = score;
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.durationSec = durationSec;
        String safeDifficulty = difficulty == null ? "normal" : difficulty.trim().toLowerCase();
        this.difficulty = safeDifficulty.isEmpty() ? "normal" : safeDifficulty;
    }

    public GameScore(int score, String name, int durationSec) {
        this(score, name, durationSec, "normal");
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

    public String getDifficulty() {
        return difficulty;
    }
}
