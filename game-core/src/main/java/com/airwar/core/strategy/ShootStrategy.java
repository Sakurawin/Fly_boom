package com.airwar.core.strategy;

import com.airwar.core.model.bullet.BaseBullet;

import java.util.List;

public interface ShootStrategy {

    List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, int power, int direction, boolean hero);
}
