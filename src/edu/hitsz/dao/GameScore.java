package edu.hitsz.dao;

import java.time.LocalDateTime;

/**
 * 游戏成绩实体类
 * 存储单局游戏的成绩数据
 * 
 * @author hitsz
 */
public class GameScore {
    
    private int score;
    private LocalDateTime gameTime;
    private String playerName;
    private int duration; // 游戏持续时间（秒）
    
    public GameScore() {
    }
    
    public GameScore(int score, String playerName, int duration) {
        this.score = score;
        this.playerName = playerName;
        this.duration = duration;
        this.gameTime = LocalDateTime.now();
    }
    
    public GameScore(int score, LocalDateTime gameTime, String playerName, int duration) {
        this.score = score;
        this.gameTime = gameTime;
        this.playerName = playerName;
        this.duration = duration;
    }
    
    // Getters and Setters
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
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    @Override
    public String toString() {
        return String.format("Score: %d, Player: %s, Duration: %ds, Time: %s", 
                           score, playerName, duration, gameTime.toString());
    }
}