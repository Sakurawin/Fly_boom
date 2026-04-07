# AircraftWar

## Android Migration (WIP)

- New Android modules: `app` + `game-core` with Java 17 and deterministic core tick engine.
- Implemented screens and flow: `Launcher -> Menu -> Game -> GameOver -> Leaderboard`.
- Added Android rendering loop (`SurfaceView`), touch target input, HUD overlay, and audio manager facade.
- Added CSV-based score persistence for Android app files directory.
- Added parity checklist: `docs/android/parity-checklist.md` and core parity test `GameplayParityTest`.
- Build note: Android app tasks require local SDK setup (`ANDROID_HOME` or `local.properties`).

## 项目概述

飞机大战游戏采用面向对象设计，实现了多种设计模式来提升代码的可维护性和可扩展性。主要包含以下设计模式：

- **单例模式**：英雄飞机的唯一实例管理
- **工厂模式**：敌机和道具的创建管理
- **策略模式**：飞机射击行为的动态切换
- **DAO模式**：游戏成绩的数据持久化

## 新增功能

### 策略模式射击系统
- **直线射击**：普通单发射击，适用于英雄飞机和普通敌机
- **散射射击**：多发子弹散射，增强精英敌机火力
- **环形射击**：360度全方位射击，Boss敌机专用
- **动态切换**：通过道具可临时改变英雄飞机射击模式

### 成绩持久化系统
- **数据存储**：CSV格式文件存储游戏成绩
- **排行榜**：支持按分数排序的排行榜显示
- **历史记录**：完整的游戏历史记录查询
- **统计功能**：成绩统计和分析功能

### 超级子弹道具
- **环形射击道具**：获得后英雄飞机临时获得360度射击能力
- **时效限制**：道具效果持续一定时间后恢复原始射击模式

## 技术特点

- **Java 8+ 特性**：Stream API、Lambda表达式、LocalDateTime
- **设计模式应用**：多种设计模式的综合运用
- **文件I/O处理**：CSV格式的数据持久化机制
- **异常处理**：完善的异常处理和资源管理

## 项目类图

## 项目类图

### 新增设计模式

#### 4. 策略模式类图

采用策略模式重构飞机射击系统，实现射击行为的动态切换。

```plantuml
@startuml
!theme plain
title 飞机射击策略模式类图

interface ShootStrategy {
    + {abstract} shoot(int x, int y, int direction, int power): List<BaseBullet>
}

class StraightShootStrategy {
    + shoot(int x, int y, int direction, int power): List<BaseBullet>
}

class ScatterShootStrategy {
    - shootNum: int
    + shoot(int x, int y, int direction, int power): List<BaseBullet>
}

class CircularShootStrategy {
    - shootNum: int
    + shoot(int x, int y, int direction, int power): List<BaseBullet>
}

abstract class AbstractAircraft {
    # shootStrategy: ShootStrategy
    + setShootStrategy(ShootStrategy strategy): void
    + executeShoot(): List<BaseBullet>
}

class HeroAircraft {
    + HeroAircraft()
    + executeShoot(): List<BaseBullet>
}

class MobEnemy {
    + MobEnemy()
    + executeShoot(): List<BaseBullet>
}

class EliteEnemy {
    + EliteEnemy()
    + executeShoot(): List<BaseBullet>
}

class BossEnemy {
    + BossEnemy()
    + executeShoot(): List<BaseBullet>
}

ShootStrategy <|.. StraightShootStrategy
ShootStrategy <|.. ScatterShootStrategy
ShootStrategy <|.. CircularShootStrategy

AbstractAircraft --> ShootStrategy : 使用
AbstractAircraft <|-- HeroAircraft
AbstractAircraft <|-- MobEnemy
AbstractAircraft <|-- EliteEnemy
AbstractAircraft <|-- BossEnemy

note right of ShootStrategy : 策略接口\n定义射击行为规范
note bottom of StraightShootStrategy : 直线射击策略\n单发子弹直线射击
note bottom of ScatterShootStrategy : 散射策略\n多发子弹散射射击
note bottom of CircularShootStrategy : 环形射击策略\n360度全方位射击

@enduml
```

