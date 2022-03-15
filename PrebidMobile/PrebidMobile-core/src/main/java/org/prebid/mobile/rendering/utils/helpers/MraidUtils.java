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

package org.prebid.mobile.rendering.utils.helpers;

import android.text.TextUtils;
import org.prebid.mobile.rendering.sdk.ManagersResolver;

public class MraidUtils {

    public static boolean isFeatureSupported(String feature) {
        if (TextUtils.isEmpty(feature)) {
            return false;
        }

        switch (feature) {
            case "sms":
            case "tel":
                return ManagersResolver.getInstance().getDeviceManager().hasTelephony();
            case "calendar":
                return true;
            case "storePicture":
                return ManagersResolver.getInstance().getDeviceManager().canStorePicture();
            case "inlineVideo":
                return true;
            case "location":
                return ManagersResolver.getInstance().getDeviceManager().hasGps();
            case "vpaid":
                return false;
            default:
                return false;
        }
    }
}
