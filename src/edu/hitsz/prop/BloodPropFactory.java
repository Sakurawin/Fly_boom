package edu.hitsz.prop;

/**
 * 血量道具工厂类
 */
public class BloodPropFactory implements PropFactory {

    @Override
    public BaseProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new BloodProp(locationX, locationY, speedX, speedY);
    }
}