#### 5. DAO模式类图

实现数据访问对象模式，提供游戏成绩的持久化功能。

```plantuml
@startuml
!theme plain
title 数据访问对象模式类图

class GameScore {
    - score: int
    - gameTime: LocalDateTime
    - playerName: String
    - duration: int
    + GameScore()
    + getScore(): int
    + getGameTime(): LocalDateTime
    + getPlayerName(): String
    + getDuration(): int
}

interface ScoreDao {
    + saveScore(GameScore gameScore): boolean
    + getAllScores(): List<GameScore>
    + getTopScores(int limit): List<GameScore>
    + clearAllScores(): boolean
}

class FileScoreDao {
    - SCORE_FILE: String
    - DELIMITER: String
    + saveScore(GameScore gameScore): boolean
    + getAllScores(): List<GameScore>
    + getTopScores(int limit): List<GameScore>
    + clearAllScores(): boolean
}

class ScoreService {
    - scoreDao: ScoreDao
    + saveGameScore(int score, String playerName, int duration): boolean
    + printAllScores(): void
    + printTopScores(int limit): void
    + printScoreStatistics(): void
}

class Game {
    - scoreService: ScoreService
    - saveGameScore(): void
    - printGameHistory(): void
}

ScoreDao <|.. FileScoreDao
ScoreService --> ScoreDao : 使用
ScoreService --> GameScore : 创建和操作
FileScoreDao --> GameScore : 持久化
Game --> ScoreService : 使用

note right of ScoreDao : DAO接口\n定义数据访问操作
note bottom of FileScoreDao : 文件存储实现\n使用CSV格式持久化
note top of ScoreService : 业务逻辑层\n处理成绩相关操作

@enduml
```

### 原有设计模式

#### 1. 英雄机单例模式类图

#### Mermaid 类图

```mermaid
classDiagram
    class AbstractFlyingObject {
        <<abstract>>
        #int locationX
        #int locationY
        #int speedX
        #int speedY
        #BufferedImage image
        #int width
        #int height
        #boolean isValid
        +AbstractFlyingObject()
        +AbstractFlyingObject(int, int, int, int)
        +forward() void
        +crash(AbstractFlyingObject) boolean
        +getLocationX() int
        +getLocationY() int
        +setLocation(double, double) void
        +getSpeedY() int
        +getImage() BufferedImage
        +getWidth() int
        +getHeight() int
        +notValid() boolean
        +vanish() void
    }

    class AbstractAircraft {
        <<abstract>>
        #int maxHp
        #int hp
        +AbstractAircraft(int, int, int, int, int)
        +decreaseHp(int) void
        +getHp() int
        +shoot() List~BaseBullet~*
    }

    class HeroAircraft {
        -int shootNum
        -int power
        -int direction
        -HeroAircraft instance$
        -HeroAircraft(int, int, int, int, int)
        +getInstance(int, int, int, int, int)$ HeroAircraft
        +forward() void
        +shoot() List~BaseBullet~
        +increaseHp(int) void
    }

    %% 继承关系
    AbstractFlyingObject <|-- AbstractAircraft
    AbstractAircraft <|-- HeroAircraft

    note for HeroAircraft "单例模式实现\n- 私有构造函数\n- 静态实例变量\n- 静态获取方法"
```

#### PlantUML 类图

```plantuml
@startuml
!theme plain
title 英雄机单例模式类图

abstract class AbstractFlyingObject
{
    # locationX:int
    # locationY:int
    # speedX:int
    # speedY:int
    # image:BufferedImage
    # width:int
    # height:int
    # isValid:boolean

    + AbstractFlyingObject()
    + AbstractFlyingObject(int locationX, int locationY, int speedX, int speedY)
    + forward():void
    + crash(AbstractFlyingObject flyingObject):boolean
    + setLocation(double locationX, double locationY):void
    + getLocationX():int
    + getLocationY():int
    + getSpeedY():int
    + getImage():BufferedImage
    + getWidth():int
    + getHeight():int
    + notValid():boolean
    + vanish():void
}

abstract class AbstractAircraft
{
    # maxHp:int
    # hp:int
    + AbstractAircraft(int locationX, int locationY, int speedX, int speedY, int hp)
    + decreaseHp(int decrease):void
    + getHp():int
    + {abstract} shoot():List<BaseBullet>
}

class HeroAircraft {
    - shootNum:int
    - power:int
    - direction:int
    - {static} instance:HeroAircraft
    - HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp)
    + {static} getInstance(int locationX, int locationY, int speedX, int speedY, int hp):HeroAircraft
    + forward():void
    + shoot():List<BaseBullet>
    + increaseHp(int hp):void
}

AbstractFlyingObject <|-- AbstractAircraft
AbstractAircraft <|-- HeroAircraft

note right of HeroAircraft : 单例模式实现\n- 私有构造函数\n- 静态实例变量\n- 公共静态获取方法

@enduml
```

