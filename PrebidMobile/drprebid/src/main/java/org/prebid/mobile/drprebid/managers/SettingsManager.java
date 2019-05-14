package org.prebid.mobile.drprebid.managers;

import android.content.Context;

import org.prebid.mobile.drprebid.model.AdServerSettings;
import org.prebid.mobile.drprebid.model.GeneralSettings;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;

public class SettingsManager {

    private static volatile SettingsManager sInstance;
    private static final Object mutex = new Object();

    public static SettingsManager getInstance() {
        SettingsManager result = sInstance;
        if (result == null) {
            synchronized (mutex) {
                result = sInstance;
                if (result == null) {
                    sInstance = result = new SettingsManager();
                }
            }
        }

        return result;
    }

    private SettingsManager() {

    }

    public GeneralSettings getGeneralSettings() {
        GeneralSettings settings = new GeneralSettings();
        return settings;
    }

    public AdServerSettings getAdServerSettings() {
        AdServerSettings settings = new AdServerSettings();
        return settings;
    }

    public PrebidServerSettings getPrebidServerSettings() {
        PrebidServerSettings settings = new PrebidServerSettings();
        return settings;
    }
}
