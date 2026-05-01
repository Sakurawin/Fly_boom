package com.airwar.android;

import android.app.Application;

import com.airwar.android.debug.StartupDiagnostics;

public class AirWarApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StartupDiagnostics.reset(this);
        StartupDiagnostics.logStage(this, "application_on_create");

        Thread.UncaughtExceptionHandler previousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            StartupDiagnostics.logStage(this, "uncaught_exception_thread=" + thread.getName());
            StartupDiagnostics.logCrash(this, throwable);
            if (previousHandler != null) {
                previousHandler.uncaughtException(thread, throwable);
            }
        });
    }
}
