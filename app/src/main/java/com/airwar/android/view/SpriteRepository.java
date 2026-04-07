package com.airwar.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;

import com.airwar.android.R;

public final class SpriteRepository {
    private final Paint textPaint;
    private final Bitmap background;
    private final Bitmap hero;
    private final Bitmap mobEnemy;
    private final Bitmap bossEnemy;
    private final Bitmap heroBullet;
    private final Bitmap enemyBullet;

    public SpriteRepository(Context context) {
        background = decode(context, R.drawable.bg);
        hero = decode(context, R.drawable.hero);
        mobEnemy = decode(context, R.drawable.mob);
        bossEnemy = decode(context, R.drawable.boss);
        heroBullet = decode(context, R.drawable.bullet_hero);
        enemyBullet = decode(context, R.drawable.bullet_enemy);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36f);
    }

    public Bitmap background() {
        return background;
    }

    public Bitmap hero() {
        return hero;
    }

    public Bitmap mobEnemy() {
        return mobEnemy;
    }

    public Bitmap bossEnemy() {
        return bossEnemy;
    }

    public Bitmap heroBullet() {
        return heroBullet;
    }

    public Bitmap enemyBullet() {
        return enemyBullet;
    }

    public Paint textPaint() {
        return textPaint;
    }

    private static Bitmap decode(Context context, int resId) {
        return BitmapFactory.decodeResource(context.getResources(), resId);
    }
}