---

#### 2. 敌机工厂模式类图

#### Mermaid 类图

```mermaid
classDiagram
    class AbstractFlyingObject {
        <<abstract>>
        #int locationX
        #int locationY
        #int speedX
        #int speedY
        #BufferedImage image
        #int width
        #int height
        #boolean isValid
        +AbstractFlyingObject()
        +AbstractFlyingObject(int, int, int, int)
        +forward() void
        +crash(AbstractFlyingObject) boolean
    }

    class AbstractAircraft {
        <<abstract>>
        #int maxHp
        #int hp
        +AbstractAircraft(int, int, int, int, int)
        +decreaseHp(int) void
        +getHp() int
        +shoot() List~BaseBullet~*
    }

    class MobEnemy {
        +MobEnemy(int, int, int, int, int)
        +forward() void
        +shoot() List~BaseBullet~
    }

    class EliteEnemy {
        -int shootNum
        -int power
        -int direction
        +EliteEnemy(int, int, int, int, int)
        +shoot() List~BaseBullet~
    }

    class EnemyFactory {
        <<interface>>
        +createEnemy() AbstractAircraft*
    }

    class MobEnemyFactory {
        +createEnemy() AbstractAircraft
    }

    class EliteEnemyFactory {
        +createEnemy() AbstractAircraft
    }

    %% 继承关系
    AbstractFlyingObject <|-- AbstractAircraft
    AbstractAircraft <|-- MobEnemy
    MobEnemy <|-- EliteEnemy

    %% 工厂模式关系
    EnemyFactory <|.. MobEnemyFactory
    EnemyFactory <|.. EliteEnemyFactory

    %% 依赖关系
    MobEnemyFactory ..> MobEnemy : creates
    EliteEnemyFactory ..> EliteEnemy : creates

    note for EnemyFactory "工厂接口\n定义创建敌机的标准"
```

#### PlantUML 类图

```plantuml
@startuml
!theme plain
title 敌机工厂模式类图

abstract class AbstractFlyingObject {
    # locationX:int
    # locationY:int
    # speedX:int
    # speedY:int
    + forward():void
    + crash(AbstractFlyingObject):boolean
}

abstract class AbstractAircraft {
    # maxHp:int
    # hp:int
    + AbstractAircraft(int,int,int,int,int)
    + decreaseHp(int):void
    + {abstract} shoot():List<BaseBullet>
}

class MobEnemy {
    + MobEnemy(int locationX, int locationY, int speedX, int speedY, int hp)
    + forward():void
    + shoot():List<BaseBullet>
}

class EliteEnemy {
    - shootNum:int
    - power:int
    - direction:int
    + EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp)
    + shoot():List<BaseBullet>
}

interface EnemyFactory {
    + {abstract} createEnemy():AbstractAircraft
}

class MobEnemyFactory {
    + createEnemy():AbstractAircraft
}

class EliteEnemyFactory {
    + createEnemy():AbstractAircraft
}

AbstractFlyingObject <|-- AbstractAircraft
AbstractAircraft <|-- MobEnemy
MobEnemy <|-- EliteEnemy

EnemyFactory <|.. MobEnemyFactory
EnemyFactory <|.. EliteEnemyFactory

MobEnemyFactory ..> MobEnemy : creates
EliteEnemyFactory ..> EliteEnemy : creates

note right of EnemyFactory : 工厂接口\n定义创建敌机的标准
note bottom of MobEnemyFactory : 普通敌机工厂\n负责创建MobEnemy
note bottom of EliteEnemyFactory : 精英敌机工厂\n负责创建EliteEnemy

@enduml
```

