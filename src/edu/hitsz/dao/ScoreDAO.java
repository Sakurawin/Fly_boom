package edu.hitsz.dao;

import java.util.List;

/**
 * 得分数据访问对象接口
 * 定义了得分数据的基本操作
 * 
 * @author hitsz
 */
public interface ScoreDAO {

    /**
     * 保存得分记录
     * 
     * @param scoreRecord 得分记录
     */
    void saveScore(ScoreRecord scoreRecord);

    /**
     * 获取所有得分记录
     * 
     * @return 得分记录列表
     */
    List<ScoreRecord> getAllScores();

    /**
     * 获取排行榜前n名
     * 
     * @param limit 排行榜数量限制
     * @return 前n名得分记录列表
     */
    List<ScoreRecord> getTopScores(int limit);

    /**
     * 删除所有得分记录
     */
    void clearAllScores();
}