package edu.hitsz.dao;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于文件存储的成绩DAO实现
 * 使用CSV格式存储游戏成绩数据
 * 
 * @author hitsz
 */
public class FileScoreDao implements ScoreDao {
    
    private static final String SCORE_FILE = "scores.csv";
    private static final String DELIMITER = ",";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public FileScoreDao() {
        // 确保文件存在
        createFileIfNotExists();
    }
    
    /**
     * 创建文件（如果不存在）
     */
    private void createFileIfNotExists() {
        File file = new File(SCORE_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
                // 写入CSV头部
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println("Score,PlayerName,Duration,GameTime");
                }
            } catch (IOException e) {
                System.err.println("Error creating score file: " + e.getMessage());
            }
        }
    }
    
    @Override
    public boolean saveScore(GameScore gameScore) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SCORE_FILE, true))) {
            String line = String.format("%d%s%s%s%d%s%s",
                gameScore.getScore(), DELIMITER,
                gameScore.getPlayerName(), DELIMITER,
                gameScore.getDuration(), DELIMITER,
                gameScore.getGameTime().format(DATE_FORMATTER));
            writer.println(line);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving score: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<GameScore> getAllScores() {
        List<GameScore> scores = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(SCORE_FILE))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                // 跳过CSV头部
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                GameScore score = parseScoreLine(line);
                if (score != null) {
                    scores.add(score);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading scores: " + e.getMessage());
        }
        return scores;
    }
    
    @Override
    public List<GameScore> getScoresByRange(int minScore, int maxScore) {
        return getAllScores().stream()
                .filter(score -> score.getScore() >= minScore && score.getScore() <= maxScore)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<GameScore> getTopScores(int limit) {
        return getAllScores().stream()
                .sorted((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<GameScore> getScoresByPlayer(String playerName) {
        return getAllScores().stream()
                .filter(score -> score.getPlayerName().equals(playerName))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean clearAllScores() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SCORE_FILE))) {
            writer.println("Score,PlayerName,Duration,GameTime");
            return true;
        } catch (IOException e) {
            System.err.println("Error clearing scores: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 解析CSV行为GameScore对象
     * @param line CSV行
     * @return GameScore对象，解析失败返回null
     */
    private GameScore parseScoreLine(String line) {
        try {
            String[] parts = line.split(DELIMITER);
            if (parts.length >= 4) {
                int score = Integer.parseInt(parts[0].trim());
                String playerName = parts[1].trim();
                int duration = Integer.parseInt(parts[2].trim());
                LocalDateTime gameTime = LocalDateTime.parse(parts[3].trim(), DATE_FORMATTER);
                return new GameScore(score, gameTime, playerName, duration);
            }
        } catch (Exception e) {
            System.err.println("Error parsing score line: " + line + " - " + e.getMessage());
        }
        return null;
    }
}