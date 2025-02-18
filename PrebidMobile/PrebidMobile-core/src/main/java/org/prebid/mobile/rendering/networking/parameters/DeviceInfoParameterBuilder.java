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

package org.prebid.mobile.rendering.networking.parameters;

import android.os.Build;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Device;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.utils.helpers.AdvertisingIdManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;

import java.util.Locale;

public class DeviceInfoParameterBuilder extends ParameterBuilder {

    static final String PLATFORM_VALUE = "Android";

    private AdUnitConfiguration adConfiguration;

    public DeviceInfoParameterBuilder(AdUnitConfiguration configuration) {
        adConfiguration = configuration;
    }

    @Override
    public void appendBuilderParameters(AdRequestInput adRequestInput) {
        DeviceInfoManager deviceManager = ManagersResolver.getInstance().getDeviceManager();
        if (deviceManager != null) {

            int screenWidth = deviceManager.getScreenWidth();
            int screenHeight = deviceManager.getScreenHeight();

            Device device = adRequestInput.getBidRequest().getDevice();

            device.pxratio = Utils.DENSITY;
            if (screenWidth > 0 && screenHeight > 0) {
                device.w = screenWidth;
                device.h = screenHeight;
            }

            String advertisingId = AdvertisingIdManager.getAdvertisingId(ManagersResolver.getInstance().getUserConsentManager());
            if (Utils.isNotBlank(advertisingId)) {
                device.ifa = advertisingId;
            }

            boolean tabletSize = deviceManager.isTablet();
            if (tabletSize) {
                device.devicetype = Device.DeviceType.TABLET.value;
            } else {
                device.devicetype = Device.DeviceType.SMARTPHONE.value;
            }

            device.make = Build.MANUFACTURER;
            device.model = Build.MODEL;
            device.os = PLATFORM_VALUE;
            device.osv = Build.VERSION.RELEASE;
            device.language = Locale.getDefault().getLanguage();
            device.ua = AppInfoManager.getUserAgent();

            // lmt and APP_ADVERTISING_ID_ENABLED are opposites
            boolean lmt = AdvertisingIdManager.isLimitedAdTrackingEnabled();
            device.lmt = lmt ? 1 : 0;

            final AdSize minSizePercentage = adConfiguration.getMinSizePercentage();
            if (minSizePercentage != null) {
                device.getExt().put("prebid", Prebid.getJsonObjectForDeviceMinSizePerc(minSizePercentage));
            }
        }
    }
}
