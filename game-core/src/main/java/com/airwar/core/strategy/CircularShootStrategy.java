package com.airwar.core.strategy;

import com.airwar.core.model.bullet.BaseBullet;

import java.util.ArrayList;
import java.util.List;

public final class CircularShootStrategy implements ShootStrategy {

    private final int shootNum;
    private final int bulletSpeed;

    public CircularShootStrategy(int shootNum, int bulletSpeed) {
        this.shootNum = Math.max(1, shootNum);
        this.bulletSpeed = bulletSpeed;
    }

    @Override
    public List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, int power, int direction, boolean hero) {
        List<BaseBullet> bullets = new ArrayList<>(shootNum);
        for (int i = 0; i < shootNum; i++) {
            double angle = Math.toRadians(i * 360.0 / shootNum);
            int vx = speedX + (int) Math.round(bulletSpeed * Math.cos(angle));
            int vy = speedY + (int) Math.round(bulletSpeed * Math.sin(angle));
            bullets.add(new StrategyBullet(locationX, locationY, vx, vy, power));
        }
        return bullets;
    }

    private static final class StrategyBullet extends BaseBullet {
        private StrategyBullet(int locationX, int locationY, int speedX, int speedY, int power) {
            super(locationX, locationY, speedX, speedY, power);
            this.width = 10;
            this.height = 20;
        }
    }
}
