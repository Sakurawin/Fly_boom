package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 火力道具
 *
 * @author hitsz
 */
public class FireProp extends AbstractProp {

    public FireProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void effect(HeroAircraft heroAircraft) {
        // 设置英雄机为散射模式
        heroAircraft.setScatterMode();
        System.out.println("FireSupply active! Hero can now shoot in scatter pattern!");
    }
}
