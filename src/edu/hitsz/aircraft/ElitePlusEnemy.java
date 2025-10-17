package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.shoot.ScatterShootStrategy;

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
     * 左右移动方向 (1:向右, -1:向左)
     */
    private int horizontalDirection;

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.power = 30;
        this.direction = 1; // 向下射击
        this.shootStrategy = new ScatterShootStrategy(); // 超级精英敌机散射
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

    /**
     * 获取敌机类型，用于判断掉落道具
     * 
     * @return 敌机类型标识
     */
    public String getEnemyType() {
        return "ElitePlus";
    }
}