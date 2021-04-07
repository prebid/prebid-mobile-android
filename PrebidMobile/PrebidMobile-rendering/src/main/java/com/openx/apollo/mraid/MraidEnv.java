package com.openx.apollo.mraid;

import androidx.annotation.NonNull;

import com.openx.apollo.sdk.ApolloSettings;
import com.openx.apollo.utils.helpers.AdIdManager;
import com.openx.apollo.utils.helpers.AppInfoManager;

public class MraidEnv {

    private MraidEnv() {

    }

    @NonNull
    public static String getWindowMraidEnv() {
        return "window.MRAID_ENV = {"
               + getStringPropertyWithSeparator("version", ApolloSettings.MRAID_VERSION)
               + getStringPropertyWithSeparator("sdk", ApolloSettings.SDK_NAME)
               + getStringPropertyWithSeparator("sdkVersion", ApolloSettings.SDK_VERSION)
               + getStringPropertyWithSeparator("appId", AppInfoManager.getPackageName())
               + getStringPropertyWithSeparator("ifa", AdIdManager.getAdId())
               + getBooleanPropertyWithSeparator("limitAdTracking", AdIdManager.isLimitAdTrackingEnabled(), ",")
               + getBooleanPropertyWithSeparator("coppa", ApolloSettings.isCoppaEnabled, "")
               + "};";
    }

    static String getStringPropertyWithSeparator(String propertyName, String propertyValue) {
        String separator = ",";
        return String.format("%s: \"%s\"%s", propertyName, propertyValue, separator);
    }

    static String getBooleanPropertyWithSeparator(String propertyName, boolean propertyValue, String separator) {
        return String.format("%s: %s%s", propertyName, propertyValue, separator);
    }
}
