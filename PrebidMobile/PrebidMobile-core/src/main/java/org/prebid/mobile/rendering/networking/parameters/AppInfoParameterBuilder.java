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

import android.text.TextUtils;

import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.App;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;

import java.util.Map;
import java.util.Set;

public class AppInfoParameterBuilder extends ParameterBuilder {

    private AdUnitConfiguration adConfiguration;

    public AppInfoParameterBuilder(AdUnitConfiguration adConfiguration) {
        this.adConfiguration = adConfiguration;
    }

    @Override
    public void appendBuilderParameters(AdRequestInput adRequestInput) {
        App app = adRequestInput.getBidRequest().getApp();
        app.getPublisher().id = PrebidMobile.getPrebidServerAccountId();

        String appName = AppInfoManager.getAppName();
        if (Utils.isNotBlank(appName)) {
            app.name = appName;
        }

        String appVersion = AppInfoManager.getAppVersion();
        if (Utils.isNotBlank(appVersion)) {
            app.ver = appVersion;
        }

        String bundle = TargetingParams.getBundleName();
        if (Utils.isNotBlank(bundle)) {
            app.bundle = bundle;
        } else if (Utils.isNotBlank(AppInfoManager.getPackageName())) {
            app.bundle = AppInfoManager.getPackageName();
        }

        String storeUrl = TargetingParams.getStoreUrl();
        if (Utils.isNotBlank(storeUrl)) {
            app.storeurl = storeUrl;
        }

        String publisherName = TargetingParams.getPublisherName();
        if (Utils.isNotBlank(publisherName)) {
            app.getPublisher().name = publisherName;
        }

        String domain = TargetingParams.getDomain();
        if (Utils.isNotBlank(domain)) {
            app.domain = domain;
        }

        app.getExt().put("prebid", Prebid.getJsonObjectForApp(BasicParameterBuilder.DISPLAY_MANAGER_VALUE, PrebidMobile.SDK_VERSION));
        final Map<String, Set<String>> extDataDictionary = TargetingParams.getExtDataDictionary();
        if (!extDataDictionary.isEmpty()) {
            app.getExt().put("data", Utils.toJson(extDataDictionary));
        }
    }
}
