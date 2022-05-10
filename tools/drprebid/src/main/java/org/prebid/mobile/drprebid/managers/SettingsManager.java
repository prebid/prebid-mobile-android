package org.prebid.mobile.drprebid.managers;

import android.content.Context;
import android.content.SharedPreferences;
import org.prebid.mobile.drprebid.Constants;
import org.prebid.mobile.drprebid.model.*;

public class SettingsManager {
    private static final String PREFERENCES_NAME = "dr_prebid_settings";
    private final SharedPreferences sharedPreferences;

    private static volatile SettingsManager instance;
    private static final Object mutex = new Object();

    public static SettingsManager getInstance(final Context context) {
        SettingsManager result = instance;
        if (result == null) {
            synchronized (mutex) {
                result = instance;
                if (result == null) {
                    instance = result = new SettingsManager(context);
                }
            }
        }

        return result;
    }

    private SettingsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public GeneralSettings getGeneralSettings() {
        GeneralSettings settings = new GeneralSettings();

        switch (sharedPreferences.getInt(Constants.Settings.AD_FORMAT, Constants.Settings.AdFormatCodes.BANNER)) {
            case Constants.Settings.AdFormatCodes.BANNER:
                settings.setAdFormat(AdFormat.BANNER);
                break;
            case Constants.Settings.AdFormatCodes.INTERSTITIAL:
                settings.setAdFormat(AdFormat.INTERSTITIAL);
                break;
            default:
                settings.setAdFormat(AdFormat.BANNER);
        }

        switch (sharedPreferences.getInt(Constants.Settings.AD_SIZE, Constants.Settings.AdSizeCodes.SIZE_300x250)) {
            case Constants.Settings.AdSizeCodes.SIZE_300x250:
                settings.setAdSize(AdSize.BANNER_300x250);
                break;
            case Constants.Settings.AdSizeCodes.SIZE_300x600:
                settings.setAdSize(AdSize.BANNER_300x600);
                break;
            case Constants.Settings.AdSizeCodes.SIZE_320x50:
                settings.setAdSize(AdSize.BANNER_320x50);
                break;
            case Constants.Settings.AdSizeCodes.SIZE_320x100:
                settings.setAdSize(AdSize.BANNER_320x100);
                break;
            case Constants.Settings.AdSizeCodes.SIZE_320x480:
                settings.setAdSize(AdSize.BANNER_320x480);
                break;
            case Constants.Settings.AdSizeCodes.SIZE_728x90:
                settings.setAdSize(AdSize.BANNER_728x90);
                break;
            default:
                settings.setAdSize(AdSize.BANNER_300x250);
        }

        return settings;
    }

    public AdServerSettings getAdServerSettings() {
        AdServerSettings settings = new AdServerSettings();

        if (sharedPreferences.getInt(
                Constants.Settings.AD_SERVER,
                Constants.Settings.AdServerCodes.GOOGLE_AD_MANAGER
        ) == Constants.Settings.AdServerCodes.GOOGLE_AD_MANAGER) {
            settings.setAdServer(AdServer.GOOGLE_AD_MANAGER);
        }

        settings.setBidPrice(sharedPreferences.getFloat(Constants.Settings.BID_PRICE, 0.0f));
        settings.setAdUnitId(sharedPreferences.getString(Constants.Settings.AD_UNIT_ID, ""));

        return settings;
    }

    public PrebidServerSettings getPrebidServerSettings() {
        PrebidServerSettings settings = new PrebidServerSettings();

        switch (sharedPreferences.getInt(
                Constants.Settings.PREBID_SERVER,
                Constants.Settings.PrebidServerCodes.APPNEXUS
        )) {
            case Constants.Settings.PrebidServerCodes.APPNEXUS:
                settings.setPrebidServer(PrebidServer.APPNEXUS);
                settings.setCustomPrebidServerUrl("");
                break;
            case Constants.Settings.PrebidServerCodes.RUBICON:
                settings.setPrebidServer(PrebidServer.RUBICON);
                settings.setCustomPrebidServerUrl("");
                break;
            case Constants.Settings.PrebidServerCodes.CUSTOM:
                settings.setPrebidServer(PrebidServer.CUSTOM);
                settings.setCustomPrebidServerUrl(sharedPreferences.getString(
                        Constants.Settings.PREBID_SERVER_CUSTOM_URL,
                        ""
                ));
                break;
            default:
                settings.setPrebidServer(PrebidServer.APPNEXUS);
        }

        settings.setAccountId(sharedPreferences.getString(Constants.Settings.ACCOUNT_ID, ""));
        settings.setConfigId(sharedPreferences.getString(Constants.Settings.CONFIG_ID, ""));

        return settings;
    }

    public void setAdFormat(AdFormat adFormat) {
        sharedPreferences.edit().putInt(Constants.Settings.AD_FORMAT, adFormat.getCode()).apply();
    }

    public void setAdSize(AdSize adSize) {
        sharedPreferences.edit().putInt(Constants.Settings.AD_SIZE, adSize.getCode()).apply();
    }

    public void setAdServer(AdServer adServer) {
        sharedPreferences.edit().putInt(Constants.Settings.AD_SERVER, adServer.getCode()).apply();
    }

    public void setBidPrice(float bidPrice) {
        sharedPreferences.edit().putFloat(Constants.Settings.BID_PRICE, bidPrice).apply();
    }

    public void setAdUnitId(String adUnitId) {
        sharedPreferences.edit().putString(Constants.Settings.AD_UNIT_ID, adUnitId).apply();
    }

    public void setPrebidServer(PrebidServer prebidServer) {
        sharedPreferences.edit().putInt(Constants.Settings.PREBID_SERVER, prebidServer.getCode()).apply();
    }

    public void setPrebidServerCustomUrl(String customUrl) {
        sharedPreferences.edit().putString(Constants.Settings.PREBID_SERVER_CUSTOM_URL, customUrl).apply();
    }

    public void setAccountId(String accountId) {
        sharedPreferences.edit().putString(Constants.Settings.ACCOUNT_ID, accountId).apply();
    }

    public void setConfigId(String configId) {
        sharedPreferences.edit().putString(Constants.Settings.CONFIG_ID, configId).apply();
    }
}
