package edu.hitsz.shoot;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 英雄机环射策略
 * 用于英雄机获得超级火力道具后 - 发射多颗子弹呈环形向各个方向射击
 * 
 * @author hitsz
 */
public class HeroCircularShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, int direction, int power) {
        List<BaseBullet> res = new LinkedList<>();

        // 环形散射：12颗子弹，360度均匀分布（比Boss少一些）
        int shootNum = 12;
        int x = locationX;
        int y = locationY + direction * 2;

        for (int i = 0; i < shootNum; i++) {
            // 计算每颗子弹的角度（弧度），确保360度均匀覆盖
            double angle = 2.0 * Math.PI * i / shootNum;

            // 子弹速度
            double bulletSpeed = 6.0; // 英雄机子弹速度稍快

            // 计算子弹的速度分量
            int bulletSpeedX = (int) Math.round(bulletSpeed * Math.cos(angle));
            int bulletSpeedY = (int) Math.round(bulletSpeed * Math.sin(angle));

            // 子弹初始位置：在英雄机周围形成圆形阵列
            int shootRadius = 15; // 发射半径，比Boss小一些
            int bulletX = x + (int) Math.round(shootRadius * Math.cos(angle));
            int bulletY = y + (int) Math.round(shootRadius * Math.sin(angle));

            BaseBullet bullet = new HeroBullet(bulletX, bulletY, bulletSpeedX, bulletSpeedY, power);
            res.add(bullet);
        }

        return res;
    }
}