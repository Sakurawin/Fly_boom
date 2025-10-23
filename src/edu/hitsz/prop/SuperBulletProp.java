package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.CircularShootStrategy;

/**
 * 超级子弹道具
 * 使英雄机获得环射能力
 * 
 * @author hitsz
 */
public class SuperBulletProp extends BaseProp {

    public SuperBulletProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void active(HeroAircraft heroAircraft) {
        // 设置英雄机为环射策略
        heroAircraft.setShootStrategy(new CircularShootStrategy(20, 3));
        System.out.println("SuperBulletSupply active!");
    }
}