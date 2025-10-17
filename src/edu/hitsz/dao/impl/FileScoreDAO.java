package edu.hitsz.dao.impl;

import edu.hitsz.dao.ScoreDAO;
import edu.hitsz.dao.ScoreRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件存储实现的得分数据访问对象
 * 
 * @author hitsz
 */
public class FileScoreDAO implements ScoreDAO {

    /**
     * 得分数据文件路径
     */
    private static final String SCORE_FILE_PATH = "scores.txt";

    /**
     * 文件锁对象，保证线程安全
     */
    private final Object fileLock = new Object();

    @Override
    public void saveScore(ScoreRecord scoreRecord) {
        synchronized (fileLock) {
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(SCORE_FILE_PATH, StandardCharsets.UTF_8, true))) {
                writer.write(scoreRecord.toFileFormat());
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                System.err.println("保存得分记录时发生错误: " + e.getMessage());
            }
        }
    }

    @Override
    public List<ScoreRecord> getAllScores() {
        synchronized (fileLock) {
            List<ScoreRecord> scores = new ArrayList<>();
            File file = new File(SCORE_FILE_PATH);

            if (!file.exists()) {
                return scores;
            }

            try (BufferedReader reader = new BufferedReader(
                    new FileReader(SCORE_FILE_PATH, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        ScoreRecord record = ScoreRecord.fromFileFormat(line.trim());
                        if (record != null) {
                            scores.add(record);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("读取得分记录时发生错误: " + e.getMessage());
            }

            // 按得分降序排列
            scores.sort(Collections.reverseOrder(Comparator.comparingInt(ScoreRecord::getScore)));
            return scores;
        }
    }

    @Override
    public List<ScoreRecord> getTopScores(int limit) {
        List<ScoreRecord> allScores = getAllScores();
        return allScores.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public void clearAllScores() {
        synchronized (fileLock) {
            try {
                File file = new File(SCORE_FILE_PATH);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                System.err.println("清除得分记录时发生错误: " + e.getMessage());
            }
        }
    }
}