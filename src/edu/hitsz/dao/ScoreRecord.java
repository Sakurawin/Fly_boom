package edu.hitsz.dao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 得分记录实体类
 * 
 * @author hitsz
 */
public class ScoreRecord implements Comparable<ScoreRecord> {
    /**
     * 玩家名称
     */
    private String playerName;

    /**
     * 得分
     */
    private int score;

    /**
     * 游戏时间
     */
    private LocalDateTime gameTime;

    public ScoreRecord() {
    }

    public ScoreRecord(String playerName, int score, LocalDateTime gameTime) {
        this.playerName = playerName;
        this.score = score;
        this.gameTime = gameTime;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public LocalDateTime getGameTime() {
        return gameTime;
    }

    public void setGameTime(LocalDateTime gameTime) {
        this.gameTime = gameTime;
    }

    @Override
    public int compareTo(ScoreRecord other) {
        // 按得分降序排列
        return Integer.compare(other.score, this.score);
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("%-15s %8d分 %s",
                playerName, score, gameTime.format(formatter));
    }

    /**
     * 将记录转换为文件存储格式
     * 
     * @return 格式化字符串
     */
    public String toFileFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return playerName + "," + score + "," + gameTime.format(formatter);
    }

    /**
     * 从文件格式字符串解析记录
     * 
     * @param line 文件行
     * @return ScoreRecord对象
     */
    public static ScoreRecord fromFileFormat(String line) {
        String[] parts = line.split(",");
        if (parts.length == 3) {
            String playerName = parts[0];
            int score = Integer.parseInt(parts[1]);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime gameTime = LocalDateTime.parse(parts[2], formatter);
            return new ScoreRecord(playerName, score, gameTime);
        }
        return null;
    }
}