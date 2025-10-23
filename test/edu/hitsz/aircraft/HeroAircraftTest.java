package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 英雄机单元测试类
 * 使用 JUnit 5 框架
 */
public class HeroAircraftTest {

    private HeroAircraft heroAircraft;

    @BeforeEach
    void setUp() {
        // 在每个测试方法执行前重置英雄机实例
        // 由于HeroAircraft使用单例模式，无法直接重置，因此我们使用现有实例
        heroAircraft = HeroAircraft.getInstance(100, 100, 0, 0, 100);
    }

    /**
     * 测试英雄机单例模式实现
     * 测试点:
     * 1. 多次获取实例应返回同一个对象
     * 2. 参数应保持为第一次初始化时的值
     */
    @Test
    void testSingleton() {
        // 获取两个实例
        HeroAircraft instance1 = HeroAircraft.getInstance(200, 200, 1, 1, 90);
        HeroAircraft instance2 = HeroAircraft.getInstance(300, 300, 2, 2, 80);

        // 测试单例模式是否生效 - 两个实例应该是同一个对象
        assertSame(instance1, instance2);

        // 测试实例参数是否是初始化时设定的值，而非后续调用的参数
        // 由于单例模式，第一次初始化后的参数应该保持不变
        assertEquals(100, instance1.getLocationX());
        assertEquals(100, instance1.getLocationY());
    }

    /**
     * 测试英雄机射击功能
     * 测试点:
     * 1. 一次射击应产生正确数量的子弹
     * 2. 子弹位置应正确设置
     * 3. 子弹速度方向应向上
     */
    @Test
    void testShoot() {
        // 测试射击功能
        List<BaseBullet> bullets = heroAircraft.shoot();

        // 默认英雄机一次射出一颗子弹
        assertEquals(1, bullets.size());

        // 子弹位置应该在英雄机位置的上方
        BaseBullet bullet = bullets.get(0);
        assertEquals(heroAircraft.getLocationX(), bullet.getLocationX());
        assertTrue(bullet.getLocationY() < heroAircraft.getLocationY());

        // 子弹速度应该是向上的
        assertTrue(bullet.getSpeedY() < 0);
    }

    /**
     * 测试英雄机生命值相关功能
     * 测试点:
     * 1. 初始生命值设置是否正确
     * 2. 减少生命值功能
     * 3. 增加生命值功能
     * 4. 生命值上限检查
     * 5. 生命值为0时消失状态检查
     */
    @Test
    void testHp() {
        // 初始生命值应该是100
        assertEquals(100, heroAircraft.getHp());

        // 测试减少生命值
        heroAircraft.decreaseHp(30);
        assertEquals(70, heroAircraft.getHp());

        // 测试增加生命值 - 不超过最大值
        heroAircraft.increaseHp(20);
        assertEquals(90, heroAircraft.getHp());

        // 测试增加生命值 - 超过最大值应该限制为100
        heroAircraft.increaseHp(50);
        assertEquals(100, heroAircraft.getHp());

        // 测试生命值为0时飞机应该消失
        heroAircraft.decreaseHp(100);
        assertEquals(0, heroAircraft.getHp());
        assertTrue(heroAircraft.notValid());
    }
}