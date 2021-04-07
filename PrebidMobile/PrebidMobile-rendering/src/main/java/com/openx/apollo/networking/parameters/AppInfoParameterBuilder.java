package com.openx.apollo.networking.parameters;

import com.openx.apollo.bidding.data.bid.Prebid;
import com.openx.apollo.models.openrtb.bidRequests.App;
import com.openx.apollo.networking.targeting.Targeting;
import com.openx.apollo.sdk.ApolloSettings;
import com.openx.apollo.utils.helpers.AppInfoManager;
import com.openx.apollo.utils.helpers.Utils;

import java.util.Map;
import java.util.Set;

public class AppInfoParameterBuilder extends ParameterBuilder {

    @Override
    public void appendBuilderParameters(AdRequestInput adRequestInput) {
        App app = adRequestInput.getBidRequest().getApp();
        app.getPublisher().id = ApolloSettings.getAccountId();

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

        app.getExt().put("prebid", Prebid.getJsonObjectForApp(BasicParameterBuilder.DISPLAY_MANAGER_VALUE, ApolloSettings.SDK_VERSION));
        final Map<String, Set<String>> contextDataDictionary = Targeting.getContextDataDictionary();
        if (!contextDataDictionary.isEmpty()) {
            app.getExt().put("data", Utils.toJson(contextDataDictionary));
        }
    }
}
