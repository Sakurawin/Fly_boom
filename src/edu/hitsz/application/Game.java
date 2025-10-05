package edu.hitsz.application;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.EliteEnemyFactory;
import edu.hitsz.aircraft.EnemyFactory;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.aircraft.MobEnemyFactory;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.prop.BaseProp;
import edu.hitsz.prop.PropFactory;
import edu.hitsz.prop.BloodPropFactory;
import edu.hitsz.prop.BombPropFactory;
import edu.hitsz.prop.BulletPropFactory;

/**
 * 游戏主面板，游戏启动
 *
 * @author hitsz
 */
public class Game extends JPanel {

    private int backGroundTop = 0;

    /**
     * Scheduled 线程池，用于任务调度
     */
    private final ScheduledExecutorService executorService;

    /**
     * 时间间隔(ms)，控制刷新频率
     */
    private int timeInterval = 40;

    /**
     * 对局中出现的对象集合
     */
    private final HeroAircraft heroAircraft;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;

    private final List<BaseProp> props;

    /**
     * 创建敌机的工厂类
     */
    EnemyFactory mobEnemyFactory;
    EnemyFactory eliteEnemyFactory;

    /**
     * 创建道具的工厂类
     */
    PropFactory bloodPropFactory;
    PropFactory bombPropFactory;
    PropFactory bulletPropFactory;
    /**
     * 敌机常量设置
     */
    private final int MOD_ENEMY = 0;
    private final int SUPER_ENEMY = 1;

    /**
     * 道具常量设置
     */
    private final int BLOOD_PROP = 0;
    private final int BOMB_PROP = 1;
    private final int BULLET_PROP = 2;

    /**
     * Random变量，静态初始化后面直接取用
     */
    private static Random random;

    /**
     * 屏幕中出现的敌机最大数量
     */
    private int enemyMaxNumber = 5;

    /**
     * 当前得分
     */
    private int score = 0;
    /**
     * 当前时刻
     */
    private int time = 0;

    /**
     * 周期（ms)
     * 指示子弹的发射、敌机的产生频率
     */
    private int cycleDuration = 600;
    private int cycleTime = 0;

    /**
     * 游戏结束标志
     */
    private boolean gameOverFlag = false;

    /**
     * 静态加载，多次复用
     */
    static {
        random = new Random();
    }

    public Game() {
        // 使用单例模式重构英雄机
        heroAircraft = HeroAircraft.getInstance(
                Main.WINDOW_WIDTH / 2,
                Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight(),
                0, 0, 100);

        // 提前加载对应的敌机工厂和敌机list
        mobEnemyFactory = new MobEnemyFactory();
        eliteEnemyFactory = new EliteEnemyFactory();

        // 提前加载对应的道具工厂
        bloodPropFactory = new BloodPropFactory();
        bombPropFactory = new BombPropFactory();
        bulletPropFactory = new BulletPropFactory();

        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();

        props = new LinkedList<>();

        /**
         * Scheduled 线程池，用于定时任务调度
         * 关于alibaba code guide：可命名的 ThreadFactory 一般需要第三方包
         * apache 第三方库： org.apache.commons.lang3.concurrent.BasicThreadFactory
         */
        this.executorService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("game-action-%d").daemon(true).build());

