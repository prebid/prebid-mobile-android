package org.prebid.mobile.drprebid.managers;

import android.content.Context;
import android.content.SharedPreferences;

import org.prebid.mobile.drprebid.Constants;

public class QrCodeScanCacheManager {
    private static final String PREFERENCES_NAME = "dr_prebid_qr_scan_cache";
    private final SharedPreferences mSharedPreferences;

    private static volatile QrCodeScanCacheManager sInstance;
    private static final Object mutex = new Object();

    public static QrCodeScanCacheManager getInstance(Context context) {
        QrCodeScanCacheManager result = sInstance;
        if (result == null) {
            synchronized (mutex) {
                result = sInstance;
                if (result == null) {
                    sInstance = result = new QrCodeScanCacheManager(context);
                }
            }
        }

        return result;
    }

    private QrCodeScanCacheManager(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void setCache(String qrCodeValue) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.Preferences.QR_CODE_SCAN_CACHE, qrCodeValue);
        editor.apply();
    }

    public boolean hasCache() {
        return mSharedPreferences.contains(Constants.Preferences.QR_CODE_SCAN_CACHE);
    }

    public String getCache() {
        String cache = mSharedPreferences.getString(Constants.Preferences.QR_CODE_SCAN_CACHE, "");
        clearCache();
        return cache;
    }

    public void clearCache() {
        mSharedPreferences.edit().remove(Constants.Preferences.QR_CODE_SCAN_CACHE).apply();
    }
}
