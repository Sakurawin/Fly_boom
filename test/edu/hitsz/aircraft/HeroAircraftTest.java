package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * HeroAircraft单元测试类
 * 采用黑盒测试和白盒测试相结合的方法
 * 测试方法包括：
 * 1. getInstance() - 单例模式测试（黑盒测试：边界值测试、等价类划分）
 * 2. increaseHp() - 增加生命值测试（白盒测试：路径覆盖、边界条件）
 * 3. shoot() - 射击功能测试（黑盒测试：功能测试，白盒测试：语句覆盖）
 *
 * @author Test
 */
class HeroAircraftTest {

    private HeroAircraft heroAircraft;

    @BeforeEach
    void setUp() {
        // 每次测试前重置单例，避免测试间相互影响
        HeroAircraft.reset();
        heroAircraft = HeroAircraft.getInstance(400, 500, 0, 0, 1000);
    }

    @AfterEach
    void tearDown() {
        // 测试后清理
        HeroAircraft.reset();
    }

    // ========================= getInstance()方法测试 =========================

    /**
     * 测试单例模式的getInstance方法
     * 黑盒测试：等价类划分测试
     * 等价类1：首次创建实例
     * 等价类2：再次获取实例应返回同一对象
     * 边界值测试：实例存在和不存在的边界情况
     */
    @Test
    @DisplayName("测试单例模式 - 多次获取实例应返回同一对象")
    void testGetInstance() {
        // 由于setUp已经创建了实例，这里验证再次调用返回同一实例
        HeroAircraft instance1 = HeroAircraft.getInstance(100, 200, 5, 5, 800);
        HeroAircraft instance2 = HeroAircraft.getInstance(300, 400, 10, 10, 900);

        // 验证单例特性
        assertSame(instance1, instance2, "多次调用getInstance应返回同一实例");
        assertSame(heroAircraft, instance1, "应该返回setUp中创建的实例");

        // 验证参数不会改变已存在的实例
        assertEquals(400, instance2.getLocationX(), "实例已存在时X坐标不应改变");
        assertEquals(500, instance2.getLocationY(), "实例已存在时Y坐标不应改变");
        assertEquals(1000, instance2.getHp(), "实例已存在时生命值不应改变");

        // 边界值测试：重置后创建新实例
        HeroAircraft.reset();
        HeroAircraft newInstance = HeroAircraft.getInstance();

        // 验证默认参数
        assertEquals(400, newInstance.getLocationX(), "默认X坐标应为400");
        assertEquals(500, newInstance.getLocationY(), "默认Y坐标应为500");
        assertEquals(1000, newInstance.getHp(), "默认生命值应为1000");
    }

    // ========================= increaseHp()方法测试 =========================

    /**
     * 测试increaseHp方法
     * 白盒测试：路径覆盖测试，包含所有可能的执行路径
     * 路径1：hp + increase <= maxHp
     * 路径2：hp + increase > maxHp
     * 边界条件：增加0、负数、刚好达到最大值
     */
    @Test
    @DisplayName("测试增加生命值 - 路径覆盖和边界条件")
    void testIncreaseHp() {
        // 路径1：hp + increase <= maxHp（正常增加）
        heroAircraft.decreaseHp(300); // 从1000减到700
        int currentHp = heroAircraft.getHp(); // 700
        heroAircraft.increaseHp(200); // 增加200，应该变成900
        assertEquals(currentHp + 200, heroAircraft.getHp(),
                "正常情况下生命值应增加指定数量");

        // 路径2：hp + increase > maxHp（超过最大值）
        heroAircraft.increaseHp(300); // 尝试增加300，总共1200，超过maxHp(1000)
        assertEquals(1000, heroAircraft.getHp(),
                "生命值不应超过最大值");

        // 边界条件1：增加0
        int hpBeforeZero = heroAircraft.getHp();
        heroAircraft.increaseHp(0);
        assertEquals(hpBeforeZero, heroAircraft.getHp(), "增加0应不改变生命值");

        // 边界条件2：增加负数
        heroAircraft.increaseHp(-100);
        assertEquals(hpBeforeZero - 100, heroAircraft.getHp(), "增加负数实际是减少");

        // 边界条件3：刚好达到最大值
        HeroAircraft.reset();
        HeroAircraft testHero = HeroAircraft.getInstance(0, 0, 0, 0, 100);
        testHero.decreaseHp(50); // 当前50
        testHero.increaseHp(50); // 增加50，刚好100
        assertEquals(100, testHero.getHp(), "刚好达到最大值");
    }

    // ========================= shoot()方法测试 =========================

    /**
     * 测试shoot方法
     * 黑盒测试：功能测试，验证射击功能的基本行为
     * 白盒测试：语句覆盖测试，覆盖所有语句和表达式计算
     */
    @Test
    @DisplayName("测试射击功能 - 功能测试和语句覆盖")
    void testShoot() {
        // 黑盒测试：基本功能测试
        List<BaseBullet> bullets = heroAircraft.shoot();

        // 验证返回结果
        assertNotNull(bullets, "射击应返回子弹列表");
        assertEquals(1, bullets.size(), "默认应发射1颗子弹");

        // 验证子弹对象
        BaseBullet bullet = bullets.get(0);
        assertNotNull(bullet, "子弹对象不应为null");

        // 白盒测试：语句覆盖 - 验证位置计算
        // x = this.getLocationX() + (i * 2 - shootNum + 1) * 10
        // 当i=0, shootNum=1时：x = locationX + (0 * 2 - 1 + 1) * 10 = locationX
        assertEquals(heroAircraft.getLocationX(), bullet.getLocationX(),
                "子弹X坐标计算错误");

        // y = this.getLocationY() + direction * 2
        // direction = -1，所以 y = locationY + (-1) * 2 = locationY - 2
        assertEquals(heroAircraft.getLocationY() - 2, bullet.getLocationY(),
                "子弹Y坐标计算错误");

        // 白盒测试：表达式覆盖 - 验证速度计算
        HeroAircraft.reset();
        HeroAircraft testHero = HeroAircraft.getInstance(400, 500, 0, -10, 1000);
        List<BaseBullet> speedTestBullets = testHero.shoot();
        BaseBullet speedTestBullet = speedTestBullets.get(0);

        // speedY = this.getSpeedY() + direction * 5
        // this.getSpeedY() = -10, direction = -1
        // 预期：-10 + (-1) * 5 = -15
        assertEquals(-15, speedTestBullet.getSpeedY(), "子弹Y方向速度计算错误");

        // 白盒测试：验证子弹属性设置
        assertEquals(30, speedTestBullet.getPower(), "子弹伤害值应为30");

        // 验证多次射击的一致性（等价类测试）
        List<BaseBullet> bullets1 = testHero.shoot();
        List<BaseBullet> bullets2 = testHero.shoot();
        assertEquals(bullets1.size(), bullets2.size(), "每次射击子弹数量应一致");
        assertEquals(bullets1.get(0).getPower(), bullets2.get(0).getPower(),
                "每次射击子弹伤害应一致");
    }
}