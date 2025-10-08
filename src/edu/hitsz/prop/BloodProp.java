package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 加血道具
 *
 * @author hitsz
 */
public class BloodProp extends AbstractProp {

    public BloodProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    // 拾取加血装备增加10点
    @Override
    public void effect(HeroAircraft heroAircraft) {
        heroAircraft.increaseHp(10);
    }
}