        // 启动英雄机鼠标监听
        new HeroController(this, heroAircraft);

    }

    /**
     * 游戏启动入口，执行游戏逻辑
     */
    public void action() {

        // 定时任务：绘制、对象产生、碰撞判定、击毁及结束判定
        Runnable task = () -> {

            time += timeInterval;

            // 周期性执行（控制频率）
            if (timeCountAndNewCycleJudge()) {
                System.out.println(time);
                // 新敌机产生

                if (enemyAircrafts.size() < enemyMaxNumber) {
                    // TODO1.使用随机数来选择生成精英敌机or普通敌机
                    generateEnemyAircraftsByRandom();
                }
                // 飞机射出子弹
                shootAction();
            }

            // 子弹移动
            bulletsMoveAction();

            // 飞机移动
            aircraftsMoveAction();

            // 道具移动
            propsMoveAction();

            // 撞击检测
            crashCheckAction();

            // 后处理
            postProcessAction();

            // 每个时刻重绘界面
            repaint();

            // 游戏结束检查英雄机是否存活
            if (heroAircraft.getHp() <= 0) {
                // 游戏结束
                executorService.shutdown();
                gameOverFlag = true;
                System.out.println("Game Over!");
            }

        };

        /**
         * 以固定延迟时间进行执行
         * 本次任务执行完成后，需要延迟设定的延迟时间，才会执行新的任务
         */
        executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);

    }

    // ***********************
    // Action 各部分
    // ***********************

    private void propsMoveAction() {
        for (BaseProp prop : props) {
            prop.forward();
        }
    }

    /**
     * 添加敌机，随机普通or精英敌机
     * 
     * @param enemyAircrafts2
     */
    private void generateEnemyAircraftsByRandom() {
        int randomChoice = random.nextInt(SUPER_ENEMY + 1); // 从[0,1]之间选择一个
        if (randomChoice == MOD_ENEMY) {
            enemyAircrafts.add(mobEnemyFactory.createEnemy());
        } else if (randomChoice == SUPER_ENEMY) {
            enemyAircrafts.add(eliteEnemyFactory.createEnemy());
        }
    }

    private boolean timeCountAndNewCycleJudge() {
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration) {
            // 跨越到新的周期
            cycleTime %= cycleDuration;
            return true;
        } else {
            return false;
        }
    }

    private void shootAction() {
        // 敌机射击
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            if (enemyAircraft instanceof EliteEnemy) {
                // 如果是精英敌机的话，将敌机子弹添加
                enemyBullets.addAll(enemyAircraft.shoot());
            }
        }

        // 英雄射击
        heroBullets.addAll(heroAircraft.shoot());
    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }

    /**
     * 碰撞检测：
     * 1. 敌机攻击英雄
     * 2. 英雄攻击/撞击敌机
     * 3. 英雄获得补给
     */
    private void crashCheckAction() {
        // 敌机子弹攻击英雄
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            if (heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
        }

        // 英雄子弹攻击敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    // 已被其他子弹击毁的敌机，不再检测
                    // 避免多个子弹重复击毁同一敌机的判定
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    // 敌机撞击到英雄机子弹
                    // 敌机损失一定生命值
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        // 获得分数，产生道具补给
                        generatePropByRandom(enemyAircraft);
                        score += 10;
                    }
                }
                // 英雄机 与 敌机 相撞，均损毁
                if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }

        // Todo: 我方获得道具，道具生效
        for (BaseProp prop : props) {
            if (prop.notValid()) {
                // 道具失效
                continue;
            }

            if (heroAircraft.crash(prop)) {
                prop.active(heroAircraft);
                prop.vanish(); // 道具失效
            }

        }

    }

    /**
     * 击败精英战机随机生成道具
     * 
     * @param enemyAircraft
     */
    private void generatePropByRandom(AbstractAircraft enemyAircraft) {
        // 1.检查战机是不是精英战机
        if (enemyAircraft instanceof EliteEnemy) {
            // 2.随机生成奖励
            int randomChoice = random.nextInt(0, 3);
            BaseProp prop;

            // 使用工厂模式生成道具
            switch (randomChoice) {
                case BLOOD_PROP:
                    prop = bloodPropFactory.createProp(enemyAircraft.getLocationX(),
                            enemyAircraft.getLocationY(),
                            0,
                            enemyAircraft.getSpeedY());
                    break;
                case BOMB_PROP:
                    prop = bombPropFactory.createProp(enemyAircraft.getLocationX(),
                            enemyAircraft.getLocationY(),
                            0,
                            enemyAircraft.getSpeedY());
                    break;
                case BULLET_PROP:
                    prop = bulletPropFactory.createProp(enemyAircraft.getLocationX(),
                            enemyAircraft.getLocationY(),
                            0,
                            enemyAircraft.getSpeedY());
                    break;
                default:
                    prop = bloodPropFactory.createProp(enemyAircraft.getLocationX(),
                            enemyAircraft.getLocationY(),
                            0,
                            enemyAircraft.getSpeedY());
                    break;
            }

            // 道具列表添加
            props.add(prop);
        }

    }

    /**
     * 后处理：
     * 1. 删除无效的子弹
     * 2. 删除无效的敌机
     * 3. 删除无效的道具
     * <p>
     * 无效的原因可能是撞击或者飞出边界
     */
    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid);
    }

    // ***********************
    // Paint 各部分
    // ***********************

    /**
     * 重写paint方法
     * 通过重复调用paint方法，实现游戏动画
     *
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // 绘制背景,图片滚动
        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop - Main.WINDOW_HEIGHT, null);
        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop, null);
        this.backGroundTop += 1;
        if (this.backGroundTop == Main.WINDOW_HEIGHT) {
            this.backGroundTop = 0;
        }

        // 先绘制子弹，后绘制飞机
        // 这样子弹显示在飞机的下层
        paintImageWithPositionRevised(g, enemyBullets);
        paintImageWithPositionRevised(g, heroBullets);

        paintImageWithPositionRevised(g, enemyAircrafts);

        // 绘制道具
        paintImageWithPositionRevised(g, props);

        g.drawImage(ImageManager.HERO_IMAGE, heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2,
                heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2, null);

        // 绘制得分和生命值
        paintScoreAndLife(g);

    }

    private void paintImageWithPositionRevised(Graphics g, List<? extends AbstractFlyingObject> objects) {
        if (objects.size() == 0) {
            return;
        }

        for (AbstractFlyingObject object : objects) {
            BufferedImage image = object.getImage();
            assert image != null : objects.getClass().getName() + " has no image! ";
            g.drawImage(image, object.getLocationX() - image.getWidth() / 2,
                    object.getLocationY() - image.getHeight() / 2, null);
        }
    }

    private void paintScoreAndLife(Graphics g) {
        int x = 10;
        int y = 25;
        g.setColor(new Color(16711680));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SCORE:" + this.score, x, y);
        y = y + 20;
        g.drawString("LIFE:" + this.heroAircraft.getHp(), x, y);
    }

}
