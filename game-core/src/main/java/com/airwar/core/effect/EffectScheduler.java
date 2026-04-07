package com.airwar.core.effect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class EffectScheduler {

    private final List<TimedEffect> activeEffects = new ArrayList<>();

    public void add(TimedEffect effect) {
        activeEffects.add(effect);
    }

    public void update(long deltaMs) {
        Iterator<TimedEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            TimedEffect effect = iterator.next();
            if (effect.update(deltaMs)) {
                iterator.remove();
            }
        }
    }

    public boolean hasActiveEffects() {
        return !activeEffects.isEmpty();
    }
}
