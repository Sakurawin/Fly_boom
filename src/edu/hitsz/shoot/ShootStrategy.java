package edu.hitsz.shoot;

import edu.hitsz.bullet.BaseBullet;

import java.util.List;

/**
 * 射击策略接口
 * 策略模式：定义不同的射击行为
 * 
 * @author hitsz
 */
public interface ShootStrategy {

    /**
     * 执行射击行为
     * 
     * @param locationX 射击者的X坐标
     * @param locationY 射击者的Y坐标
     * @param speedX    射击者的X速度（用于计算子弹初始速度）
     * @param speedY    射击者的Y速度（用于计算子弹初始速度）
     * @param direction 射击方向（1为向下，-1为向上）
     * @param power     子弹伤害
     * @return 发射的子弹列表
     */
    List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, int direction, int power);
}