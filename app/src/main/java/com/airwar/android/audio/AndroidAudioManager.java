package com.airwar.android.audio;

import android.app.Activity;

public class AndroidAudioManager {
    public interface Backend {
        void playBgmGame();

        void playBgmBoss();

        void stopAllBgm();

        boolean isBgmPlaying();
    }

    private final Backend backend;
    private boolean enabled = true;

    public AndroidAudioManager(Activity activity) {
        this(createBackend(activity));
    }

    public AndroidAudioManager(Backend backend) {
        this.backend = backend == null ? new NoOpBackend() : backend;
    }

    public void playBgmGame() {
        if (!enabled) {
            return;
        }
        backend.playBgmGame();
    }

    public void playBgmBoss() {
        if (!enabled) {
            return;
        }
        backend.playBgmBoss();
    }

    public void stopAllBgm() {
        backend.stopAllBgm();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            backend.stopAllBgm();
        }
    }

    public boolean isBgmPlaying() {
        return backend.isBgmPlaying();
    }

    private static Backend createBackend(Activity activity) {
        return new NoOpBackend();
    }

    private static final class NoOpBackend implements Backend {
        @Override
        public void playBgmGame() {
        }

        @Override
        public void playBgmBoss() {
        }

        @Override
        public void stopAllBgm() {
        }

        @Override
        public boolean isBgmPlaying() {
            return false;
        }
    }
}
