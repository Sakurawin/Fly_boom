package edu.hitsz.shoot;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 直射策略
 * 用于精英敌机 - 发射单颗子弹直射
 * 
 * @author hitsz
 */
public class DirectShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, int direction, int power) {
        List<BaseBullet> res = new LinkedList<>();

        // 直射：发射1颗子弹
        int x = locationX;
        int y = locationY + direction * 2;
        int bulletSpeedX = 0; // 直射，无横向速度
        int bulletSpeedY = speedY + direction * 5;

        BaseBullet bullet = new EnemyBullet(x, y, bulletSpeedX, bulletSpeedY, power);
        res.add(bullet);

        return res;
    }
}