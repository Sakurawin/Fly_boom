package edu.hitsz.aircraft;

import edu.hitsz.shoot.HeroDirectShootStrategy;
import edu.hitsz.shoot.HeroScatterShootStrategy;
import edu.hitsz.shoot.HeroCircularShootStrategy;

/**
 * 英雄飞机，游戏玩家操控
 * 
 * @author hitsz
 */
public class HeroAircraft extends AbstractAircraft {

    // 使用volatile保证多线程下的可见性与禁止指令重排序（DCL 必须）
    private static volatile HeroAircraft instance;

    private final int maxHp;

    /**
     * 火力状态枚举
     */
    public enum FireMode {
        NORMAL, // 普通模式：直射
        SCATTER, // 散射模式：获得火力道具
        CIRCULAR // 环射模式：获得超级火力道具
    }

    /**
     * 当前火力状态
     */
    private FireMode currentFireMode = FireMode.NORMAL;

    /**
     * @param locationX 英雄机位置x坐标
     * @param locationY 英雄机位置y坐标
     * @param speedX    英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param speedY    英雄机射出的子弹的基准速度（英雄机无特定速度）
     * @param hp        初始生命值
     */
    // 私有构造函数
    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.maxHp = hp;
        this.power = 30;
        this.direction = -1; // 向上射击
        this.shootStrategy = new HeroDirectShootStrategy(); // 默认直射
    }

    /**
     * 获取唯一实例（懒汉式，非线程安全版本，如需多线程支持可加synchronized或双重检查）
     */
    public static HeroAircraft getInstance(int locationX, int locationY, int speedX, int speedY, int hp) {
        if (instance == null) { // 第一重检查（减少进入同步块的频率）
            synchronized (HeroAircraft.class) { // 仅首次创建时同步
                if (instance == null) { // 第二重检查（确保只创建一次）
                    instance = new HeroAircraft(locationX, locationY, speedX, speedY, hp);
                }
            }
        }
        return instance;
    }

    /**
     * 无参获取（已创建后使用当前位置，不存在则用默认中心位置创建）
     */
    public static HeroAircraft getInstance() {
        if (instance == null) {
            return getInstance(400, 500, 0, 0, 1000);
        }
        return instance;
    }

    /** 重置单例（可用于重新开始游戏） */
    public static void reset() {
        synchronized (HeroAircraft.class) {
            instance = null;
        }
    }

    public void increaseHp(int increase) {
        hp += increase;
        if (hp > maxHp) {
            hp = maxHp;
        }
    }

    @Override
    public void forward() {
        // 英雄机由鼠标控制，不通过forward函数移动
    }

    /**
     * 设置火力模式为散射（获得火力道具）
     */
    public void setScatterMode() {
        this.currentFireMode = FireMode.SCATTER;
        this.shootStrategy = new HeroScatterShootStrategy();
        System.out.println("Hero fire mode changed to SCATTER");
    }

    /**
     * 设置火力模式为环射（获得超级火力道具）
     */
    public void setSuperFireMode() {
        this.currentFireMode = FireMode.CIRCULAR;
        this.shootStrategy = new HeroCircularShootStrategy();
        System.out.println("Hero fire mode changed to CIRCULAR");
    }

    /**
     * 重置火力模式为普通模式
     */
    public void resetFireMode() {
        this.currentFireMode = FireMode.NORMAL;
        this.shootStrategy = new HeroDirectShootStrategy();
        System.out.println("Hero fire mode reset to NORMAL");
    }

    /**
     * 获取当前火力模式
     * 
     * @return 当前火力模式
     */
    public FireMode getCurrentFireMode() {
        return currentFireMode;
    }

}
