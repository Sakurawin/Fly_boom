package edu.hitsz.shoot;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 英雄机散射策略
 * 用于英雄机获得火力道具后 - 发射3颗子弹呈散射
 * 
 * @author hitsz
 */
public class HeroScatterShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, int direction, int power) {
        List<BaseBullet> res = new LinkedList<>();

        // 散射：发射3颗子弹，中间直射，两边斜射
        int x = locationX;
        int y = locationY + direction * 2;
        int baseSpeedY = speedY + direction * 5;

        for (int i = 0; i < 3; i++) {
            int bulletSpeedX = 0;
            int bulletSpeedY = baseSpeedY;
            int bulletX = x;

            if (i == 0) {
                // 左斜射
                bulletSpeedX = -3;
                bulletX = x - 15;
            } else if (i == 1) {
                // 直射
                bulletSpeedX = 0;
                bulletX = x;
            } else if (i == 2) {
                // 右斜射
                bulletSpeedX = 3;
                bulletX = x + 15;
            }

            BaseBullet bullet = new HeroBullet(bulletX, y, bulletSpeedX, bulletSpeedY, power);
            res.add(bullet);
        }

        return res;
    }
}