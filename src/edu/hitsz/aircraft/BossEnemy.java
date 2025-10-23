package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.strategy.CircularShootStrategy;

import java.util.LinkedList;
import java.util.List;

/**
 * Boss敌机
 * 可以射击，使用环射弹道
 * 悬浮于界面上方左右移动
 *
 * @author hitsz
 */
public class BossEnemy extends AbstractAircraft {

    /** 攻击方式 */

    /**
     * 子弹一次发射数量 - 环射弹道发射20颗子弹
     */
    private int shootNum = 20;

    /**
     * 子弹伤害
     */
    private int power = 20;

    /**
     * 子弹基础速度
     */
    private int bulletSpeed = 3;

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // Boss敌机使用环射策略
        this.shootStrategy = new CircularShootStrategy(shootNum, bulletSpeed);
    }

    @Override
    public void forward() {
        // Boss敌机悬浮于界面上方左右移动
        locationX += speedX;
        locationY += speedY;

        // 限制在屏幕上方区域移动
        if (locationY > Main.WINDOW_HEIGHT * 0.3) {
            locationY = (int) (Main.WINDOW_HEIGHT * 0.3);
            speedY = 0; // 到达指定位置后停止垂直移动
        }

        // 左右边界反弹
        if (locationX <= 0 || locationX >= Main.WINDOW_WIDTH) {
            speedX = -speedX;
        }
    }

    @Override
    /**
     * 通过射击产生环射弹道的子弹
     * 同时发射20颗子弹，呈环形
     * 
     * @return 射击出的子弹List
     */
    public List<BaseBullet> shoot() {
        if (shootStrategy != null) {
            return shootStrategy.shoot(this.getLocationX(), this.getLocationY(), 
                                     0, this.getSpeedY(), 
                                     power, 1, false);
        }
        return new LinkedList<>();
    }

    /**
     * 设置Boss子弹威力
     * 
     * @param power 子弹威力
     */
    public void setPower(int power) {
        this.power = power;
    }

    /**
     * 设置Boss射击子弹数量
     * 
     * @param shootNum 子弹数量
     */
    public void setShootNum(int shootNum) {
        this.shootNum = shootNum;
    }

    /**
     * 设置子弹速度
     * 
     * @param bulletSpeed 子弹速度
     */
    public void setBulletSpeed(int bulletSpeed) {
        this.bulletSpeed = bulletSpeed;
    }
}