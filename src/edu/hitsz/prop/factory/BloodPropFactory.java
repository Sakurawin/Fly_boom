package edu.hitsz.prop.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BloodProp;

/**
 * 加血道具工厂
 * 负责创建加血道具实例
 * 
 * @author hitsz
 */
public class BloodPropFactory extends PropFactory {

    @Override
    public AbstractProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new BloodProp(locationX, locationY, speedX, speedY);
    }
}