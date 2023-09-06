package org.prebid.mobile.rendering.sdk;

import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebSettings;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;

public class UserAgentFetcherTask {

    private static final String TAG = "UserAgentFetcherTask";

    private UserAgentFetcherTask() {
    }

    public static void run() {
        String userAgent = "";

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                userAgent = WebSettings.getDefaultUserAgent(PrebidContextHolder.getContext());
            }
        } catch (Exception any) {
            LogUtil.error(TAG, "Failed to get user agent");
        }

        if (TextUtils.isEmpty(userAgent) || userAgent.contains("UNAVAILABLE")) {
            userAgent = "Mozilla/5.0 (Linux; U; Android " + android.os.Build.VERSION.RELEASE + ";" + " " + getDeviceName() + ")";
        }

        AppInfoManager.setUserAgent(userAgent);
    }


    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

}
