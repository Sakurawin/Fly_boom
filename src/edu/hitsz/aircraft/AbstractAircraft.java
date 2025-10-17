package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.shoot.ShootStrategy;

import java.util.List;

/**
 * 所有种类飞机的抽象父类：
 * 敌机（BOSS, ELITE, MOB），英雄飞机
 *
 * @author hitsz
 */
public abstract class AbstractAircraft extends AbstractFlyingObject {
    /**
     * 生命值
     */
    protected int maxHp;
    protected int hp;

    /**
     * 射击策略
     */
    protected ShootStrategy shootStrategy;

    /**
     * 子弹伤害
     */
    protected int power;

    /**
     * 射击方向 (向上发射：-1，向下发射：1)
     */
    protected int direction;

    public AbstractAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY);
        this.hp = hp;
        this.maxHp = hp;
    }

    public void decreaseHp(int decrease) {
        hp -= decrease;
        if (hp <= 0) {
            hp = 0;
            vanish();
        }
    }

    public int getHp() {
        return hp;
    }

    /**
     * 设置射击策略
     * 
     * @param shootStrategy 射击策略
     */
    public void setShootStrategy(ShootStrategy shootStrategy) {
        this.shootStrategy = shootStrategy;
    }

    /**
     * 获取射击策略
     * 
     * @return 当前射击策略
     */
    public ShootStrategy getShootStrategy() {
        return shootStrategy;
    }

    /**
     * 飞机射击方法，使用策略模式
     * 
     * @return 子弹列表
     */
    public List<BaseBullet> shoot() {
        if (shootStrategy != null) {
            return shootStrategy.shoot(locationX, locationY, speedX, speedY, direction, power);
        }
        return null;
    }

}
