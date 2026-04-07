package com.airwar.core.model.prop;

import com.airwar.core.config.GameConstants;
import com.airwar.core.model.FlyingObject;
import com.airwar.core.model.aircraft.HeroAircraft;

public abstract class BaseProp extends FlyingObject {

    protected BaseProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
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

    public abstract void active(HeroAircraft heroAircraft);
}
