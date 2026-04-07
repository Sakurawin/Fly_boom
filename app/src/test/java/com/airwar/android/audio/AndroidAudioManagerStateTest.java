package com.airwar.android.audio;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AndroidAudioManagerStateTest {

    @Test
    public void playMethodsDelegateWhenEnabled() {
        FakeBackend backend = new FakeBackend();
        AndroidAudioManager manager = new AndroidAudioManager(backend);

        manager.playBgmGame();
        manager.playBgmBoss();

        assertEquals(1, backend.playGameCalls);
        assertEquals(1, backend.playBossCalls);
    }

    @Test
    public void disablingStopsAndBlocksPlayback() {
        FakeBackend backend = new FakeBackend();
        AndroidAudioManager manager = new AndroidAudioManager(backend);

        manager.playBgmGame();
        manager.setEnabled(false);
        manager.playBgmBoss();

        assertEquals(1, backend.playGameCalls);
        assertEquals(0, backend.playBossCalls);
        assertEquals(1, backend.stopAllCalls);
    }

    @Test
    public void reEnableAllowsPlaybackAgain() {
        FakeBackend backend = new FakeBackend();
        AndroidAudioManager manager = new AndroidAudioManager(backend);

        manager.setEnabled(false);
        manager.setEnabled(true);
        manager.playBgmBoss();

        assertEquals(1, backend.playBossCalls);
    }

    @Test
    public void isBgmPlayingTracksEnabledState() {
        FakeBackend backend = new FakeBackend();
        AndroidAudioManager manager = new AndroidAudioManager(backend);

        backend.playing = true;
        assertTrue(manager.isBgmPlaying());

        manager.setEnabled(false);
        assertFalse(manager.isBgmPlaying());
    }

    private static final class FakeBackend implements AndroidAudioManager.Backend {
        private int playGameCalls;
        private int playBossCalls;
        private int stopAllCalls;
        private boolean playing;

        @Override
        public void playBgmGame() {
            playGameCalls++;
            playing = true;
        }

        @Override
        public void playBgmBoss() {
            playBossCalls++;
            playing = true;
        }

        @Override
        public void stopAllBgm() {
            stopAllCalls++;
            playing = false;
        }

        @Override
        public boolean isBgmPlaying() {
            return playing;
        }
    }
}
