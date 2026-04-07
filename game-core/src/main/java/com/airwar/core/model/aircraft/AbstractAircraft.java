package com.airwar.core.model.aircraft;

import com.airwar.core.model.FlyingObject;
import com.airwar.core.model.bullet.BaseBullet;

import java.util.List;

public abstract class AbstractAircraft extends FlyingObject {

    protected int maxHp;
    protected int hp;

    protected AbstractAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY);
        this.hp = hp;
        this.maxHp = hp;
        this.width = 50;
        this.height = 50;
    }

    public void decreaseHp(int decrease) {
        hp -= decrease;
        if (hp <= 0) {
            hp = 0;
            vanish();
        }
    }

    public int getHp() {
        return hp;
    }

    public abstract List<BaseBullet> shoot();
}
