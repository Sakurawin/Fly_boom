package edu.hitsz.prop;

import java.util.Random;

import edu.hitsz.aircraft.HeroAircraft;

public class BloodProp extends BaseProp {
  // 使用随机[10,30]的奖励添加血量
  int hpReward;

  public BloodProp(int locationX, int locationY, int speedX, int speedY) {
    super(locationX, locationY, speedX, speedY);
    Random random = new Random();
    hpReward = (random.nextInt(4) + 1) * 10; // 随机生成生命值奖励
  }

  // 重写active方法
  @Override
  public void active(HeroAircraft heroAircraft) {
    heroAircraft.increaseHp(hpReward);
  }

}
