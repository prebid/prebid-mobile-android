package org.prebid.mobile.rendering.networking.parameters;

import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.App;
import org.prebid.mobile.rendering.networking.targeting.Targeting;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;

import java.util.Map;
import java.util.Set;

public class AppInfoParameterBuilder extends ParameterBuilder {

    @Override
    public void appendBuilderParameters(AdRequestInput adRequestInput) {
        App app = adRequestInput.getBidRequest().getApp();
        app.getPublisher().id = PrebidRenderingSettings.getAccountId();

        String appName = AppInfoManager.getAppName();
        if (Utils.isNotBlank(appName)) {
            app.name = appName;
        }

        String appVersion = AppInfoManager.getAppVersion();
        if (Utils.isNotBlank(appVersion)) {
            app.ver = appVersion;
        }

        String bundle = AppInfoManager.getPackageName();
        if (Utils.isNotBlank(bundle)) {
            app.bundle = bundle;
        }

        String storeUrl = Targeting.getAppStoreMarketUrl();
        if (Utils.isNotBlank(storeUrl)) {
            app.storeurl = storeUrl;
        }

        String publisherName = Targeting.getPublisherName();
        if (Utils.isNotBlank(publisherName)) {
            app.getPublisher().name = publisherName;
        }

        app.getExt().put("prebid", Prebid.getJsonObjectForApp(BasicParameterBuilder.DISPLAY_MANAGER_VALUE, PrebidRenderingSettings.SDK_VERSION));
        final Map<String, Set<String>> contextDataDictionary = Targeting.getContextDataDictionary();
        if (!contextDataDictionary.isEmpty()) {
            app.getExt().put("data", Utils.toJson(contextDataDictionary));
        }
    }
}
