package edu.hitsz.prop;

/**
 * 子弹道具工厂类
 */
public class BulletPropFactory implements PropFactory {

    @Override
    public BaseProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new BulletProp(locationX, locationY, speedX, speedY);
    }
}