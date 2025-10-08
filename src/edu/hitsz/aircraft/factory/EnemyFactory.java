package edu.hitsz.aircraft.factory;

import edu.hitsz.aircraft.AbstractAircraft;

/**
 * 敌机工厂抽象类
 * 工厂方法模式的抽象工厂
 * 
 * @author hitsz
 */
public abstract class EnemyFactory {

    /**
     * 创建敌机的抽象方法
     * 
     * @param locationX X的坐标
     * @param locationY Y的坐标
     * @param speedX    X方向速度
     * @param speedY    Y方向速度
     * @param hp        生命值
     * @return 敌机实例
     */
    public abstract AbstractAircraft createEnemy(int locationX, int locationY, int speedX, int speedY, int hp);
}