package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;

public class BombProp extends BaseProp {

  public BombProp(int locationX, int locationY, int speedX, int speedY) {
    super(locationX, locationY, speedX, speedY);
  }

  @Override
  public void active(HeroAircraft heroAircraft) {
    // TODO添加炸弹的逻辑，实验一只要求完成控制台输出
    System.out.println("BombSupply active!");
  }

}
