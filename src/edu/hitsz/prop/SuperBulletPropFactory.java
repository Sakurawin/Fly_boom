package edu.hitsz.prop;

/**
 * 超级子弹道具工厂
 * 
 * @author hitsz
 */
public class SuperBulletPropFactory implements PropFactory {

    @Override
    public BaseProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new SuperBulletProp(locationX, locationY, speedX, speedY);
    }
}