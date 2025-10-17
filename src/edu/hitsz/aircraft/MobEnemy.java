package edu.hitsz.aircraft;

import edu.hitsz.application.Main;
import edu.hitsz.shoot.NoShootStrategy;

/**
 * 普通敌机
 * 不可射击
 *
 * @author hitsz
 */
public class MobEnemy extends AbstractAircraft {

    public MobEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.power = 10;
        this.direction = 1; // 向下射击
        this.shootStrategy = new NoShootStrategy(); // 普通敌机不射击
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
