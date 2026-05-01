package com.airwar.android.debug;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class StartupDiagnostics {
    private static final String FILE_NAME = "startup-diagnostics.log";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private StartupDiagnostics() {
    }

    public static void reset(Context context) {
        File file = file(context);
        if (file.exists() && !file.delete()) {
            appendLine(context, "failed to clear previous diagnostics file");
            return;
        }
        appendLine(context, "diagnostics reset");
    }

    public static void logStage(Context context, String stage) {
        appendLine(context, "stage=" + stage);
    }

    public static void logCrash(Context context, Throwable throwable) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        appendLine(context, "uncaught=" + throwable + "\n" + writer);
    }

    public static File file(Context context) {
        return new File(context.getApplicationContext().getFilesDir(), FILE_NAME);
    }

    private static synchronized void appendLine(Context context, String message) {
        File file = file(context);
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write("[");
            writer.write(LocalDateTime.now().format(TIME_FORMAT));
            writer.write("] ");
            writer.write(message);
            writer.write('\n');
        } catch (IOException ignored) {
        }
    }
}
