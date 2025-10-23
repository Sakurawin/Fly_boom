package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.StraightShootStrategy;

import java.util.LinkedList;
import java.util.List;

/**
 * 英雄飞机，游戏玩家操控
 * 
 * @author hitsz
 */
public class HeroAircraft extends AbstractAircraft {

    /** 攻击方式 */

    /**
     * 子弹一次发射数量
     */
    private int shootNum = 1;
    /**
     * 子弹伤害
     */
    private int power = 30;

    /**
     * 子弹射击方向 (向上发射：-1，向下发射：1)
     */
    private int direction = -1;

    private static HeroAircraft instance;

    /**
     * 私有构造方法，英雄机采用单例模式，因此通过接口返回
     * 
     * @param locationX 英雄机位置x坐标
     * @param locationY 英雄机位置y坐标
     * @param speedX    英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param speedY    英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param hp        初始生命值
     */
    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // 英雄机默认使用直射策略
        this.shootStrategy = new StraightShootStrategy(shootNum);
    }

    /**
     * 获取实例的方法，保证全局只有一个实例
     * 
     * @param locationX
     * @param locationY
     * @param speedX
     * @param speedY
     * @param hp
     * @return
     */
    public static HeroAircraft getInstance(int locationX, int locationY, int speedX, int speedY, int hp) {
        if (instance == null) {
            synchronized (HeroAircraft.class) {
                // 使用类上锁同时双重保证原子性
                if (instance == null) {
                    instance = new HeroAircraft(locationX, locationY, speedX, speedY, hp);
                }
            }
        }
        return instance;
    }

    @Override
    public void forward() {
        // 英雄机由鼠标控制，不通过forward函数移动
    }

    @Override
    /**
     * 通过射击产生子弹
     * 
     * @return 射击出的子弹List
     */
    public List<BaseBullet> shoot() {
        if (shootStrategy != null) {
            return shootStrategy.shoot(this.getLocationX(), this.getLocationY(), 
                                     0, this.getSpeedY(), 
                                     power, direction, true);
        }
        return new LinkedList<>();
    }

    public void increaseHp(int hp) {
        if (this.hp + hp > 100) {
            this.hp = 100;
        } else {
            this.hp += hp;
        }
    }
    
    /**
     * 获取子弹威力
     * @return 子弹威力
     */
    public int getPower() {
        return power;
    }
    
    /**
     * 获取射击方向
     * @return 射击方向
     */
    public int getDirection() {
        return direction;
    }
    
    /**
     * 获取子弹发射数量
     * @return 子弹发射数量
     */
    public int getShootNum() {
        return shootNum;
    }
    
    /**
     * 设置子弹发射数量
     * @param shootNum 子弹发射数量
     */
    public void setShootNum(int shootNum) {
        this.shootNum = shootNum;
        // 当shootNum改变时，更新直射策略
        if (shootStrategy instanceof StraightShootStrategy) {
            this.shootStrategy = new StraightShootStrategy(shootNum);
        }
    }

}
