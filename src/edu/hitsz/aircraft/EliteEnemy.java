package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.shoot.DirectShootStrategy;

/**
 * 精英敌机
 *
 * @author hitsz
 */
public class EliteEnemy extends AbstractAircraft {

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.power = 20;
        this.direction = 1; // 向下射击
        this.shootStrategy = new DirectShootStrategy(); // 精英敌机直射
    }

    @Override
    public void forward() {
        super.forward();
        // 判定 y 轴向下飞行出界
        if (locationY >= Main.WINDOW_HEIGHT) {
            vanish();
        }
    }

}
