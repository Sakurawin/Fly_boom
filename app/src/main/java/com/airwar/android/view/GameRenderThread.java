package com.airwar.android.view;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;

import com.airwar.core.config.GameConstants;
import com.airwar.core.engine.GameEngine;
import com.airwar.core.engine.GameStateSnapshot;

import java.util.List;

public final class GameRenderThread extends Thread {
    private static final long TICK_MS = 40L;

    private final SurfaceHolder holder;
    private final GameEngine engine;
    private final Object engineLock;
    private final SpriteRepository sprites;
    private final FrameListener frameListener;

    private volatile boolean running;

    public GameRenderThread(
            SurfaceHolder holder,
            GameEngine engine,
            Object engineLock,
            SpriteRepository sprites,
            FrameListener frameListener
    ) {
        this.holder = holder;
        this.engine = engine;
        this.engineLock = engineLock;
        this.sprites = sprites;
        this.frameListener = frameListener;
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

            if (frameListener != null) {
                frameListener.onFrame(snapshot);
            }
        }
    }

    private void drawFrame(Canvas canvas, GameStateSnapshot snapshot) {
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        float scaleX = width / GameConstants.LOGICAL_WIDTH;
        float scaleY = height / GameConstants.LOGICAL_HEIGHT;

        if (sprites.background() != null) {
            canvas.drawBitmap(sprites.background(), null, new Rect(0, 0, (int) width, (int) height), null);
        } else {
            canvas.drawRGB(8, 16, 32);
        }

        float heroX = snapshot.heroTargetX() * scaleX;
        float heroY = snapshot.heroTargetY() * scaleY;
        drawBitmapCentered(canvas, sprites.hero(), heroX, heroY, (int) (52 * scaleX), (int) (52 * scaleY));

        drawEntities(canvas, snapshot.enemies(), sprites.mobEnemy(), sprites.bossEnemy(), 48, 48, scaleX, scaleY);
        drawEntities(canvas, snapshot.heroBullets(), sprites.heroBullet(), null, 14, 26, scaleX, scaleY);
        drawEntities(canvas, snapshot.enemyBullets(), sprites.enemyBullet(), null, 14, 26, scaleX, scaleY);

        if (snapshot.bossActive()) {
            canvas.drawText("Boss incoming", 24f, 96f, sprites.textPaint());
        }
    }

    private static void drawEntities(
            Canvas canvas,
            List<GameStateSnapshot.EntitySnapshot> entities,
            android.graphics.Bitmap normal,
            android.graphics.Bitmap boss,
            int width,
            int height,
            float scaleX,
            float scaleY
    ) {
        for (GameStateSnapshot.EntitySnapshot entity : entities) {
            android.graphics.Bitmap bitmap = "boss".equals(entity.type()) && boss != null ? boss : normal;
            float x = entity.x() * scaleX;
            float y = entity.y() * scaleY;
            drawBitmapCentered(canvas, bitmap, x, y, (int) (width * scaleX), (int) (height * scaleY));
        }
    }

    private static void drawBitmapCentered(Canvas canvas, android.graphics.Bitmap bitmap, float cx, float cy, int w, int h) {
        float left = cx - w / 2f;
        float top = cy - h / 2f;
        float right = cx + w / 2f;
        float bottom = cy + h / 2f;
        Rect dst = new Rect((int) left, (int) top, (int) right, (int) bottom);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, null, dst, null);
            return;
        }
        canvas.drawRect(dst, new android.graphics.Paint());
    }

    public interface FrameListener {
        void onFrame(GameStateSnapshot snapshot);
    }
}
