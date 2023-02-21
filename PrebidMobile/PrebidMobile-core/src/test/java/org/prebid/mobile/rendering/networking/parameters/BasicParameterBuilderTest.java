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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.prebid.mobile.rendering.networking.parameters.BasicParameterBuilder.KEY_OM_PARTNER_NAME;
import static org.prebid.mobile.rendering.networking.parameters.BasicParameterBuilder.KEY_OM_PARTNER_VERSION;
import static org.prebid.mobile.rendering.networking.parameters.BasicParameterBuilder.VIDEO_INTERSTITIAL_PLAYBACK_END;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.DataObject;
import org.prebid.mobile.ExternalUserId;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.Signals;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.VideoBaseAdUnit;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.models.PlacementType;
import org.prebid.mobile.rendering.models.openrtb.BidRequest;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Imp;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.User;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.devices.Geo;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Banner;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Video;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.pmps.Format;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.source.Source;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, qualifiers = "w1920dp-h1080dp")
public class BasicParameterBuilderTest {
    private static final int VIDEO_INTERSTITIAL_PLACEMENT = 5;
    private static final int USER_AGE = 20;
    private static final int USER_YOB = Calendar.getInstance().get(Calendar.YEAR) - USER_AGE;
    private static final float USER_LAT = 1;
    private static final float USER_LON = 0;
    private static final String USER_ID = "id";
    private static final String USER_KEYWORDS = "keywords";
    private static final String USER_CUSTOM = "custom";
    private static final String USER_GENDER = "M";
    private static final String USER_BUYER_ID = "bid";

    private Context context;

    private final boolean browserActivityAvailable = true;

    @Before
    public void setUp() throws Exception {
        context = Robolectric.buildActivity(Activity.class).create().get();
        org.prebid.mobile.PrebidMobile.initializeSdk(context, null);
        ManagersResolver.getInstance().prepare(context);
    }

    @After
    public void cleanup() throws Exception {
        TargetingParams.clearUserData();
        TargetingParams.clearUserKeywords();
        TargetingParams.setUserLatLng(null, null);
        TargetingParams.setGender(TargetingParams.GENDER.UNKNOWN);
        TargetingParams.clearStoredExternalUserIds();
        TargetingParams.setBuyerId(null);
        TargetingParams.setUserId(null);
        TargetingParams.setUserCustomData(null);
        TargetingParams.setYearOfBirth(0);
        TargetingParams.setOmidPartnerName(null);
        TargetingParams.setOmidPartnerVersion(null);

        PrebidMobile.sendMraidSupportParams = true;
        PrebidMobile.useExternalBrowser = false;
        PrebidMobile.isCoppaEnabled = false;
        PrebidMobile.clearStoredBidResponses();
        PrebidMobile.setStoredAuctionResponse(null);
    }

