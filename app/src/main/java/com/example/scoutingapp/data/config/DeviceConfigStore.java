package com.example.scoutingapp.data.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Per-tablet settings that survive app restarts. For BUILT this is only the last scouter
 * name typed on this tablet, so the next match setup can pre-fill it.
 */
public class DeviceConfigStore {

    private static final String PREFS_NAME = "device_config";
    private static final String KEY_SCOUT_NAME = "scout_name";

    private final SharedPreferences prefs;

    public DeviceConfigStore(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** The last scouter name saved on this tablet, or "" if none yet. */
    public String getScoutName() {
        return prefs.getString(KEY_SCOUT_NAME, "");
    }

    public void updateScoutName(String name) {
        prefs.edit().putString(KEY_SCOUT_NAME, name == null ? "" : name.trim()).apply();
    }
}
