package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

/**
 * 超级火力道具
 * 提供环射能力
 *
 * @author hitsz
 */
public class SuperFireProp extends AbstractProp {

    public SuperFireProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void effect(HeroAircraft heroAircraft) {
        // 设置英雄机为环射模式
        heroAircraft.setSuperFireMode();
        System.out.println("SuperFireSupply active! Hero can now shoot in circular pattern!");
    }
}