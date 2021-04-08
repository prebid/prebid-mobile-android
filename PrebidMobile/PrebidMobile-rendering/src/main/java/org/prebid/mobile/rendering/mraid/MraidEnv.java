package org.prebid.mobile.rendering.mraid;

import androidx.annotation.NonNull;

import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.rendering.utils.helpers.AdIdManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;

public class MraidEnv {

    private MraidEnv() {

    }

    @NonNull
    public static String getWindowMraidEnv() {
        return "window.MRAID_ENV = {"
               + getStringPropertyWithSeparator("version", PrebidRenderingSettings.MRAID_VERSION)
               + getStringPropertyWithSeparator("sdk", PrebidRenderingSettings.SDK_NAME)
               + getStringPropertyWithSeparator("sdkVersion", PrebidRenderingSettings.SDK_VERSION)
               + getStringPropertyWithSeparator("appId", AppInfoManager.getPackageName())
               + getStringPropertyWithSeparator("ifa", AdIdManager.getAdId())
               + getBooleanPropertyWithSeparator("limitAdTracking", AdIdManager.isLimitAdTrackingEnabled(), ",")
               + getBooleanPropertyWithSeparator("coppa", PrebidRenderingSettings.isCoppaEnabled, "")
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
