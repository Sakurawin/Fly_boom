package com.airwar.core.strategy;

import com.airwar.core.model.bullet.BaseBullet;

import java.util.ArrayList;
import java.util.List;

public final class StraightShootStrategy implements ShootStrategy {

    private final int shootNum;

    public StraightShootStrategy(int shootNum) {
        this.shootNum = Math.max(1, shootNum);
    }

    @Override
    public List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, int power, int direction, boolean hero) {
        List<BaseBullet> bullets = new ArrayList<>(shootNum);
        int startX = locationX - (shootNum - 1) / 2;
        for (int i = 0; i < shootNum; i++) {
            bullets.add(new StrategyBullet(startX + i, locationY, speedX, speedY + direction * 5, power));
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
