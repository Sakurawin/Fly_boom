package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 超级精英敌机
 * 特性：
 * - 每隔一定周期随机产生
 * - 向屏幕下方左右移动
 * - 散射弹道，同时发射3颗子弹，呈扇形
 * - 随机掉落<=1个道具
 *
 * @author hitsz
 */
public class ElitePlusEnemy extends AbstractAircraft {

    /**
     * 攻击方式：扇形散射，3颗子弹
     */
    private int shootNum = 3;

    /**
     * 子弹伤害
     */
    private int power = 30;

    /**
     * 子弹射击方向
     */
    private int direction = 1;

    /**
     * 左右移动方向 (1:向右, -1:向左)
     */
    private int horizontalDirection;

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        // 随机初始化左右移动方向
        this.horizontalDirection = Math.random() > 0.5 ? 1 : -1;
    }

    @Override
    public void forward() {
        // 向下移动
        locationY += speedY;

        // 左右移动，碰到边界则反向
        locationX += horizontalDirection * Math.abs(speedX);

        // 检查左右边界碰撞，反向移动
        if (locationX <= 0 || locationX >= Main.WINDOW_WIDTH - 50) {
            horizontalDirection *= -1;
        }

        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int x = this.getLocationX();
        int y = this.getLocationY() + direction * 2;
        int baseSpeedY = this.getSpeedY() + direction * 5;

        // 扇形散射：3颗子弹，中间直射，两边斜射
        for (int i = 0; i < shootNum; i++) {
            int speedX = 0;
            int speedY = baseSpeedY;
            int bulletX = x;

            if (i == 0) {
                // 左斜射
                speedX = -2;
                bulletX = x - 10;
            } else if (i == 1) {
                // 直射
                speedX = 0;
                bulletX = x;
            } else if (i == 2) {
                // 右斜射
                speedX = 2;
                bulletX = x + 10;
            }

            BaseBullet bullet = new EnemyBullet(bulletX, y, speedX, speedY, power);
            res.add(bullet);
        }
        return res;
    }

    /**
     * 获取敌机类型，用于判断掉落道具
     * 
     * @return 敌机类型标识
     */
    public String getEnemyType() {
        return "ElitePlus";
    }
}