---

#### 3. 道具工厂模式类图

#### Mermaid 类图

```mermaid
classDiagram
    class AbstractFlyingObject {
        <<abstract>>
        #int locationX
        #int locationY
        #int speedX
        #int speedY
        #BufferedImage image
        #int width
        #int height
        #boolean isValid
        +AbstractFlyingObject()
        +AbstractFlyingObject(int, int, int, int)
        +forward() void
    }

    class BaseProp {
        <<abstract>>
        +BaseProp(int, int, int, int)
        +forward() void
        +active(HeroAircraft) void*
    }

    class BloodProp {
        -int hpReward
        +BloodProp(int, int, int, int)
        +active(HeroAircraft) void
    }

    class BombProp {
        +BombProp(int, int, int, int)
        +active(HeroAircraft) void
    }

    class BulletProp {
        +BulletProp(int, int, int, int)
        +active(HeroAircraft) void
    }

    class PropFactory {
        <<interface>>
        +createProp(int, int, int, int) BaseProp*
    }

    class BloodPropFactory {
        +createProp(int, int, int, int) BaseProp
    }

    class BombPropFactory {
        +createProp(int, int, int, int) BaseProp
    }

    class BulletPropFactory {
        +createProp(int, int, int, int) BaseProp
    }

    %% 继承关系
    AbstractFlyingObject <|-- BaseProp
    BaseProp <|-- BloodProp
    BaseProp <|-- BombProp
    BaseProp <|-- BulletProp

    %% 工厂模式关系
    PropFactory <|.. BloodPropFactory
    PropFactory <|.. BombPropFactory
    PropFactory <|.. BulletPropFactory

    %% 依赖关系
    BloodPropFactory ..> BloodProp : creates
    BombPropFactory ..> BombProp : creates
    BulletPropFactory ..> BulletProp : creates

    note for PropFactory "道具工厂接口\n定义创建道具的标准"
```

#### PlantUML 类图

```plantuml
@startuml
!theme plain
title 道具工厂模式类图

abstract class AbstractFlyingObject {
    # locationX:int
    # locationY:int
    # speedX:int
    # speedY:int
    + forward():void
}

abstract class BaseProp
{
    + BaseProp(int locationX, int locationY, int speedX, int speedY)
    + forward():void
    + {abstract} active(HeroAircraft heroAircraft):void
}

class BloodProp {
    - hpReward:int
    + BloodProp(int locationX, int locationY, int speedX, int speedY)
    + active(HeroAircraft heroAircraft):void
}

class BombProp {
    + BombProp(int locationX, int locationY, int speedX, int speedY)
    + active(HeroAircraft heroAircraft):void
}

class BulletProp {
    + BulletProp(int locationX, int locationY, int speedX, int speedY)
    + active(HeroAircraft heroAircraft):void
}

interface PropFactory {
    + {abstract} createProp(int locationX, int locationY, int speedX, int speedY):BaseProp
}

class BloodPropFactory {
    + createProp(int locationX, int locationY, int speedX, int speedY):BaseProp
}

class BombPropFactory {
    + createProp(int locationX, int locationY, int speedX, int speedY):BaseProp
}

class BulletPropFactory {
    + createProp(int locationX, int locationY, int speedX, int speedY):BaseProp
}

AbstractFlyingObject <|-- BaseProp
BaseProp <|-- BloodProp
BaseProp <|-- BombProp
BaseProp <|-- BulletProp

PropFactory <|.. BloodPropFactory
PropFactory <|.. BombPropFactory
PropFactory <|.. BulletPropFactory

BloodPropFactory ..> BloodProp : creates
BombPropFactory ..> BombProp : creates
BulletPropFactory ..> BulletProp : creates

note right of PropFactory : 道具工厂接口\n定义创建道具的标准
note bottom of BloodPropFactory : 血量道具工厂
note bottom of BombPropFactory : 炸弹道具工厂
note bottom of BulletPropFactory : 子弹道具工厂

@enduml
```
