package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

public class BulletProp extends BaseProp {

  public BulletProp(int locationX, int locationY, int speedX, int speedY) {
    super(locationX, locationY, speedX, speedY);
  }

  // 重写active方法
  @Override
  public void active(HeroAircraft heroAircraft) {
    // TODO添加子弹生效逻辑
    System.out.println("FireSupply active!");
  }
}
