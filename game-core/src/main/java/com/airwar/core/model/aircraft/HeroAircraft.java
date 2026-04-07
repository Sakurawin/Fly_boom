package com.airwar.core.model.aircraft;

import com.airwar.core.model.bullet.BaseBullet;
import com.airwar.core.strategy.ShootStrategy;
import com.airwar.core.strategy.StraightShootStrategy;

import java.util.List;

public final class HeroAircraft extends AbstractAircraft {

    private static final int DEFAULT_SHOOT_NUM = 1;
    private static final int DEFAULT_POWER = 30;
    private static final int DEFAULT_DIRECTION = -1;

    private ShootStrategy shootStrategy;

    public HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootStrategy = new StraightShootStrategy(DEFAULT_SHOOT_NUM);
    }

    @Override
    public void forward() {
    }

    @Override
    public List<BaseBullet> shoot() {
        return shootStrategy.shoot(locationX, locationY, speedX, speedY, DEFAULT_POWER, DEFAULT_DIRECTION, true);
    }

    public void setShootStrategy(ShootStrategy shootStrategy) {
        if (shootStrategy == null) {
            throw new IllegalArgumentException("shootStrategy must not be null");
        }
        this.shootStrategy = shootStrategy;
    }
}
