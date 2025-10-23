package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.StraightShootStrategy;

import java.util.LinkedList;
import java.util.List;

/**
 * 精英敌机--继承自普通敌机，添加射击
 * 可以射击
 *
 * @author Acc
 */
public class EliteEnemy extends MobEnemy {

    /** 攻击方式 */

    /**
     * 子弹一次发射数量
     */
    private int shootNum = 1;
    /**
     * 子弹伤害
     */
    private int power = 10; // TODO敌机伤害待设定

    /**
     * 子弹射击方向 (向上发射：-1，向下发射：1)
     */
    private int direction = 1;

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp); // 使用父类普通敌机的方法
        // 精英敌机使用直射策略
        this.shootStrategy = new StraightShootStrategy(shootNum);
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
                                     power, direction, false);
        }
        return new LinkedList<>();
    }

}
