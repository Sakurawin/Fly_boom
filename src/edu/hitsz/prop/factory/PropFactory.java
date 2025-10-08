package edu.hitsz.prop.factory;

import edu.hitsz.prop.AbstractProp;

/**
 * 道具工厂抽象类
 * 工厂方法模式的抽象工厂
 * 
 * @author hitsz
 */
public abstract class PropFactory {

    /**
     * 创建道具的抽象方法
     * 
     * @param locationX X坐标
     * @param locationY Y坐标
     * @param speedX    X方向速度
     * @param speedY    Y方向速度
     * @return 道具实例
     */
    public abstract AbstractProp createProp(int locationX, int locationY, int speedX, int speedY);
}