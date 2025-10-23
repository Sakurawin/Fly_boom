package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.strategy.ScatterShootStrategy;

public class BulletProp extends BaseProp {

  public BulletProp(int locationX, int locationY, int speedX, int speedY) {
    super(locationX, locationY, speedX, speedY);
  }

  // 重写active方法
  @Override
  public void active(HeroAircraft heroAircraft) {
    // 设置英雄机为散射策略，发射3颗子弹，散射角度30度
    heroAircraft.setShootStrategy(new ScatterShootStrategy(3, 30));
    System.out.println("FireSupply active!");
  }
}
