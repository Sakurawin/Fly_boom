package edu.hitsz.prop;

/**
 * 道具工厂类接口
 */
public interface PropFactory {
    /**
     * 创建道具实例
     * 
     * @param locationX 道具x坐标
     * @param locationY 道具y坐标
     * @param speedX    道具x轴速度
     * @param speedY    道具y轴速度
     * @return 道具实例
     */
    BaseProp createProp(int locationX, int locationY, int speedX, int speedY);
}