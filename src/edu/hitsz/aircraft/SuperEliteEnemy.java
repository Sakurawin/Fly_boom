package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.ScatterShootStrategy;

import java.util.LinkedList;
import java.util.List;

/**
 * 超级精英敌机
 * 可以射击，使用散射弹道
 *
 * @author hitsz
 */
public class SuperEliteEnemy extends EliteEnemy {

    /** 攻击方式 */

    /**
     * 子弹一次发射数量 - 散射弹道发射3颗子弹
     */
    private int shootNum = 3;
    /**
     * 子弹伤害
     */
    private int power = 15;

    /**
     * 子弹射击方向 (向上发射：-1，向下发射：1)
     */
    private int direction = 1;

    /**
     * 散射角度（度）
     */
    private int spreadAngle = 30;

    public SuperEliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // 超级精英敌机使用散射策略
        this.shootStrategy = new ScatterShootStrategy(shootNum, spreadAngle);
    }

    @Override
    public void forward() {
        super.forward();
        // 左右移动逻辑
        if (locationX <= 0 || locationX >= Main.WINDOW_WIDTH) {
            speedX = -speedX;
        }
    }

    @Override
    /**
     * 通过射击产生散射弹道的子弹
     * 同时发射3颗子弹，呈扇形
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