package edu.hitsz.application;

import edu.hitsz.aircraft.*;
import edu.hitsz.aircraft.factory.*;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BloodProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.FireProp;
import edu.hitsz.prop.factory.*;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

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

    private final HeroAircraft heroAircraft;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<AbstractProp> props;

    /**
     * 屏幕中出现的敌机最大数量
     */
    private int enemyMaxNumber = 5;

    /**
     * 当前得分
     */
    private int score = 0;

    /**
     * Boss触发分数阈值
     */
    private int bossScoreThreshold = 500;

    /**
     * 当前Boss出现次数
     */
    private int bossCount = 0;

    /**
     * Boss是否已经出现（防止重复生成）
     */
    private boolean bossSpawned = false;
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
     * 超级精英敌机生成周期（ms）
     * 每隔一定周期随机产生
     */
    private int elitePlusCycleDuration = 8000; // 8秒周期
    private int elitePlusCycleTime = 0;

    /**
     * 游戏结束标志
     */
    private boolean gameOverFlag = false;

    /**
     * 敌机工厂
     */
    private final EnemyFactory mobEnemyFactory;
    private final EnemyFactory eliteEnemyFactory;
    private final EnemyFactory elitePlusEnemyFactory;
    private final EnemyFactory bossEnemyFactory;

    /**
     * 道具工厂
     */
    private final PropFactory bloodPropFactory;
    private final PropFactory bombPropFactory;
    private final PropFactory firePropFactory;

    public Game() {
        // 初始化敌机工厂
        mobEnemyFactory = new MobEnemyFactory();
        eliteEnemyFactory = new EliteEnemyFactory();
        elitePlusEnemyFactory = new ElitePlusEnemyFactory();
        bossEnemyFactory = new BossEnemyFactory();

        // 初始化道具工厂
        bloodPropFactory = new BloodPropFactory();
        bombPropFactory = new BombPropFactory();
        firePropFactory = new FirePropFactory();
        heroAircraft = HeroAircraft.getInstance(
                Main.WINDOW_WIDTH / 2,
                Main.WINDOW_HEIGHT - ImageManager.HERO_IMAGE.getHeight(),
                0, 0, 1000);

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
                // 新敌机产生（使用工厂模式）
                if (enemyAircrafts.size() < enemyMaxNumber) {
                    if (Math.random() < 0.5) {
                        // 使用普通敌机工厂生成普通敌机
                        enemyAircrafts.add(mobEnemyFactory.createEnemy(
                                (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.MOB_ENEMY_IMAGE.getWidth())),
                                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                                0,
                                10,
                                30));
                    } else {
                        // 使用精英敌机工厂生成精英敌机
                        enemyAircrafts.add(eliteEnemyFactory.createEnemy(
                                (int) (Math.random() * (Main.WINDOW_WIDTH - ImageManager.ELITE_ENEMY_IMAGE.getWidth())),
                                (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                                (Math.random() > 0.5 ? 1 : -1) * 5,
                                10,
                                60));
                    }
                }
                // 飞机射出子弹
                shootAction();
            }

            // 超级精英敌机周期性生成
            if (elitePlusCountAndNewCycleJudge()) {
                // 每隔一定周期随机产生超级精英敌机
                if (Math.random() < 0.6) { // 60%概率生成
                    enemyAircrafts.add(elitePlusEnemyFactory.createEnemy(
                            (int) (Math.random()
                                    * (Main.WINDOW_WIDTH - ImageManager.ELITE_PLUS_ENEMY_IMAGE.getWidth())),
                            (int) (Math.random() * Main.WINDOW_HEIGHT * 0.05),
                            5, // 左右移动速度
                            8, // 向下移动速度
                            100)); // 更高的生命值
                }
            }

            // Boss敌机分数阈值触发生成
            if (shouldSpawnBoss()) {
                spawnBoss();
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

    /**
     * 超级精英敌机周期判断
     */
    private boolean elitePlusCountAndNewCycleJudge() {
        elitePlusCycleTime += timeInterval;
        if (elitePlusCycleTime >= elitePlusCycleDuration) {
            // 跨越到新的周期
            elitePlusCycleTime %= elitePlusCycleDuration;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否应该生成Boss
     */
    private boolean shouldSpawnBoss() {
        // 当分数达到阈值且Boss还未生成时
        if (score >= bossScoreThreshold && !bossSpawned) {
            return true;
        }
        // 每次击败Boss后，下次Boss需要更高的分数
        if (score >= bossScoreThreshold + (bossCount * 1000) && !bossSpawned) {
            return true;
        }
        return false;
    }

    /**
     * 生成Boss敌机
     */
    private void spawnBoss() {
        if (!bossSpawned) {
            enemyAircrafts.add(bossEnemyFactory.createEnemy(
                    Main.WINDOW_WIDTH / 2 - 40, // Boss居中出现
                    100, // Boss悬浮在界面上方
                    3, // 左右移动速度
                    0, // 不向下移动
                    500 + (bossCount * 200))); // Boss生命值随次数增长
            bossSpawned = true;
            System.out.println("Boss出现！第" + (bossCount + 1) + "次");
        }
    }

    private void shootAction() {
        // 敌机射击
        for (AbstractAircraft enemy : enemyAircrafts) {
            enemyBullets.addAll(enemy.shoot());
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

    private void propsMoveAction() {
        for (AbstractProp prop : props) {
            prop.forward();
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
                        score += 10;
                        if (enemyAircraft instanceof EliteEnemy) {
                            // 精英敌机坠毁，按权重掉落道具：血/火/炸弹各30%，10%不掉落
                            double r = Math.random();
                            int propX = enemyAircraft.getLocationX();
                            int propY = enemyAircraft.getLocationY();
                            int propSpeedX = 0;
                            int propSpeedY = 5;

                            if (r < 0.3) {
                                // 0% - 30%: 使用加血道具工厂创建加血道具
                                props.add(bloodPropFactory.createProp(propX, propY, propSpeedX, propSpeedY));
                            } else if (r < 0.6) {
                                // 30% - 60%: 使用火力道具工厂创建火力道具
                                props.add(firePropFactory.createProp(propX, propY, propSpeedX, propSpeedY));
                            } else if (r < 0.9) {
                                // 60% - 90%: 使用炸弹道具工厂创建炸弹道具
                                props.add(bombPropFactory.createProp(propX, propY, propSpeedX, propSpeedY));
                            } else {
                                // 90% - 100%: 不掉落
                            }
                        } else if (enemyAircraft instanceof ElitePlusEnemy) {
                            // 超级精英敌机坠毁，随机掉落<=1个道具
                            double r = Math.random();
                            int propX = enemyAircraft.getLocationX();
                            int propY = enemyAircraft.getLocationY();
                            int propSpeedX = 0;
                            int propSpeedY = 5;

                            if (r < 0.25) {
                                // 0% - 25%: 使用加血道具工厂创建加血道具
                                props.add(bloodPropFactory.createProp(propX, propY, propSpeedX, propSpeedY));
                            } else if (r < 0.5) {
                                // 25% - 50%: 使用火力道具工厂创建火力道具
                                props.add(firePropFactory.createProp(propX, propY, propSpeedX, propSpeedY));
                            } else if (r < 0.75) {
                                // 50% - 75%: 使用炸弹道具工厂创建炸弹道具
                                props.add(bombPropFactory.createProp(propX, propY, propSpeedX, propSpeedY));
                            } else {
                                // 75% - 100%: 不掉落
                            }
                        } else if (enemyAircraft instanceof BossEnemy) {
                            // Boss敌机坠毁，随机掉落<=3个道具，获得更多分数
                            score += 200; // Boss额外奖励分数
                            bossCount++; // Boss击败次数增加
                            bossSpawned = false; // 重置Boss生成标志，允许下次生成

                            int propX = enemyAircraft.getLocationX();
                            int propY = enemyAircraft.getLocationY();
                            int propSpeedX = 0;
                            int propSpeedY = 5;

                            // 掉落1-3个道具
                            int dropCount = 1 + (int) (Math.random() * 3); // 1-3个道具
                            for (int i = 0; i < dropCount; i++) {
                                double r = Math.random();
                                int offsetX = (i - 1) * 20; // 道具横向分散

                                if (r < 0.33) {
                                    // 33%概率掉落加血道具
                                    props.add(bloodPropFactory.createProp(propX + offsetX, propY, propSpeedX,
                                            propSpeedY));
                                } else if (r < 0.66) {
                                    // 33%概率掉落火力道具
                                    props.add(
                                            firePropFactory.createProp(propX + offsetX, propY, propSpeedX, propSpeedY));
                                } else {
                                    // 33%概率掉落炸弹道具
                                    props.add(
                                            bombPropFactory.createProp(propX + offsetX, propY, propSpeedX, propSpeedY));
                                }
                            }

                            System.out.println("Boss被击败！掉落" + dropCount + "个道具，已击败Boss" + bossCount + "次");
                        }
                    }
                }
                // 英雄机 与 敌机 相撞，均损毁
                if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }

        // 我方获得道具，道具生效
        for (AbstractProp prop : props) {
            if (prop.notValid()) {
                continue;
            }
            if (heroAircraft.crash(prop)) {
                prop.effect(heroAircraft);
                prop.vanish();
            }
        }
    }

    /**
     * 后处理：
     * 1. 删除无效的子弹
     * 2. 删除无效的敌机
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
