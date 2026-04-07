package com.airwar.core.effect;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffectSchedulerTest {

    @Test
    void effectExpiresAtExpectedTick() {
        EffectScheduler scheduler = new EffectScheduler();
        AtomicBoolean expired = new AtomicBoolean(false);

        scheduler.add(new TimedEffect(1000, () -> { }, () -> expired.set(true)));
        scheduler.update(1000);

        assertTrue(expired.get());
    }

    @Test
    void startRunsOnceAndExpireRunsAfterAccumulatedDelta() {
        EffectScheduler scheduler = new EffectScheduler();
        AtomicInteger starts = new AtomicInteger();
        AtomicInteger expires = new AtomicInteger();

        scheduler.add(new TimedEffect(1000, starts::incrementAndGet, expires::incrementAndGet));
        scheduler.update(400);
        scheduler.update(300);

        assertEquals(1, starts.get());
        assertEquals(0, expires.get());

        scheduler.update(300);

        assertEquals(1, starts.get());
        assertEquals(1, expires.get());
    }

    @Test
    void schedulerRemovesExpiredEffect() {
        EffectScheduler scheduler = new EffectScheduler();
        AtomicBoolean expired = new AtomicBoolean(false);

        scheduler.add(new TimedEffect(10, () -> { }, () -> expired.set(true)));
        scheduler.update(10);

        assertTrue(expired.get());
        assertFalse(scheduler.hasActiveEffects());
    }
}
