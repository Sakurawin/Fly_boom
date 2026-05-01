package com.airwar.android.ui;

import android.content.Context;
import android.content.SharedPreferences;

import com.airwar.android.net.NetworkConfig;

public final class LocalMultiplayerPrefs {
    private LocalMultiplayerPrefs() {
    }

    public static String getBaseUrl(Context context) {
        return prefs(context).getString(NetworkConfig.PREF_BASE_URL, NetworkConfig.DEFAULT_BASE_URL);
    }

    public static String getUsername(Context context) {
        return prefs(context).getString(NetworkConfig.PREF_USERNAME, "");
    }

    public static String getRoomId(Context context) {
        return prefs(context).getString(NetworkConfig.PREF_ROOM_ID, "");
    }

    // 把服务地址和用户名持久化下来，方便玩家重进页面后直接恢复联机配置。
    public static void saveBaseConfig(Context context, String baseUrl, String username) {
        prefs(context).edit()
                .putString(NetworkConfig.PREF_BASE_URL, NetworkConfig.normalizeBaseUrl(baseUrl))
                .putString(NetworkConfig.PREF_USERNAME, username == null ? "" : username.trim())
                .apply();
    }

    public static void saveRoomId(Context context, String roomId) {
        prefs(context).edit()
                .putString(NetworkConfig.PREF_ROOM_ID, roomId == null ? "" : roomId.trim())
                .apply();
    }

    public static void clearRoomId(Context context) {
        prefs(context).edit().remove(NetworkConfig.PREF_ROOM_ID).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(NetworkConfig.PREFS_NAME, Context.MODE_PRIVATE);
    }
}
