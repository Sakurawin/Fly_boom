package edu.hitsz.shoot;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 环射策略
 * 用于Boss敌机 - 发射20颗子弹呈环形向各个方向射击
 * 
 * @author hitsz
 */
public class CircularShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, int direction, int power) {
        List<BaseBullet> res = new LinkedList<>();

        // 环形散射：20颗子弹，360度完美均匀分布
        int shootNum = 20;
        int x = locationX;
        int y = locationY + 40; // Boss体型较大，子弹从下方发射

        for (int i = 0; i < shootNum; i++) {
            // 计算每颗子弹的角度（弧度），确保360度均匀覆盖
            double angle = 2.0 * Math.PI * i / shootNum;

            // 子弹速度，使用double计算提高精度
            double bulletSpeed = 5.5; // 统一子弹速度，确保环形效果

            // 计算子弹的速度分量，使用更精确的计算
            int bulletSpeedX = (int) Math.round(bulletSpeed * Math.cos(angle));
            int bulletSpeedY = (int) Math.round(bulletSpeed * Math.sin(angle));

            // 子弹初始位置：在Boss周围形成标准圆形阵列
            int shootRadius = 25; // 发射半径，形成更明显的圆形
            int bulletX = x + (int) Math.round(shootRadius * Math.cos(angle));
            int bulletY = y + (int) Math.round(shootRadius * Math.sin(angle));

            BaseBullet bullet = new EnemyBullet(bulletX, bulletY, bulletSpeedX, bulletSpeedY, power);
            res.add(bullet);
        }

        return res;
    }
}