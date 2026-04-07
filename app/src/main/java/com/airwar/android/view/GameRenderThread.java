package com.airwar.android.view;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.airwar.core.engine.GameEngine;
import com.airwar.core.engine.GameStateSnapshot;

public final class GameRenderThread extends Thread {
    private static final long TICK_MS = 40L;

    private final SurfaceHolder holder;
    private final GameEngine engine;
    private final Object engineLock;
    private final SpriteRepository sprites;

    private volatile boolean running;

    public GameRenderThread(SurfaceHolder holder, GameEngine engine, Object engineLock, SpriteRepository sprites) {
        this.holder = holder;
        this.engine = engine;
        this.engineLock = engineLock;
        this.sprites = sprites;
    }

    public void requestStart() {
        running = true;
    }

    public void requestStop() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        while (running) {
            long frameStart = System.currentTimeMillis();

            GameStateSnapshot snapshot;
            synchronized (engineLock) {
                engine.tick(TICK_MS);
                snapshot = engine.getSnapshot();
            }

            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    drawFrame(canvas, snapshot);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }

            long spent = System.currentTimeMillis() - frameStart;
            long sleepMs = TICK_MS - spent;
            if (sleepMs > 0L) {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void drawFrame(Canvas canvas, GameStateSnapshot snapshot) {
        float width = canvas.getWidth();
        float height = canvas.getHeight();

        canvas.drawRect(0f, 0f, width, height, sprites.backgroundPaint());

        float heroX = snapshot.heroTargetX();
        float heroY = snapshot.heroTargetY();
        heroX = Math.max(36f, Math.min(width - 36f, heroX));
        heroY = Math.max(36f, Math.min(height - 36f, heroY));
        canvas.drawCircle(heroX, heroY, 36f, sprites.heroPaint());

        int markerCount = Math.max(1, snapshot.bossCount());
        float spacing = width / (markerCount + 1f);
        for (int i = 1; i <= markerCount; i++) {
            float x = spacing * i;
            canvas.drawCircle(x, 160f, 24f, sprites.enemyPaint());
        }

        canvas.drawText("Boss markers: " + snapshot.bossCount(), 24f, 56f, sprites.textPaint());
    }
}
