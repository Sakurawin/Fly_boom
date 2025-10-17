package edu.hitsz.aircraft.factory;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.ElitePlusEnemy;

/**
 * 超级精英敌机工厂
 * 负责创建超级精英敌机实例
 * 
 * @author hitsz
 */
public class ElitePlusEnemyFactory extends EnemyFactory {

    @Override
    public AbstractAircraft createEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        return new ElitePlusEnemy(locationX, locationY, speedX, speedY, hp);
    }
}