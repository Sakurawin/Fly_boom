# 策略模式重构 - 射击系统

## 概述

本项目使用策略模式重构了飞机大战游戏的射击系统，实现了不同机型的火力发射和火力道具加成效果。

## 重构目标

- **普通敌机**：不发射子弹
- **精英敌机**：直射（单颗子弹）
- **超级精英敌机**：散射（3颗子弹呈扇形）
- **Boss敌机**：环射（20颗子弹呈环形）
- **英雄机**：
  - **无道具**：直射（单颗子弹）
  - **火力道具**：散射（3颗子弹呈扇形）
  - **超级火力道具**：环射（12颗子弹呈环形）

## 实现架构

### 1. 策略接口

```java
public interface ShootStrategy {
    List<BaseBullet> shoot(int locationX, int locationY, int speedX, int speedY, 
                           int direction, int power);
}
```

### 2. 具体策略类

#### 敌机策略
- `NoShootStrategy` - 普通敌机（不射击）
- `DirectShootStrategy` - 精英敌机（直射）
- `ScatterShootStrategy` - 超级精英敌机（散射）
- `CircularShootStrategy` - Boss敌机（环射）

#### 英雄机策略
- `HeroDirectShootStrategy` - 普通模式（直射）
- `HeroScatterShootStrategy` - 火力道具模式（散射）
- `HeroCircularShootStrategy` - 超级火力道具模式（环射）

### 3. 上下文类修改

#### AbstractAircraft类
- 添加了`shootStrategy`字段
- 添加了`power`和`direction`字段
- 实现了策略模式的`shoot()`方法
- 提供了`setShootStrategy()`方法

#### 具体飞机类
每个飞机类在构造函数中设置相应的射击策略：

```java
// 普通敌机
this.shootStrategy = new NoShootStrategy();

// 精英敌机
this.shootStrategy = new DirectShootStrategy();

// 超级精英敌机
this.shootStrategy = new ScatterShootStrategy();

// Boss敌机
this.shootStrategy = new CircularShootStrategy();

// 英雄机（默认）
this.shootStrategy = new HeroDirectShootStrategy();
```

#### HeroAircraft类特殊功能
- 添加了`FireMode`枚举（NORMAL, SCATTER, CIRCULAR）
- 提供了切换火力模式的方法：
  - `setScatterMode()` - 设置散射模式
  - `setSuperFireMode()` - 设置环射模式
  - `resetFireMode()` - 重置为普通模式

### 4. 道具系统扩展

#### 新增超级火力道具
- `SuperFireProp` - 超级火力道具类
- `SuperFirePropFactory` - 超级火力道具工厂
- 图片资源：`prop_bulletPlus.png`

#### 道具效果
- `FireProp` - 调用`heroAircraft.setScatterMode()`
- `SuperFireProp` - 调用`heroAircraft.setSuperFireMode()`

### 5. 游戏系统更新

#### Game类修改
- 添加了`superFirePropFactory`
- 更新了道具掉落概率：
  - 精英敌机：血/火/炸弹各25%，超级火力15%
  - 超级精英敌机：血/火/炸弹各20%，超级火力25%
  - Boss敌机：四种道具各25%概率

#### ImageManager类修改
- 添加了`SUPER_FIRE_PROP_IMAGE`
- 注册了SuperFireProp的图片映射

## 设计优势

### 1. 策略模式优势
- **消除条件分支**：不再需要在shoot()方法中使用if-else判断机型
- **易于扩展**：添加新的射击模式只需实现ShootStrategy接口
- **职责分离**：每种射击模式的逻辑独立封装
- **运行时切换**：英雄机可以根据道具动态切换射击策略

### 2. 代码质量提升
- **可读性**：每个策略类职责单一，代码逻辑清晰
- **可维护性**：修改某种射击模式不影响其他模式
- **可测试性**：每个策略可以独立测试
- **可重用性**：策略可以在不同的上下文中使用

### 3. 符合设计原则
- **开闭原则**：对扩展开放，对修改关闭
- **单一职责原则**：每个策略类只负责一种射击行为
- **依赖倒置原则**：高层模块依赖抽象接口，不依赖具体实现

## 射击效果详细说明

### 敌机射击模式

1. **普通敌机（NoShootStrategy）**
   - 返回空的子弹列表
   - 不产生任何子弹

2. **精英敌机（DirectShootStrategy）**
   - 发射1颗子弹
   - 垂直向下直射
   - 子弹伤害：20

3. **超级精英敌机（ScatterShootStrategy）**
   - 发射3颗子弹
   - 中间直射，两侧斜射（±2像素/帧横向速度）
   - 子弹间距：20像素
   - 子弹伤害：30

4. **Boss敌机（CircularShootStrategy）**
   - 发射20颗子弹
   - 360度均匀分布
   - 发射半径：25像素
   - 子弹速度：5.5像素/帧
   - 子弹伤害：40

### 英雄机射击模式

1. **普通模式（HeroDirectShootStrategy）**
   - 发射1颗子弹
   - 垂直向上直射
   - 子弹伤害：30

2. **散射模式（HeroScatterShootStrategy）**
   - 发射3颗子弹
   - 中间直射，两侧斜射（±3像素/帧横向速度）
   - 子弹间距：30像素
   - 子弹伤害：30

3. **环射模式（HeroCircularShootStrategy）**
   - 发射12颗子弹
   - 360度均匀分布
   - 发射半径：15像素
   - 子弹速度：6.0像素/帧
   - 子弹伤害：30

## 测试验证

重构后的代码已通过编译测试，游戏可以正常运行。所有射击策略都按预期工作，英雄机可以通过获得道具来切换不同的射击模式。

## 文件结构

```
src/edu/hitsz/
├── shoot/                          # 新增：射击策略包
│   ├── ShootStrategy.java           # 策略接口
│   ├── NoShootStrategy.java         # 普通敌机策略
│   ├── DirectShootStrategy.java     # 精英敌机策略
│   ├── ScatterShootStrategy.java    # 超级精英敌机策略
│   ├── CircularShootStrategy.java   # Boss敌机策略
│   ├── HeroDirectShootStrategy.java # 英雄机直射策略
│   ├── HeroScatterShootStrategy.java # 英雄机散射策略
│   └── HeroCircularShootStrategy.java # 英雄机环射策略
├── aircraft/                       # 修改：飞机类
│   ├── AbstractAircraft.java       # 添加策略支持
│   ├── MobEnemy.java               # 使用NoShootStrategy
│   ├── EliteEnemy.java             # 使用DirectShootStrategy
│   ├── ElitePlusEnemy.java         # 使用ScatterShootStrategy
│   ├── BossEnemy.java              # 使用CircularShootStrategy
│   └── HeroAircraft.java           # 支持动态策略切换
├── prop/                           # 修改：道具类
│   ├── SuperFireProp.java          # 新增：超级火力道具
│   ├── FireProp.java               # 修改：设置散射模式
│   └── factory/
│       └── SuperFirePropFactory.java # 新增：超级火力道具工厂
├── application/                    # 修改：应用类
│   ├── Game.java                   # 添加超级火力道具支持
│   └── ImageManager.java           # 添加超级火力道具图片
└── uml/
    └── ShootStrategy.puml          # 新增：策略模式UML图
```

这次重构成功地使用策略模式重新设计了射击系统，使代码更加灵活、可扩展和易于维护。