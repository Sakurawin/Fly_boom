package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

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
        List<BaseBullet> res = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY() + direction * 2;
        int baseSpeedX = 0;
        int baseSpeedY = this.getSpeedY() + direction * 5;

        BaseBullet bullet;
        for (int i = 0; i < shootNum; i++) {
            // 计算散射角度，中间一颗直射，两边各一颗斜射
            double angle = Math.toRadians((i - 1) * spreadAngle); // -30°, 0°, 30°
            int speedX = (int) (baseSpeedX + baseSpeedY * Math.sin(angle));
            int speedY = (int) (baseSpeedY * Math.cos(angle));

            bullet = new EnemyBullet(x, y, speedX, speedY, power);
            res.add(bullet);
        }
        return res;
    }
}