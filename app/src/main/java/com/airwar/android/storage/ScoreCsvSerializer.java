package com.airwar.android.storage;

import java.util.ArrayList;
import java.util.List;

public class ScoreCsvSerializer {
    public static final String HEADER = "score,name,durationSec,difficulty,avatarId";

    public String serialize(GameScore gameScore) {
        return gameScore.getScore()
                + ","
                + escape(gameScore.getName())
                + ","
                + gameScore.getDurationSec()
                + ","
                + escape(gameScore.getDifficulty())
                + ","
                + escape(gameScore.getAvatarId());
    }

    public GameScore deserialize(String line) {
        List<String> fields = splitEscaped(line);
        if (fields.size() != 3 && fields.size() != 4 && fields.size() != 5) {
            throw new IllegalArgumentException("invalid score csv line: " + line);
        }
        int score = Integer.parseInt(fields.get(0));
        String name = unescape(fields.get(1));
        int durationSec = Integer.parseInt(fields.get(2));
        String difficulty = fields.size() >= 4 ? unescape(fields.get(3)) : "normal";
        String avatarId = fields.size() >= 5 ? unescape(fields.get(4)) : "default";
        return new GameScore(score, name, durationSec, difficulty, avatarId);
    }

    private String escape(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '\\' || ch == ',') {
                builder.append('\\');
            }
            builder.append(ch);
        }
        return builder.toString();
    }

    private String unescape(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        boolean escaping = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (escaping) {
                builder.append(ch);
                escaping = false;
                continue;
            }
            if (ch == '\\') {
                escaping = true;
                continue;
            }
            builder.append(ch);
        }
        if (escaping) {
            builder.append('\\');
        }
        return builder.toString();
    }

    private List<String> splitEscaped(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaping = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (escaping) {
                current.append(ch);
                escaping = false;
                continue;
            }
            if (ch == '\\') {
                escaping = true;
                current.append(ch);
                continue;
            }
            if (ch == ',') {
                fields.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }

        if (escaping) {
            current.append('\\');
        }
        fields.add(current.toString());
        return fields;
    }
}
