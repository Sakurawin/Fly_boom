package edu.hitsz.strategy;

import edu.hitsz.bullet.BaseBullet;

import java.util.List;

/**
 * 射击策略接口
 * 定义不同的射击方式
 * 
 * @author hitsz
 */
public interface ShootStrategy {
    
    /**
     * 射击方法
     * 
     * @param locationX 射击位置X坐标
     * @param locationY 射击位置Y坐标
     * @param speedX 基础速度X
     * @param speedY 基础速度Y
     * @param power 子弹威力
     * @param direction 射击方向 (向上发射：-1，向下发射：1)
     * @param isHero 是否为英雄机射击
     * @return 射击出的子弹List
     */
    List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, int power, int direction, boolean isHero);
}