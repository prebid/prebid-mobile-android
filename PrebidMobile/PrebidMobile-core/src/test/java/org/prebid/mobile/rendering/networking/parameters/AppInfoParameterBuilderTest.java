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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.ContentObject;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.models.openrtb.BidRequest;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.App;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.rendering.utils.helpers.AdIdManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AppInfoParameterBuilderTest {

    private String APP_NAME = "app";
    private String APP_BUNDLE = "bundle";
    private String ADVERTISING_ID = "123";
    private boolean ADVERTISING_ID_ENABLED = true;

    @Before
    public void setUp() throws Exception {
        AppInfoManager.setAppName(APP_NAME);
        AppInfoManager.setPackageName(APP_BUNDLE);
        AdIdManager.setAdId(ADVERTISING_ID);
        AdIdManager.setLimitAdTrackingEnabled(!ADVERTISING_ID_ENABLED);
    }

    @Test
    public void testAppendBuilderParameters() throws Exception {
        TargetingParams.clearContextData();

        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        ContentObject contentObject = new ContentObject();
        contentObject.setUrl("test.com");
        adConfiguration.setAppContent(contentObject);
        AppInfoParameterBuilder builder = new AppInfoParameterBuilder(adConfiguration);
        AdRequestInput adRequestInput = new AdRequestInput();

        final String expectedStoreurl = "https://google.play.com";
        final String expectedPublisherName = "prebid";
        final String expectedDomain = "test_domain";

        TargetingParams.setPublisherName(expectedPublisherName);
        TargetingParams.setStoreUrl(expectedStoreurl);
        TargetingParams.setDomain(expectedDomain);

        builder.appendBuilderParameters(adRequestInput);

        BidRequest expectedBidRequest = new BidRequest();
        final App expectedApp = expectedBidRequest.getApp();
        expectedApp.name = APP_NAME;
        expectedApp.bundle = APP_BUNDLE;
        expectedApp.storeurl = expectedStoreurl;
        expectedApp.getPublisher().id = PrebidMobile.getPrebidServerAccountId();
        expectedApp.getPublisher().name = expectedPublisherName;
        expectedApp.domain = expectedDomain;
        expectedApp.getExt().put("prebid", Prebid.getJsonObjectForApp(BasicParameterBuilder.DISPLAY_MANAGER_VALUE, PrebidMobile.SDK_VERSION));
        ContentObject expectedContentObject = new ContentObject();
        expectedContentObject.setUrl("test.com");
        expectedApp.contentObject = expectedContentObject;

        assertEquals(
                expectedBidRequest.getJsonObject().toString(),
                adRequestInput.getBidRequest().getJsonObject().toString()
        );
    }

    @Test
    public void whenAppendParametersAndTargetingContextDataNotEmpty_ContextDataAddedToAppExt() throws JSONException {
        TargetingParams.addContextData("context", "contextData");

        AppInfoParameterBuilder builder = new AppInfoParameterBuilder(new AdUnitConfiguration());
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        Ext appExt = adRequestInput.getBidRequest().getApp().getExt();
        assertTrue(appExt.getMap().containsKey("data"));
        JSONObject appDataJson = (JSONObject) appExt.getMap().get("data");
        assertTrue(appDataJson.has("context"));
        assertEquals("contextData", appDataJson.getJSONArray("context").get(0));
    }

    @Test
    public void whenAppendParametersAndTargetingContextKeywordNotEmpty_ContextKeywordAddedToAppExt() throws JSONException {
        TargetingParams.addContextKeyword("contextKeyword1");
        TargetingParams.addContextKeyword("contextKeyword2");

        AppInfoParameterBuilder builder = new AppInfoParameterBuilder(new AdUnitConfiguration());
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        App app = adRequestInput.getBidRequest().getApp();

        assertEquals("contextKeyword1,contextKeyword2", app.keywords);

        JSONObject appJson = app.getJsonObject();
        assertTrue(appJson.has("keywords"));
        assertEquals("contextKeyword1,contextKeyword2", appJson.getString("keywords"));
    }

}
