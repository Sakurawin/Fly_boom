package edu.hitsz.service;

import edu.hitsz.dao.GameScore;
import edu.hitsz.dao.ScoreDao;
import edu.hitsz.dao.FileScoreDao;

import java.util.List;

/**
 * 成绩服务类
 * 处理游戏成绩相关的业务逻辑
 * 
 * @author hitsz
 */
public class ScoreService {
    
    private ScoreDao scoreDao;
    
    public ScoreService() {
        this.scoreDao = new FileScoreDao();
    }
    
    public ScoreService(ScoreDao scoreDao) {
        this.scoreDao = scoreDao;
    }
    
    /**
     * 保存游戏成绩
     * @param score 分数
     * @param playerName 玩家名称
     * @param duration 游戏持续时间（秒）
     * @return 保存是否成功
     */
    public boolean saveGameScore(int score, String playerName, int duration) {
        GameScore gameScore = new GameScore(score, playerName, duration);
        boolean success = scoreDao.saveScore(gameScore);
        if (success) {
            System.out.println("游戏成绩已保存: " + gameScore);
        } else {
            System.err.println("保存游戏成绩失败!");
        }
        return success;
    }
    
    /**
     * 获取并打印所有成绩记录
     */
    public void printAllScores() {
        List<GameScore> scores = scoreDao.getTopScores(Integer.MAX_VALUE); // 按分数排序获取所有成绩
        if (scores.isEmpty()) {
            System.out.println("暂无游戏成绩记录");
            return;
        }
        
        System.out.println("\n=== 所有游戏成绩记录 ===");
        for (int i = 0; i < scores.size(); i++) {
            GameScore score = scores.get(i);
            System.out.printf("第%d名：%s，%d分，游戏时长%d秒，游戏时间：%s\n", 
                            i + 1, 
                            score.getPlayerName(), 
                            score.getScore(), 
                            score.getDuration(),
                            score.getGameTime().toString());
        }
        System.out.println("========================\n");
    }
    
    /**
     * 获取并打印前N名高分记录
     * @param limit 记录数量
     */
    public void printTopScores(int limit) {
        List<GameScore> topScores = scoreDao.getTopScores(limit);
        if (topScores.isEmpty()) {
            System.out.println("暂无游戏成绩记录");
            return;
        }
        
        System.out.println("\n=== 前" + limit + "名高分记录 ===");
        for (int i = 0; i < topScores.size(); i++) {
            GameScore score = topScores.get(i);
            System.out.printf("第%d名：%s，%d分，游戏时长%d秒，游戏时间：%s\n", 
                            i + 1, 
                            score.getPlayerName(), 
                            score.getScore(), 
                            score.getDuration(),
                            score.getGameTime().toString());
        }
        System.out.println("=======================\n");
    }
    
    /**
     * 获取并打印指定玩家的成绩记录
     * @param playerName 玩家名称
     */
    public void printPlayerScores(String playerName) {
        List<GameScore> playerScores = scoreDao.getScoresByPlayer(playerName);
        if (playerScores.isEmpty()) {
            System.out.println("玩家 " + playerName + " 暂无成绩记录");
            return;
        }
        
        // 按分数排序
        playerScores.sort((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()));
        
        System.out.println("\n=== " + playerName + " 的成绩记录 ===");
        for (int i = 0; i < playerScores.size(); i++) {
            GameScore score = playerScores.get(i);
            System.out.printf("第%d名：%s，%d分，游戏时长%d秒，游戏时间：%s\n", 
                            i + 1, 
                            score.getPlayerName(), 
                            score.getScore(), 
                            score.getDuration(),
                            score.getGameTime().toString());
        }
        System.out.println("============================\n");
    }
    
    /**
     * 获取成绩统计信息
     */
    public void printScoreStatistics() {
        List<GameScore> allScores = scoreDao.getAllScores();
        if (allScores.isEmpty()) {
            System.out.println("暂无游戏成绩记录");
            return;
        }
        
        int totalGames = allScores.size();
        int totalScore = allScores.stream().mapToInt(GameScore::getScore).sum();
        int maxScore = allScores.stream().mapToInt(GameScore::getScore).max().orElse(0);
        int minScore = allScores.stream().mapToInt(GameScore::getScore).min().orElse(0);
        double avgScore = (double) totalScore / totalGames;
        
        System.out.println("\n=== 成绩统计 ===");
        System.out.println("总游戏局数: " + totalGames);
        System.out.println("最高分: " + maxScore);
        System.out.println("最低分: " + minScore);
        System.out.println("平均分: " + String.format("%.2f", avgScore));
        System.out.println("总分: " + totalScore);
        System.out.println("===============\n");
    }
    
    /**
     * 获取所有成绩记录（按分数降序排列）
     * @return 所有成绩记录列表
     */
    public List<GameScore> getAllScores() {
        return scoreDao.getTopScores(Integer.MAX_VALUE);
    }
    
    /**
     * 删除指定玩家的所有记录
     * @param playerName 玩家名称
     * @return 删除是否成功
     */
    public boolean deletePlayerRecords(String playerName) {
        return scoreDao.deletePlayerRecords(playerName);
    }
    
    /**
     * 清空所有成绩记录
     * @return 清空是否成功
     */
    public boolean clearAllScores() {
        boolean success = scoreDao.clearAllScores();
        if (success) {
            System.out.println("所有成绩记录已清空");
        } else {
            System.err.println("清空成绩记录失败!");
        }
        return success;
    }
}