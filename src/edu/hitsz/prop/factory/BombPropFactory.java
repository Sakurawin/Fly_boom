package edu.hitsz.prop.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BombProp;

/**
 * 炸弹道具工厂
 * 负责创建炸弹道具实例
 * 
 * @author hitsz
 */
public class BombPropFactory extends PropFactory {

    @Override
    public AbstractProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new BombProp(locationX, locationY, speedX, speedY);
    }
}