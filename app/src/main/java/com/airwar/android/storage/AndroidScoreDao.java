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
        List<GameScore> scores = new ArrayList<>();
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
                        scores.add(serializer.deserialize(line));
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
                writer.write(line);
                writer.newLine();
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
