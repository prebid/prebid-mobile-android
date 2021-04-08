package org.prebid.mobile.rendering.networking.parameters;

import android.os.Build;

import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Device;
import org.prebid.mobile.rendering.networking.targeting.Targeting;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.utils.helpers.AdIdManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;

import java.util.Locale;

public class DeviceInfoParameterBuilder extends ParameterBuilder {

    static final String PLATFORM_VALUE = "Android";

    private AdConfiguration mAdConfiguration;

    public DeviceInfoParameterBuilder(AdConfiguration configuration) {
        mAdConfiguration = configuration;
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

            String advertisingId = AdIdManager.getAdId();
            if (Utils.isNotBlank(advertisingId)) {
                device.ifa = advertisingId;
            }

            device.make = Build.MANUFACTURER;
            device.model = Build.MODEL;
            device.os = PLATFORM_VALUE;
            device.osv = Build.VERSION.RELEASE;
            device.language = Locale.getDefault().getLanguage();
            device.ua = AppInfoManager.getUserAgent();

            // lmt and APP_ADVERTISING_ID_ENABLED are opposites
            boolean lmt = AdIdManager.isLimitAdTrackingEnabled();
            device.lmt = lmt ? 1 : 0;
            device.carrier = Targeting.getCarrier();
            device.ip = Targeting.getDeviceIpAddress();

            final AdSize minSizePercentage = mAdConfiguration.getMinSizePercentage();
            if (minSizePercentage != null) {
                device.getExt().put("prebid", Prebid.getJsonObjectForDeviceMinSizePerc(minSizePercentage));
            }
        }
    }
}
