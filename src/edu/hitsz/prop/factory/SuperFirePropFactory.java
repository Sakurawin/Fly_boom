package edu.hitsz.prop.factory;

import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.SuperFireProp;

/**
 * 超级火力道具工厂
 * 负责创建超级火力道具实例
 * 
 * @author hitsz
 */
public class SuperFirePropFactory extends PropFactory {

    @Override
    public AbstractProp createProp(int locationX, int locationY, int speedX, int speedY) {
        return new SuperFireProp(locationX, locationY, speedX, speedY);
    }
}