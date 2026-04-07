package com.airwar.core.strategy;

import com.airwar.core.model.bullet.BaseBullet;

import java.util.ArrayList;
import java.util.List;

public final class ScatterShootStrategy implements ShootStrategy {

    private final int shootNum;
    private final int scatterStepX;

    public ScatterShootStrategy(int shootNum, int scatterStepX) {
        this.shootNum = Math.max(1, shootNum);
        this.scatterStepX = scatterStepX;
    }

    @Override
    public List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, int power, int direction, boolean hero) {
        List<BaseBullet> bullets = new ArrayList<>(shootNum);
        int center = shootNum / 2;
        for (int i = 0; i < shootNum; i++) {
            int offset = i - center;
            int bulletSpeedX = speedX + offset * scatterStepX;
            int bulletSpeedY = speedY + direction * 5;
            bullets.add(new StrategyBullet(locationX, locationY, bulletSpeedX, bulletSpeedY, power));
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
