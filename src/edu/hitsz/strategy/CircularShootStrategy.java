package edu.hitsz.strategy;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 环射策略
 * 发射呈环形分布的子弹
 * 
 * @author hitsz
 */
public class CircularShootStrategy implements ShootStrategy {
    
    private int shootNum;
    private int bulletSpeed;
    
    public CircularShootStrategy(int shootNum, int bulletSpeed) {
        this.shootNum = shootNum;
        this.bulletSpeed = bulletSpeed;
    }
    
    @Override
    public List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, int power, int direction, boolean isHero) {
        List<BaseBullet> res = new LinkedList<>();
        int x = locationX;
        int y = locationY;

        BaseBullet bullet;
        for (int i = 0; i < shootNum; i++) {
            // 计算环形发射角度，平均分布在360度
            double angle = Math.toRadians(i * 360.0 / shootNum);
            int bulletSpeedX = (int) (bulletSpeed * Math.cos(angle));
            int bulletSpeedY = (int) (bulletSpeed * Math.sin(angle));

            if (isHero) {
                bullet = new HeroBullet(x, y, bulletSpeedX, bulletSpeedY, power);
            } else {
                bullet = new EnemyBullet(x, y, bulletSpeedX, bulletSpeedY, power);
            }
            res.add(bullet);
        }
        return res;
    }
}