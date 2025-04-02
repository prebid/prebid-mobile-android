/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.mraid;

import androidx.annotation.NonNull;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.utils.helpers.AdvertisingIdManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;

public class MraidEnv {

    private MraidEnv() {

    }

    @NonNull
    public static String getWindowMraidEnv() {
        return "window.MRAID_ENV = {"
                + getStringPropertyWithSeparator("version", PrebidMobile.MRAID_VERSION)
                + getStringPropertyWithSeparator("sdk", PrebidMobile.SDK_NAME)
                + getStringPropertyWithSeparator("sdkVersion", PrebidMobile.SDK_VERSION)
                + getStringPropertyWithSeparator("appId", AppInfoManager.getPackageName())
                + getStringPropertyWithSeparator("ifa", AdvertisingIdManager.getAdvertisingId(ManagersResolver.getInstance().getUserConsentManager()))
                + getBooleanPropertyWithSeparator("limitAdTracking", AdvertisingIdManager.isLimitedAdTrackingEnabled(), ",")
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