    @Test
    public void sourceOmidValues_originalApi_customValues() throws JSONException {
        AdUnitConfiguration config = new AdUnitConfiguration();
        config.setAdFormat(AdFormat.BANNER);
        config.setIsOriginalAdUnit(true);

        TargetingParams.setOmidPartnerName("testOmidValue");
        TargetingParams.setOmidPartnerVersion("testOmidVersion");

        BasicParameterBuilder builder = new BasicParameterBuilder(config,
            context.getResources(),
            browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        Source source = adRequestInput.getBidRequest().getSource();
        assertEquals(adRequestInput.getBidRequest().getId(), source.getTid());

        JSONObject sourceExtJson = source.getJsonObject().getJSONObject("ext");
        assertEquals("testOmidValue", sourceExtJson.getString("omidpn"));
        assertEquals("testOmidVersion", sourceExtJson.getString("omidpv"));
    }

    @Test
    public void sourceOmidValues_originalApi_emptyValues() throws JSONException {
        AdUnitConfiguration config = new AdUnitConfiguration();
        config.setAdFormat(AdFormat.BANNER);
        config.setIsOriginalAdUnit(true);

        BasicParameterBuilder builder = new BasicParameterBuilder(config,
            context.getResources(),
            browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        Source source = adRequestInput.getBidRequest().getSource();
        assertEquals(adRequestInput.getBidRequest().getId(), source.getTid());

        assertFalse(source.getJsonObject().has("ext"));
    }

    @Test
    public void sourceOmidValues_renderingApi_customValues() throws JSONException {
        AdUnitConfiguration config = new AdUnitConfiguration();
        config.setAdFormat(AdFormat.BANNER);
        config.setIsOriginalAdUnit(false);

        TargetingParams.setOmidPartnerName("testOmidValue");
        TargetingParams.setOmidPartnerVersion("testOmidVersion");

        BasicParameterBuilder builder = new BasicParameterBuilder(config,
            context.getResources(),
            browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        Source source = adRequestInput.getBidRequest().getSource();
        assertEquals(adRequestInput.getBidRequest().getId(), source.getTid());

        JSONObject sourceExtJson = source.getJsonObject().getJSONObject("ext");
        assertEquals("testOmidValue", sourceExtJson.getString("omidpn"));
        assertEquals("testOmidVersion", sourceExtJson.getString("omidpv"));
    }

    @Test
    public void sourceOmidValues_renderingApi_defaultValues() throws JSONException {
        AdUnitConfiguration config = new AdUnitConfiguration();
        config.setAdFormat(AdFormat.BANNER);
        config.setIsOriginalAdUnit(false);

        BasicParameterBuilder builder = new BasicParameterBuilder(config,
            context.getResources(),
            browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        Source source = adRequestInput.getBidRequest().getSource();
        assertEquals(adRequestInput.getBidRequest().getId(), source.getTid());

        JSONObject sourceExtJson = source.getJsonObject().getJSONObject("ext");
        assertEquals("Prebid", sourceExtJson.getString("omidpn"));
        assertEquals(PrebidMobile.SDK_VERSION, sourceExtJson.getString("omidpv"));
    }

    @Test
    public void whenAppendParametersAndBannerType_ImpWithValidBannerObject() throws JSONException {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));
        adConfiguration.setPbAdSlot("12345");
        PrebidMobile.addStoredBidResponse("bidderTest", "123456");
        PrebidMobile.setStoredAuctionResponse("storedResponse");

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration,
                context.getResources(),
                browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        BidRequest actualBidRequest = adRequestInput.getBidRequest();
        BidRequest expectedBidRequest = getExpectedBidRequest(adConfiguration, actualBidRequest.getId());

        assertEquals(expectedBidRequest.getJsonObject().toString(), actualBidRequest.getJsonObject().toString());
        Imp actualImp = actualBidRequest.getImp().get(0);
        assertNotNull(actualImp.banner);
        assertTrue(actualImp.banner.getFormats().containsAll(expectedBidRequest.getImp().get(0).banner.getFormats()));
        assertNull(actualImp.video);
        assertEquals(1, actualImp.secure.intValue());
        assertEquals(0, actualImp.instl.intValue());
    }

    @Test
    public void whenAppendParametersAndBInterstitialType_ImpWithValidBannerObject()
    throws JSONException {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.INTERSTITIAL);
        adConfiguration.setAdPosition(AdPosition.FULLSCREEN);

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration,
                context.getResources(),
                browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        BidRequest actualBidRequest = adRequestInput.getBidRequest();
        BidRequest expectedBidRequest = getExpectedBidRequest(adConfiguration, actualBidRequest.getId());

        assertEquals(expectedBidRequest.getJsonObject().toString(), actualBidRequest.getJsonObject().toString());
        Imp actualImp = actualBidRequest.getImp().get(0);
        assertNotNull(actualImp.banner);
        Format expectedFormat = new Format(1920, 1080);
        assertTrue(actualImp.banner.getFormats().contains(expectedFormat));
        assertNull(actualImp.video);
        assertEquals(1, actualImp.secure.intValue());
        assertEquals(1, actualImp.instl.intValue());
    }

    @Test
    public void whenAppendParametersAndVastWithoutPlacementType_ImpWithValidVideoObject()
    throws JSONException {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.VAST);
        adConfiguration.setAdPosition(AdPosition.FULLSCREEN);

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration,
                context.getResources(),
                browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        BidRequest actualBidRequest = adRequestInput.getBidRequest();
        BidRequest expectedBidRequest = getExpectedBidRequest(adConfiguration, actualBidRequest.getId());

        assertEquals(expectedBidRequest.getJsonObject().toString(), actualBidRequest.getJsonObject().toString());
        Imp actualImp = actualBidRequest.getImp().get(0);
        assertNotNull(actualImp.video);
        assertNull(actualImp.banner);
        assertNull(actualImp.secure);
        assertEquals(1920, actualImp.video.w.intValue());
        assertEquals(1080, actualImp.video.h.intValue());
        assertEquals(VIDEO_INTERSTITIAL_PLACEMENT, actualImp.video.placement.intValue());
        assertEquals(1, actualImp.instl.intValue());
    }

