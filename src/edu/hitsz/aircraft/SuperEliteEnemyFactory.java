package edu.hitsz.aircraft;

import edu.hitsz.application.ImageManager;
import edu.hitsz.application.Main;

/**
 * 超级精英敌机工厂类
 */
public class SuperEliteEnemyFactory implements EnemyFactory {

    @Override
    public AbstractAircraft createEnemy() {
        return new SuperEliteEnemy(
                (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_ENEMY_IMAGE.getWidth())),
                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                ((Math.random() > 0.5) ? 1 : -1) * 2, // 给一个随机的水平速度，使其能够左右移动
                8,
                60);
    }

}