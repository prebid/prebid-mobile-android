package org.prebid.mobile.javademo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class Settings {

    private static Settings INSTANCE;

    public static void init(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        INSTANCE = new Settings(preferences);
        INSTANCE.setFakeGdpr();
    }

    public static Settings get() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        throw new NullPointerException();
    }

    private static final String KEY_REFRESH_TIME = "KEY_REFRESH_TIME";
    private static final String KEY_LAST_INTEGRATION_KIND_ID = "KEY_LAST_INTEGRATION_KIND_ID";
    private static final String KEY_LAST_AD_FORMAT_ID = "KEY_LAST_AD_FORMAT_ID";

    private final SharedPreferences preferences;

    public Settings(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public int getRefreshTimeSeconds() {
        return preferences.getInt(KEY_REFRESH_TIME, 30);
    }

    public void setRefreshTimeSeconds(int seconds) {
        preferences
            .edit()
            .putInt(KEY_REFRESH_TIME, seconds)
            .apply();
    }

    public int getLastIntegrationKindId() {
        return preferences.getInt(KEY_LAST_INTEGRATION_KIND_ID, 1);
    }

    public void setLastIntegrationKindId(int id) {
        preferences
            .edit()
            .putInt(KEY_LAST_INTEGRATION_KIND_ID, id)
            .apply();
    }

    public int getLastAdFormatId() {
        return preferences.getInt(KEY_LAST_AD_FORMAT_ID, 0);
    }

    public void setLastAdFormatId(int id) {
        preferences
            .edit()
            .putInt(KEY_LAST_AD_FORMAT_ID, id)
            .apply();
    }

    /**
     * Only for test cases!
     */
    private void setFakeGdpr() {
        preferences
            .edit()
            .putInt("IABTCF_gdprApplies", 0)
            .putInt("IABTCF_CmpSdkID", 123)
            .apply();
    }

}
