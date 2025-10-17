package edu.hitsz.service;

import edu.hitsz.dao.ScoreDAO;
import edu.hitsz.dao.ScoreRecord;
import edu.hitsz.dao.impl.FileScoreDAO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 得分服务类
 * 负责管理得分排行榜的业务逻辑
 * 
 * @author hitsz
 */
public class ScoreService {

    /**
     * 得分数据访问对象
     */
    private final ScoreDAO scoreDAO;

    /**
     * 排行榜显示数量限制
     */
    private static final int DEFAULT_LEADERBOARD_SIZE = 10;

    public ScoreService() {
        this.scoreDAO = new FileScoreDAO();
    }

    public ScoreService(ScoreDAO scoreDAO) {
        this.scoreDAO = scoreDAO;
    }

    /**
     * 记录玩家得分
     * 
     * @param playerName 玩家名称
     * @param score      得分
     */
    public void recordScore(String playerName, int score) {
        ScoreRecord record = new ScoreRecord(playerName, score, LocalDateTime.now());
        scoreDAO.saveScore(record);
        System.out.println("得分已记录: " + playerName + " - " + score + "分");
    }

    /**
     * 获取排行榜
     * 
     * @return 排行榜记录列表
     */
    public List<ScoreRecord> getLeaderboard() {
        return getLeaderboard(DEFAULT_LEADERBOARD_SIZE);
    }

    /**
     * 获取指定数量的排行榜
     * 
     * @param limit 排行榜数量限制
     * @return 排行榜记录列表
     */
    public List<ScoreRecord> getLeaderboard(int limit) {
        return scoreDAO.getTopScores(limit);
    }

    /**
     * 打印排行榜到控制台
     */
    public void printLeaderboard() {
        List<ScoreRecord> leaderboard = getLeaderboard();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("                        得分排行榜");
        System.out.println("=".repeat(60));
        System.out.printf("%-4s %-15s %-10s %s%n", "排名", "玩家姓名", "得分", "游戏时间");
        System.out.println("-".repeat(60));

        if (leaderboard.isEmpty()) {
            System.out.println("                    暂无得分记录");
        } else {
            for (int i = 0; i < leaderboard.size(); i++) {
                ScoreRecord record = leaderboard.get(i);
                System.out.printf("%-4d %s%n", (i + 1), record.toString());
            }
        }

        System.out.println("=".repeat(60));
    }

    /**
     * 打印指定数量的排行榜到控制台
     * 
     * @param limit 排行榜数量限制
     */
    public void printLeaderboard(int limit) {
        List<ScoreRecord> leaderboard = getLeaderboard(limit);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("                     得分排行榜 (前" + limit + "名)");
        System.out.println("=".repeat(60));
        System.out.printf("%-4s %-15s %-10s %s%n", "排名", "玩家姓名", "得分", "游戏时间");
        System.out.println("-".repeat(60));

        if (leaderboard.isEmpty()) {
            System.out.println("                    暂无得分记录");
        } else {
            for (int i = 0; i < leaderboard.size(); i++) {
                ScoreRecord record = leaderboard.get(i);
                System.out.printf("%-4d %s%n", (i + 1), record.toString());
            }
        }

        System.out.println("=".repeat(60));
    }

    /**
     * 清除所有得分记录
     */
    public void clearAllScores() {
        scoreDAO.clearAllScores();
        System.out.println("所有得分记录已清除");
    }

    /**
     * 获取总记录数
     * 
     * @return 总记录数
     */
    public int getTotalRecordsCount() {
        return scoreDAO.getAllScores().size();
    }
}