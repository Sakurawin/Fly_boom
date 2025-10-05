package edu.hitsz.aircraft;

/**
 * 敌机工厂类接口
 */
public interface EnemyFactory {
  /**
   * 创建敌机实例
   * 
   * @return
   */
  AbstractAircraft createEnemy();
}
