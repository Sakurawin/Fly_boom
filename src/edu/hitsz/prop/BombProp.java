package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 炸弹道具
 *
 * @author hitsz
 */
public class BombProp extends AbstractProp {

    public BombProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void effect(HeroAircraft heroAircraft) {
        System.out.println("BombSupply active!");
    }
}
