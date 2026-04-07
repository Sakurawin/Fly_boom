package com.airwar.android.ui;

import java.util.Locale;
import java.util.Random;

public class RandomPilotNameGenerator {
    public interface TimeProvider {
        long nowMillis();
    }

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final TimeProvider timeProvider;
    private final Random random;
    private long lastTimestamp = -1L;
    private int counter = 0;

    public RandomPilotNameGenerator(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        this.random = new Random();
    }

    public RandomPilotNameGenerator() {
        this(System::currentTimeMillis);
    }

    public synchronized String generate() {
        long now = timeProvider.nowMillis();
        if (now == lastTimestamp) {
            counter++;
        } else {
            lastTimestamp = now;
            counter = 0;
        }
        String base = "飞行员-" + randomToken(3) + "-" + String.format(Locale.ROOT, "%08d", Math.floorMod(now, 100_000_000L));
        return counter == 0 ? base : base + "-" + counter;
    }

    private String randomToken(int length) {
        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALPHABET.length());
            token.append(ALPHABET.charAt(index));
        }
        return token.toString();
    }
}
