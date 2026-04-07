package com.airwar.android.storage;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AndroidScoreDao {
    private static final String FILE_NAME = "scores.csv";
    private static final String LEGACY_HEADER = "score,name,durationSec,difficulty";

    private final Context appContext;
    private final ScoreCsvSerializer serializer;

    public AndroidScoreDao(Context context) {
        this.appContext = context.getApplicationContext();
        this.serializer = new ScoreCsvSerializer();
    }

    public boolean appendScore(GameScore gameScore) {
        try {
            ensureHeader();
            File file = getScoreFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(serializer.serialize(gameScore));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public List<GameScore> readScoresSorted() {
        return readScoresSortedByDifficulty(null);
    }

    public List<GameScore> readScoresSortedByDifficulty(String difficulty) {
        List<GameScore> scores = new ArrayList<>();
        String filterDifficulty = difficulty == null ? null : difficulty.trim().toLowerCase();
        try {
            ensureHeader();
            File file = getScoreFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                boolean firstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    try {
                        GameScore score = serializer.deserialize(line);
                        if (filterDifficulty == null || filterDifficulty.isEmpty() || filterDifficulty.equals(score.getDifficulty())) {
                            scores.add(score);
                        }
                    } catch (RuntimeException ignored) {
                    }
                }
            }
        } catch (IOException ignored) {
        }

        scores.sort(
                Comparator.comparingInt(GameScore::getScore).reversed()
                        .thenComparingInt(GameScore::getDurationSec)
        );
        return scores;
    }

    private File getScoreFile() {
        return new File(appContext.getFilesDir(), FILE_NAME);
    }

    private void ensureHeader() throws IOException {
        File file = getScoreFile();
        if (!file.exists()) {
            writeHeaderOnly(file);
            return;
        }

        String firstLine;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            firstLine = reader.readLine();
        }

        if (firstLine == null) {
            writeHeaderOnly(file);
            return;
        }

        if (ScoreCsvSerializer.HEADER.equals(firstLine)) {
            return;
        }

        if (LEGACY_HEADER.equals(firstLine)) {
            rewriteFileWithCurrentHeader(file);
            return;
        }

        rewriteFileWithCurrentHeader(file);
    }

    private void rewriteFileWithCurrentHeader(File file) throws IOException {

        List<String> allLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(ScoreCsvSerializer.HEADER);
            writer.newLine();
            for (String line : allLines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }
                try {
                    GameScore score = serializer.deserialize(line);
                    writer.write(serializer.serialize(score));
                    writer.newLine();
                } catch (RuntimeException ignored) {
                }
            }
        }
    }

    private void writeHeaderOnly(File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(ScoreCsvSerializer.HEADER);
            writer.newLine();
        }
    }
}
