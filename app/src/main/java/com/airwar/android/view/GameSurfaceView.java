package com.airwar.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.airwar.core.difficulty.DifficultyLevel;
import com.airwar.core.engine.GameEngine;

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private final GameEngine engine;
    private final SpriteRepository spriteRepository;
    private final Object engineLock = new Object();

    private GameRenderThread renderThread;

    public GameSurfaceView(Context context) {
        this(context, null);
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        engine = GameEngine.create(DifficultyLevel.NORMAL);
        spriteRepository = new SpriteRepository();
        getHolder().addCallback(this);
        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            synchronized (engineLock) {
                engine.setHeroTarget(Math.round(event.getX()), Math.round(event.getY()));
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        stopRenderThreadIfNeeded();
        renderThread = new GameRenderThread(holder, engine, engineLock, spriteRepository);
        renderThread.requestStart();
        renderThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopRenderThreadIfNeeded();
    }

    private void stopRenderThreadIfNeeded() {
        if (renderThread == null) {
            return;
        }

        GameRenderThread thread = renderThread;
        thread.requestStop();
        boolean interrupted = false;
        int retries = 0;
        while (thread.isAlive() && retries < 20) {
            try {
                thread.join(50L);
            } catch (InterruptedException ignored) {
                interrupted = true;
                retries++;
                continue;
            }
            retries++;
        }

        if (thread.isAlive()) {
            renderThread = null;
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            return;
        }

        renderThread = null;
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
