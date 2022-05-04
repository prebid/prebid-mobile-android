package org.prebid.mobile.drprebid.managers;

import android.content.Context;
import android.content.SharedPreferences;
import org.prebid.mobile.drprebid.Constants;

public class UserPrefsManager {
    private static final String PREFERENCES_NAME = "dr_prebid_user_prefs";
    private final SharedPreferences sharedPreferences;

    private static volatile UserPrefsManager instance;
    private static final Object mutex = new Object();

    public static UserPrefsManager getInstance(Context context) {
        UserPrefsManager result = instance;
        if (result == null) {
            synchronized (mutex) {
                result = instance;
                if (result == null) {
                    instance = result = new UserPrefsManager(context);
                }
            }
        }

        return result;
    }

    private UserPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void setHasWelcomeBeenShown(boolean hasBeenShown) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.Preferences.WELCOME_SHOWN, hasBeenShown);
        editor.apply();
    }

    public boolean hasWelcomeBeenShown() {
        return sharedPreferences.getBoolean(Constants.Preferences.WELCOME_SHOWN, false);
    }
}
