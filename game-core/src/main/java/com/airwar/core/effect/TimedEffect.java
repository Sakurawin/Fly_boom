package com.airwar.core.effect;

public final class TimedEffect {

    private final long durationMs;
    private final Runnable onStart;
    private final Runnable onExpire;
    private long elapsed;
    private boolean started;

    public TimedEffect(long durationMs, Runnable onStart, Runnable onExpire) {
        this.durationMs = durationMs;
        this.onStart = onStart;
        this.onExpire = onExpire;
    }

    boolean update(long deltaMs) {
        if (!started) {
            onStart.run();
            started = true;
        }
        elapsed += deltaMs;
        if (elapsed >= durationMs) {
            onExpire.run();
            return true;
        }
        return false;
    }
}
