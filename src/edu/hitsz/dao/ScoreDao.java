package edu.hitsz.dao;

import java.util.List;

/**
 * 成绩数据访问对象接口
 * 定义对游戏成绩数据的基本操作
 * 
 * @author hitsz
 */
public interface ScoreDao {
    
    /**
     * 保存游戏成绩
     * @param gameScore 游戏成绩对象
     * @return 保存是否成功
     */
    boolean saveScore(GameScore gameScore);
    
    /**
     * 获取所有成绩记录
     * @return 成绩列表
     */
    List<GameScore> getAllScores();
    
    /**
     * 根据分数范围获取成绩记录
     * @param minScore 最低分数
     * @param maxScore 最高分数
     * @return 成绩列表
     */
    List<GameScore> getScoresByRange(int minScore, int maxScore);
    
    /**
     * 获取前N名高分记录
     * @param limit 记录数量
     * @return 成绩列表，按分数降序排列
     */
    List<GameScore> getTopScores(int limit);
    
    /**
     * 获取指定玩家的成绩记录
     * @param playerName 玩家名称
     * @return 成绩列表
     */
    List<GameScore> getScoresByPlayer(String playerName);
    
    /**
     * 清空所有成绩记录
     * @return 清空是否成功
     */
    boolean clearAllScores();
}