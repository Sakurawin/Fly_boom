package com.airwar.core.model.bullet;

import com.airwar.core.config.GameConstants;
import com.airwar.core.model.FlyingObject;

public abstract class BaseBullet extends FlyingObject {

    private final int power;

    protected BaseBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY);
        this.power = power;
    }

    @Override
    public void forward() {
        super.forward();
        if (locationX <= 0 || locationX >= GameConstants.WINDOW_WIDTH) {
            vanish();
        }
        if (speedY > 0 && locationY >= GameConstants.WINDOW_HEIGHT) {
            vanish();
        } else if (locationY <= 0) {
            vanish();
        }
    }

    public int getPower() {
        return power;
    }
}