    @Test
    public void whenAppendParametersAndVastWithPlacementType_ImpWithValidVideoObject()
    throws JSONException {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.VAST);
        adConfiguration.setPlacementType(PlacementType.IN_BANNER);
        adConfiguration.setAdPosition(AdPosition.FULLSCREEN);
        adConfiguration.addSize(new AdSize(300, 250));

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration,
                context.getResources(),
                browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        BidRequest actualBidRequest = adRequestInput.getBidRequest();
        BidRequest expectedBidRequest = getExpectedBidRequest(adConfiguration, actualBidRequest.getId());

        assertEquals(expectedBidRequest.getJsonObject().toString(), actualBidRequest.getJsonObject().toString());
        Imp actualImp = actualBidRequest.getImp().get(0);
        assertNotNull(actualImp.video);
        assertNull(actualImp.banner);
        assertNull(actualImp.secure);
        assertEquals(1, actualImp.instl.intValue());
        assertEquals(300, actualImp.video.w.intValue());
        assertEquals(250, actualImp.video.h.intValue());
        assertNotEquals(VIDEO_INTERSTITIAL_PLACEMENT, actualImp.video.placement.intValue());
    }

    @Test
    public void whenAppendParametersAndCoppaTrue_CoppaEqualsOne() {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        PrebidMobile.isCoppaEnabled = true;

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration,
                context.getResources(),
                browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        BidRequest actualBidRequest = adRequestInput.getBidRequest();
        assertEquals(1, actualBidRequest.getRegs().coppa.intValue());
    }

    @Test
    public void whenAppendParametersAndCoppaFalse_CoppaNull() {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration,
                context.getResources(),
                browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        BidRequest actualBidRequest = adRequestInput.getBidRequest();
        assertNull(actualBidRequest.getRegs().coppa);
    }

    @Test
    public void whenAppendParametersAndTargetingParamsWereSet_TargetingParamsWereAppend()
    throws JSONException {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        TargetingParams.setUserId(USER_ID);
        TargetingParams.setUserAge(USER_AGE);
        TargetingParams.addUserKeyword(USER_KEYWORDS);
        TargetingParams.setUserCustomData(USER_CUSTOM);
        TargetingParams.setGender(TargetingParams.GENDER.MALE);
        TargetingParams.setBuyerId(USER_BUYER_ID);
        TargetingParams.setUserExt(new Ext());
        TargetingParams.setUserLatLng(USER_LAT, USER_LON);

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration,
                context.getResources(),
            browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        User actualUser = adRequestInput.getBidRequest().getUser();
        User expectedUser = getExpectedUser();
        assertEquals(expectedUser.getJsonObject().toString(), actualUser.getJsonObject().toString());
    }

    @Test
    public void appendContextKeywordsAndData() throws JSONException {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        adConfiguration.addExtKeyword("adUnitContextKeyword1");
        adConfiguration.addExtKeyword("adUnitContextKeyword2");

        adConfiguration.addExtData("adUnitContextDataKey1", "adUnitContextDataValue1");
        adConfiguration.addExtData("adUnitContextDataKey2", "adUnitContextDataValue2");

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration,
            context.getResources(),
            browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        BidRequest actualRequest = adRequestInput.getBidRequest();
        BidRequest expectedRequest = getExpectedBidRequest(adConfiguration, actualRequest.getId());
        assertEquals(expectedRequest.getJsonObject().toString(), actualRequest.getJsonObject().toString());
    }

    @Test
    public void appendTargetingContextKeywords() throws JSONException {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        TargetingParams.addContextKeyword("contextKeyword1");
        TargetingParams.addContextKeyword("contextKeyword2");

        AppInfoParameterBuilder builder = new AppInfoParameterBuilder(adConfiguration);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        JSONObject appJson = adRequestInput.getBidRequest().getApp().getJsonObject();
        assertTrue(appJson.has("keywords"));
        assertEquals("contextKeyword1,contextKeyword2", appJson.getString("keywords"));
    }

    @Test
    public void whenAppendParametersAndSendMraidSupportParamsFalse_NoMraidApi() {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        PrebidMobile.sendMraidSupportParams = false;

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration,
                context.getResources(),
                browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        Imp actualImp = adRequestInput.getBidRequest().getImp().get(0);
        assertEquals(Arrays.toString(new int[]{7}), Arrays.toString(actualImp.banner.api));
    }

    @Test
    public void whenAppendParametersAndUseExternalBrowserFalseAndBrowserActivityAvailable_ClickBrowserEqualsZero() {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        PrebidMobile.useExternalBrowser = false;

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration,
                context.getResources(),
                browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        Imp actualImp = adRequestInput.getBidRequest().getImp().get(0);
        assertEquals(0, actualImp.clickBrowser.intValue());
    }

    @Test
    public void whenAppendParametersAndUseExternalBrowserTrueAndBrowserActivityAvailable_ClickBrowserEqualsOne() {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        PrebidMobile.useExternalBrowser = true;

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration,
                context.getResources(),
                browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        Imp actualImp = adRequestInput.getBidRequest().getImp().get(0);
        assertEquals(1, actualImp.clickBrowser.intValue());
    }

    @Test
    public void whenAppendParametersAndUseExternalBrowserFalseAndBrowserActivityNotAvailable_ClickBrowserEqualsOne() {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        PrebidMobile.useExternalBrowser = false;

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, context.getResources(), false);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        Imp actualImp = adRequestInput.getBidRequest().getImp().get(0);
        assertEquals(1, actualImp.clickBrowser.intValue());
    }

    @Test
    public void whenAppendParametersAndTargetingAccessControlNotEmpty_BiddersAddedToExt()
    throws JSONException {
        TargetingParams.addBidderToAccessControlList("bidder");

        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setConfigId("config");
        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, context.getResources(), false);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        BidRequest bidRequest = adRequestInput.getBidRequest();
        JSONObject prebidJson = bidRequest.getExt().getJsonObject().getJSONObject("prebid");
        assertTrue(prebidJson.has("data"));
        JSONArray biddersArray = prebidJson.getJSONObject("data").getJSONArray("bidders");
        assertEquals("bidder", biddersArray.get(0));
    }

    @Test
    public void whenAppendParametersAndTargetingUserDataNotEmpty_UserDataAddedToUserExt()
            throws JSONException {
        TargetingParams.addUserData("user", "userData");

        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setConfigId("config");
        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, context.getResources(), false);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        User user = adRequestInput.getBidRequest().getUser();
        assertTrue(user.getExt().getMap().containsKey("data"));
        JSONObject userDataJson = (JSONObject) user.getExt().getMap().get("data");
        assertTrue(userDataJson.has("user"));
        assertEquals("userData", userDataJson.getJSONArray("user").get(0));
    }

    @Test
    public void whenAppendUserData_UserDataAddedToUser()
            throws JSONException {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setConfigId("config");
        DataObject dataObject = new DataObject();
        String testName = "testDataObject";
        dataObject.setName(testName);
        adConfiguration.addUserData(dataObject);
        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, context.getResources(), false);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        User user = adRequestInput.getBidRequest().getUser();
        assertEquals(1, user.dataObjects.size());
        JSONObject jsonUser = user.getJsonObject();
        assertTrue(jsonUser.has("data"));
        JSONArray jsonData = jsonUser.getJSONArray("data");
        JSONObject jsonDataObject = jsonData.getJSONObject(0);
        assertTrue(jsonDataObject.has("name"));
        String dataName = jsonDataObject.getString("name");
        assertEquals(testName, dataName);
    }

    @Test
    public void whenAppendParametersAndAdConfigContextDataNotEmpty_ContextDataAddedToImpExt()
    throws JSONException {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.addExtData("context", "contextData");
        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, context.getResources(), false);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext impExt = adRequestInput.getBidRequest().getImp().get(0).getExt();

        Map<String, Object> impExtMap = impExt.getMap();
        assertTrue(impExtMap.containsKey("data"));

        JSONObject dataJson = (JSONObject) impExtMap.get("data");
        assertTrue(dataJson.has("context"));
        assertEquals("contextData", dataJson.getJSONArray("context").get(0));
    }

    @Test
    public void testMultiFormatAdUnit_bannerAndVideoObjectsAreNotNull() {
        AdUnitConfiguration configuration = new AdUnitConfiguration();
        configuration.setAdFormats(EnumSet.of(AdUnitFormat.DISPLAY, AdUnitFormat.VIDEO));

        BasicParameterBuilder builder = new BasicParameterBuilder(configuration, null, false);

        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        BidRequest bidRequest = adRequestInput.getBidRequest();
        Imp firstImp = bidRequest.getImp().iterator().next();

        assertNotNull(firstImp);

        assertNull(firstImp.nativeObj);
        assertNotNull(firstImp.banner);
        assertNotNull(firstImp.video);
    }

    @Test
    public void testNativeAdUnit_nativeObjectIsNotNull() {
        AdUnitConfiguration configuration = new AdUnitConfiguration();
        configuration.addAdFormat(AdFormat.NATIVE);

        BasicParameterBuilder builder = new BasicParameterBuilder(configuration, null, false);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        BidRequest bidRequest = adRequestInput.getBidRequest();
        Imp firstImp = bidRequest.getImp().iterator().next();

        assertNotNull(firstImp);

        assertNotNull(firstImp.nativeObj);
        assertNull(firstImp.banner);
        assertNull(firstImp.video);
    }

    @Test
    public void testOriginalApiVideoParameters_empty() {
        AdUnitConfiguration configuration = new AdUnitConfiguration();
        configuration.setIsOriginalAdUnit(true);
        configuration.setAdFormat(AdFormat.VAST);

        BasicParameterBuilder builder = new BasicParameterBuilder(
            configuration,
            context.getResources(),
            browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);
        BidRequest bidRequest = adRequestInput.getBidRequest();
        Imp imp = bidRequest.getImp().iterator().next();

        Video video = imp.getVideo();
        assertNotNull(video);
        assertNotNull(video.w);
        assertNotNull(video.h);
        assertNotNull(video.delivery);

        assertNull(video.placement);
        assertNull(video.mimes);
        assertNull(video.minduration);
        assertNull(video.maxduration);
        assertNull(video.protocols);
        assertNull(video.api);
        assertNull(video.linearity);
        assertNull(video.minbitrate);
        assertNull(video.maxbitrate);
        assertNull(video.playbackmethod);
        assertNull(video.pos);
        assertNull(video.playbackend);
        assertNull(video.startDelay);
    }

    @Test
    public void testOriginalApiVideoParameters_full() {
        AdUnitConfiguration configuration = new AdUnitConfiguration();
        configuration.setIsOriginalAdUnit(true);
        configuration.setAdFormat(AdFormat.VAST);
        configuration.setVideoParameters(createFullVideoParameters());

        BasicParameterBuilder builder = new BasicParameterBuilder(
            configuration,
            context.getResources(),
            browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);
        BidRequest bidRequest = adRequestInput.getBidRequest();
        Imp imp = bidRequest.getImp().iterator().next();

        Video video = imp.getVideo();
        assertNotNull(video);
        assertNotNull(video.w);
        assertNotNull(video.h);
        assertNotNull(video.delivery);
        assertEquals(new Integer(2), video.placement);

        assertEquals(new Integer(101), video.minduration);
        assertEquals(new Integer(102), video.maxduration);
        assertEquals(new Integer(201), video.minbitrate);
        assertEquals(new Integer(202), video.maxbitrate);
        assertEquals(new Integer(0), video.startDelay);
        assertEquals(new Integer(1), video.linearity);
        assertArrayEquals(new String[]{"Mime1", "Mime2"}, video.mimes);
        assertArrayEquals(new int[]{11, 12}, video.protocols);
        assertArrayEquals(new int[]{21, 22}, video.api);
        assertArrayEquals(new int[]{31, 32}, video.playbackmethod);

        assertNull(video.pos);
        assertNull(video.playbackend);
    }

    @Test
    public void testRenderingApiVideoParameters_empty() {
        AdUnitConfiguration configuration = new AdUnitConfiguration();
        configuration.setIsOriginalAdUnit(false);
        configuration.setAdFormat(AdFormat.VAST);

        BasicParameterBuilder builder = new BasicParameterBuilder(
            configuration,
            context.getResources(),
            browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);
        BidRequest bidRequest = adRequestInput.getBidRequest();
        Imp imp = bidRequest.getImp().iterator().next();

        Video video = imp.getVideo();
        assertNotNull(video);
        assertNotNull(video.w);
        assertNotNull(video.h);
        assertEquals(new Integer(5), video.placement);
        assertEquals(new Integer(1), video.linearity);
        assertEquals(new Integer(2), video.playbackend);
        assertArrayEquals(new String[]{"video/mp4", "video/3gpp", "video/webm", "video/mkv"}, video.mimes);
        assertArrayEquals(new int[]{2, 5}, video.protocols);
        assertArrayEquals(new int[]{3}, video.delivery);

        assertNull(video.minduration);
        assertNull(video.maxduration);
        assertNull(video.api);
        assertNull(video.minbitrate);
        assertNull(video.maxbitrate);
        assertNull(video.playbackmethod);
        assertNull(video.pos);
        assertNull(video.startDelay);
    }


    @Test
    public void testRenderingApiVideoParameters_full() {
        AdUnitConfiguration configuration = new AdUnitConfiguration();
        configuration.setIsOriginalAdUnit(false);
        configuration.setAdFormat(AdFormat.VAST);
        configuration.setVideoParameters(createFullVideoParameters());

        BasicParameterBuilder builder = new BasicParameterBuilder(
            configuration,
            context.getResources(),
            browserActivityAvailable
        );
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);
        BidRequest bidRequest = adRequestInput.getBidRequest();
        Imp imp = bidRequest.getImp().iterator().next();

        Video video = imp.getVideo();
        assertNotNull(video);
        assertNotNull(video.w);
        assertNotNull(video.h);
        assertEquals(new Integer(5), video.placement);

        assertEquals(new Integer(1), video.linearity);
        assertEquals(new Integer(2), video.playbackend);
        assertArrayEquals(new int[]{3}, video.delivery);
        assertArrayEquals(new String[]{"video/mp4", "video/3gpp", "video/webm", "video/mkv"}, video.mimes);
        assertArrayEquals(new int[]{2, 5}, video.protocols);

        assertNull(video.minduration);
        assertNull(video.maxduration);
        assertNull(video.api);
        assertNull(video.minbitrate);
        assertNull(video.maxbitrate);
        assertNull(video.playbackmethod);
        assertNull(video.pos);
        assertNull(video.startDelay);
    }


    private VideoBaseAdUnit.Parameters createFullVideoParameters() {
        VideoBaseAdUnit.Parameters parameters = new VideoBaseAdUnit.Parameters();

        parameters.setMinDuration(101);
        parameters.setMaxDuration(102);
        parameters.setMinBitrate(201);
        parameters.setMaxBitrate(202);
        parameters.setPlacement(Signals.Placement.InBanner);
        parameters.setLinearity(1);
        parameters.setStartDelay(Signals.StartDelay.PreRoll);

        ArrayList<String> mimes = new ArrayList<>(2);
        mimes.add("Mime1");
        mimes.add("Mime2");
        parameters.setMimes(mimes);

        ArrayList<Signals.Protocols> protocols = new ArrayList<>(2);
        protocols.add(new Signals.Protocols(11));
        protocols.add(new Signals.Protocols(12));
        parameters.setProtocols(protocols);

        ArrayList<Signals.Api> api = new ArrayList<>(2);
        api.add(new Signals.Api(21));
        api.add(new Signals.Api(22));
        parameters.setApi(api);

        ArrayList<Signals.PlaybackMethod> playbackMethods = new ArrayList<>(2);
        playbackMethods.add(new Signals.PlaybackMethod(31));
        playbackMethods.add(new Signals.PlaybackMethod(32));
        parameters.setPlaybackMethod(playbackMethods);

        return parameters;
    }

    private BidRequest getExpectedBidRequest(
        AdUnitConfiguration adConfiguration,
        String uuid
    ) {
        BidRequest bidRequest = new BidRequest();
        bidRequest.setId(uuid);
        boolean isVideo = adConfiguration.isAdType(AdFormat.VAST);
        bidRequest.getExt()
            .put("prebid", Prebid.getJsonObjectForBidRequest(PrebidMobile.getPrebidServerAccountId(), isVideo, false));
        //if coppaEnabled - set 1, else No coppa is sent
        if (PrebidMobile.isCoppaEnabled) {
            bidRequest.getRegs().coppa = 1;
        }

        Imp imp = getExpectedImp(adConfiguration, uuid);
        bidRequest.getImp().add(imp);

        Source source = bidRequest.getSource();
        source.setTid(uuid);
        source.getExt().put(KEY_OM_PARTNER_NAME, OmAdSessionManager.PARTNER_NAME);
        source.getExt().put(KEY_OM_PARTNER_VERSION, OmAdSessionManager.PARTNER_VERSION);

        bidRequest.getUser();

        return bidRequest;
    }

    private Imp getExpectedImp(AdUnitConfiguration adConfiguration, String uuid) {
        Imp imp = new Imp();

        imp.displaymanager = BasicParameterBuilder.DISPLAY_MANAGER_VALUE;
        imp.displaymanagerver = PrebidMobile.SDK_VERSION;

        if (!adConfiguration.isAdType(AdFormat.VAST)) {
            imp.secure = 1;
        }
        //Send 1 for interstitial/interstitial video and 0 for banners
        boolean isInterstitial = adConfiguration.isAdType(AdFormat.VAST) ||
                adConfiguration.isAdType(AdFormat.INTERSTITIAL);
        imp.instl = isInterstitial ? 1 : 0;

        // 0 == embedded, 1 == native
        imp.clickBrowser = !PrebidMobile.useExternalBrowser && browserActivityAvailable ? 0 : 1;
        imp.id = uuid;
        imp.getExt().put("prebid", Prebid.getJsonObjectForImp(adConfiguration));

        if (adConfiguration.isAdType(AdFormat.VAST)) {
            imp.video = getExpectedVideoImpValues(imp, adConfiguration);
        } else {
            imp.banner = getExpectedBannerImpValues(imp, adConfiguration);
        }

        final String pbAdSlot = adConfiguration.getPbAdSlot();
        if (pbAdSlot != null) {
            JSONObject data = new JSONObject();
            Utils.addValue(data, "adslot", pbAdSlot);
            imp.getExt().put("data", data);
        }

        appendImpExtParameters(imp, adConfiguration);

        return imp;
    }

    private void appendImpExtParameters(Imp imp, AdUnitConfiguration config) {
        try {
            Map<String, Set<String>> contextDataDictionary = config.getExtDataDictionary();
            if (contextDataDictionary.size() > 0) {
                JSONObject dataJson = new JSONObject();
                for (String key : contextDataDictionary.keySet()) {
                    dataJson.put(key, new JSONArray(contextDataDictionary.get(key)));
                }
                imp.getExt().put("data", dataJson);
            }

            Set<String> contextKeywordsSet = config.getExtKeywordsSet();
            if (contextKeywordsSet.size() > 0) {
                String join = stringsToCommaSeparatedString(contextKeywordsSet);
                imp.getExt().put("keywords", join);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String stringsToCommaSeparatedString(Set<String> strings) {
        if (strings.size() == 0) {
            return "";
        }

        int index = 0;
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            if (index != 0) {
                builder.append(",");
            }

            builder.append(string);
            index++;
        }

        return builder.toString();
    }

    private Banner getExpectedBannerImpValues(Imp imp, AdUnitConfiguration adConfiguration) {
        Banner banner = new Banner();
        banner.api = new int[]{3, 5, 6, 7};

        if (adConfiguration.isAdType(AdFormat.BANNER)) {
            for (AdSize size : adConfiguration.getSizes()) {
                banner.addFormat(size.getWidth(), size.getHeight());
            }
        } else if (adConfiguration.isAdType(AdFormat.INTERSTITIAL)) {
            Configuration deviceConfiguration = context.getResources().getConfiguration();
            banner.addFormat(deviceConfiguration.screenWidthDp,
                    deviceConfiguration.screenHeightDp);
        }

        if (adConfiguration.isAdPositionValid()) {
            banner.pos = adConfiguration.getAdPositionValue();
        }
        return banner;
    }

    private Video getExpectedVideoImpValues(Imp imp, AdUnitConfiguration adConfiguration) {
        Video video = new Video();
        //Common values for all video reqs
        video.mimes = BasicParameterBuilder.SUPPORTED_VIDEO_MIME_TYPES;
        video.protocols = BasicParameterBuilder.SUPPORTED_VIDEO_PROTOCOLS;
        video.linearity = BasicParameterBuilder.VIDEO_LINEARITY_LINEAR;

        //Interstitial video specific values
        video.playbackend = VIDEO_INTERSTITIAL_PLAYBACK_END;//On Leaving Viewport or when Terminated by User
        video.delivery = new int[]{BasicParameterBuilder.VIDEO_DELIVERY_DOWNLOAD};
        video.pos = AdPosition.FULLSCREEN.getValue();

        if (!adConfiguration.isPlacementTypeValid()) {
            video.placement = VIDEO_INTERSTITIAL_PLACEMENT;
            Configuration deviceConfiguration = context.getResources().getConfiguration();
            video.w = deviceConfiguration.screenWidthDp;
            video.h = deviceConfiguration.screenHeightDp;
        }
        else {
            video.placement = adConfiguration.getPlacementTypeValue();
            for (AdSize size : adConfiguration.getSizes()) {
                video.w = size.getWidth();
                video.h = size.getHeight();
                break;
            }
        }

        return video;
    }

    private User getExpectedUser() {
        final User user = new User();

        user.id = USER_ID;
        user.yob = USER_YOB;
        user.keywords = USER_KEYWORDS;
        user.customData = USER_CUSTOM;
        user.gender = USER_GENDER;
        user.buyerUid = USER_BUYER_ID;
        List<ExternalUserId> extendedUserIds = TargetingParams.fetchStoredExternalUserIds();
        if (extendedUserIds != null && extendedUserIds.size() > 0) {
            user.ext = new Ext();
            JSONArray idsJson = new JSONArray();
            for (ExternalUserId id : extendedUserIds) {
                idsJson.put(id.getJson());
            }
            user.ext.put("eids", idsJson);
        }

        final Geo userGeo = user.getGeo();
        userGeo.lat = USER_LAT;
        userGeo.lon = USER_LON;

        return user;
    }

}