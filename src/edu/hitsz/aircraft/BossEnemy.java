package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.shoot.CircularShootStrategy;

/**
 * Boss敌机
 * 特性：
 * - 分数达到设定阈值后出现，可多次出现
 * - 悬浮于界面上方左右移动
 * - 环射弹道，同时发射20颗子弹，呈环形
 * - 随机掉落<=3个道具
 *
 * @author hitsz
 */
public class BossEnemy extends AbstractAircraft {

    /**
     * 左右移动方向 (1:向右, -1:向左)
     */
    private int horizontalDirection;

    /**
     * Boss悬浮的Y坐标范围（悬浮于界面上方）
     */
    private int minY = 50;
    private int maxY = 150;
    private int targetY;

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.power = 40;
        this.direction = 1; // 向下射击
        this.shootStrategy = new CircularShootStrategy(); // Boss敌机环射
        // 随机初始化左右移动方向
        this.horizontalDirection = Math.random() > 0.5 ? 1 : -1;
        // 设置悬浮目标Y坐标
        this.targetY = minY + (int) (Math.random() * (maxY - minY));
    }

    @Override
    public void forward() {
        // Boss悬浮移动：左右移动 + 上下微调
        locationX += horizontalDirection * Math.abs(speedX);

        // 检查左右边界碰撞，反向移动
        if (locationX <= 0 || locationX >= Main.WINDOW_WIDTH - 80) {
            horizontalDirection *= -1;
        }

        // 悬浮效果：轻微的上下移动
        if (Math.abs(locationY - targetY) > 5) {
            if (locationY < targetY) {
                locationY += 1;
            } else {
                locationY -= 1;
            }
        } else {
            // 到达目标位置后，随机选择新的目标位置
            if (Math.random() < 0.01) { // 1%概率改变目标
                targetY = minY + (int) (Math.random() * (maxY - minY));
            }
        }

        // Boss不会飞出界面（悬浮在上方）
    }

    /**
     * 获取敌机类型，用于判断掉落道具
     * 
     * @return 敌机类型标识
     */
    public String getEnemyType() {
        return "Boss";
    }

    /**
     * Boss是否可以被击败（血量为0）
     */
    @Override
    public boolean notValid() {
        return hp <= 0;
    }
}