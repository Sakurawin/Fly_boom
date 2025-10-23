package edu.hitsz.strategy;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 散射策略
 * 发射呈扇形分布的子弹
 * 
 * @author hitsz
 */
public class ScatterShootStrategy implements ShootStrategy {
    
    private int shootNum;
    private int spreadAngle; // 散射角度（度）
    
    public ScatterShootStrategy(int shootNum, int spreadAngle) {
        this.shootNum = shootNum;
        this.spreadAngle = spreadAngle;
    }
    
    @Override
    public List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, int power, int direction, boolean isHero) {
        List<BaseBullet> res = new LinkedList<>();
        int x = locationX;
        int y = locationY + direction * 2;
        int baseSpeedX = 0;
        int baseSpeedY = speedY + direction * 5;

        BaseBullet bullet;
        for (int i = 0; i < shootNum; i++) {
            // 计算散射角度，中间一颗直射，两边各一颗斜射
            double angle = Math.toRadians((i - shootNum / 2) * spreadAngle / (shootNum - 1)); 
            int bulletSpeedX = (int) (baseSpeedX + Math.abs(baseSpeedY) * Math.sin(angle));
            int bulletSpeedY = (int) (baseSpeedY * Math.cos(angle));

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