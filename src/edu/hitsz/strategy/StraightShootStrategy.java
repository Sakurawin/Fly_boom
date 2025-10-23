package edu.hitsz.strategy;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 直射策略
 * 发射单颗或多颗直线子弹
 * 
 * @author hitsz
 */
public class StraightShootStrategy implements ShootStrategy {
    
    private int shootNum;
    
    public StraightShootStrategy(int shootNum) {
        this.shootNum = shootNum;
    }
    
    @Override
    public List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, int power, int direction, boolean isHero) {
        List<BaseBullet> res = new LinkedList<>();
        int x = locationX;
        int y = locationY + direction * 2;
        int bulletSpeedX = 0;
        int bulletSpeedY = speedY + direction * 5;
        
        BaseBullet bullet;
        for (int i = 0; i < shootNum; i++) {
            // 子弹发射位置相对飞机位置向前偏移
            // 多个子弹横向分散
            if (isHero) {
                bullet = new HeroBullet(x + (i * 2 - shootNum + 1) * 10, y, bulletSpeedX, bulletSpeedY, power);
            } else {
                bullet = new EnemyBullet(x + (i * 2 - shootNum + 1) * 10, y, bulletSpeedX, bulletSpeedY, power);
            }
            res.add(bullet);
        }
        return res;
    }
}