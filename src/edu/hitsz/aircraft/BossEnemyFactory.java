package edu.hitsz.aircraft;

import edu.hitsz.application.Main;

/**
 * Boss敌机工厂类
 */
public class BossEnemyFactory implements EnemyFactory {

    @Override
    public AbstractAircraft createEnemy() {
        return new BossEnemy(
                Main.WINDOW_WIDTH / 2, // 出现在屏幕中央
                (int) (Main.WINDOW_HEIGHT * 0.2), // 出现在屏幕上方 20% 的位置
                2, // 初始水平速度
                2, // 垂直速度较慢，让Boss能够缓慢下降到固定位置
                500); // Boss具有很高的生命值
    }

}