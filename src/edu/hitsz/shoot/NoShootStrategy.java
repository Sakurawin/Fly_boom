package edu.hitsz.shoot;

import edu.hitsz.bullet.BaseBullet;

import java.util.LinkedList;
import java.util.List;

/**
 * 不射击策略
 * 用于普通敌机 - 不发射子弹
 * 
 * @author hitsz
 */
public class NoShootStrategy implements ShootStrategy {

    @Override
    public List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, int direction, int power) {
        // 普通敌机不发射子弹，返回空列表
        return new LinkedList<>();
    }
}