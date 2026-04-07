package com.airwar.android.view;

import android.graphics.Color;
import android.graphics.Paint;

public final class SpriteRepository {
    private final Paint backgroundPaint;
    private final Paint heroPaint;
    private final Paint enemyPaint;
    private final Paint textPaint;

    public SpriteRepository() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.rgb(8, 16, 32));

        heroPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        heroPaint.setColor(Color.rgb(60, 200, 255));

        enemyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        enemyPaint.setColor(Color.rgb(255, 96, 96));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36f);
    }

    public Paint backgroundPaint() {
        return backgroundPaint;
    }

    public Paint heroPaint() {
        return heroPaint;
    }

    public Paint enemyPaint() {
        return enemyPaint;
    }

    public Paint textPaint() {
        return textPaint;
    }
}
