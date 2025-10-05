package edu.hitsz.prop;

/**
 * 炸弹道具工厂类
 */
public class BombPropFactory implements PropFactory {

    @Override
    public BaseProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new BombProp(locationX, locationY, speedX, speedY);
    }
}