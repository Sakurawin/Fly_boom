package edu.hitsz.prop.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.FireProp;

/**
 * 火力道具工厂
 * 负责创建火力道具实例
 * 
 * @author hitsz
 */
public class FirePropFactory extends PropFactory {

    @Override
    public AbstractProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new FireProp(locationX, locationY, speedX, speedY);
    }
}