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
import org.prebid.mobile.*;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.models.AdConfiguration;
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;
import static org.prebid.mobile.rendering.networking.parameters.BasicParameterBuilder.*;

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

    private Context mContext;

    private final boolean mBrowserActivityAvailable = true;

    @Before
    public void setUp() throws Exception {
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        org.prebid.mobile.PrebidMobile.setApplicationContext(mContext);
        ManagersResolver.getInstance().prepare(mContext);
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

        PrebidMobile.sendMraidSupportParams = true;
        PrebidMobile.useExternalBrowser = false;
        PrebidMobile.isCoppaEnabled = false;
        PrebidMobile.clearStoredBidResponses();
        PrebidMobile.setStoredAuctionResponse(null);
    }

    @Test
    public void whenAppendParametersAndBannerType_ImpWithValidBannerObject() throws JSONException {
        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));
        adConfiguration.setPbAdSlot("12345");
        PrebidMobile.addStoredBidResponse("bidderTest", "123456");
        PrebidMobile.setStoredAuctionResponse("storedResponse");

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), mBrowserActivityAvailable);
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
        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.INTERSTITIAL);
        adConfiguration.setAdPosition(AdPosition.FULLSCREEN);

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), mBrowserActivityAvailable);
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
        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.VAST);
        adConfiguration.setAdPosition(AdPosition.FULLSCREEN);

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), mBrowserActivityAvailable);
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
        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.VAST);
        adConfiguration.setPlacementType(PlacementType.IN_BANNER);
        adConfiguration.setAdPosition(AdPosition.FULLSCREEN);
        adConfiguration.addSize(new AdSize(300, 250));

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), mBrowserActivityAvailable);
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
        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        PrebidMobile.isCoppaEnabled = true;

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), mBrowserActivityAvailable);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        BidRequest actualBidRequest = adRequestInput.getBidRequest();
        assertEquals(1, actualBidRequest.getRegs().coppa.intValue());
    }

    @Test
    public void whenAppendParametersAndCoppaFalse_CoppaNull() {
        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), mBrowserActivityAvailable);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        BidRequest actualBidRequest = adRequestInput.getBidRequest();
        assertNull(actualBidRequest.getRegs().coppa);
    }

    @Test
    public void whenAppendParametersAndTargetingParamsWereSet_TargetingParamsWereAppend()
    throws JSONException {
        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        TargetingParams.setUserId(USER_ID);
        TargetingParams.setUserAge(USER_AGE);
        TargetingParams.addUserKeyword(USER_KEYWORDS);
        TargetingParams.setUserCustomData(USER_CUSTOM);
        TargetingParams.setGender(TargetingParams.GENDER.MALE);
        TargetingParams.setBuyerId(USER_BUYER_ID);
        TargetingParams.setUserExt(new ExtObject());
        TargetingParams.setUserLatLng(USER_LAT, USER_LON);

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), mBrowserActivityAvailable);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        User actualUser = adRequestInput.getBidRequest().getUser();
        User expectedUser = getExpectedUser();
        assertEquals(expectedUser.getJsonObject().toString(), actualUser.getJsonObject().toString());
    }

    @Test
    public void whenAppendParametersAndSendMraidSupportParamsFalse_NoMraidApi() {
        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        PrebidMobile.sendMraidSupportParams = false;

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), mBrowserActivityAvailable);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        Imp actualImp = adRequestInput.getBidRequest().getImp().get(0);
        assertEquals(Arrays.toString(new int[]{7}), Arrays.toString(actualImp.banner.api));
    }

    @Test
    public void whenAppendParametersAndUseExternalBrowserFalseAndBrowserActivityAvailable_ClickBrowserEqualsZero() {
        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        PrebidMobile.useExternalBrowser = false;

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), mBrowserActivityAvailable);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        Imp actualImp = adRequestInput.getBidRequest().getImp().get(0);
        assertEquals(0, actualImp.clickBrowser.intValue());
    }

    @Test
    public void whenAppendParametersAndUseExternalBrowserTrueAndBrowserActivityAvailable_ClickBrowserEqualsOne() {
        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        PrebidMobile.useExternalBrowser = true;

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), mBrowserActivityAvailable);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        Imp actualImp = adRequestInput.getBidRequest().getImp().get(0);
        assertEquals(1, actualImp.clickBrowser.intValue());
    }

    @Test
    public void whenAppendParametersAndUseExternalBrowserFalseAndBrowserActivityNotAvailable_ClickBrowserEqualsOne() {
        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);
        adConfiguration.addSize(new AdSize(320, 50));

        PrebidMobile.useExternalBrowser = false;

        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), false);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        Imp actualImp = adRequestInput.getBidRequest().getImp().get(0);
        assertEquals(1, actualImp.clickBrowser.intValue());
    }

    @Test
    public void whenAppendParametersAndTargetingAccessControlNotEmpty_BiddersAddedToExt()
    throws JSONException {
        TargetingParams.addBidderToAccessControlList("bidder");

        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setConfigId("config");
        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), false);
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

        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setConfigId("config");
        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), false);
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
        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setConfigId("config");
        DataObject dataObject = new DataObject();
        String testName = "testDataObject";
        dataObject.setName(testName);
        adConfiguration.addUserData(dataObject);
        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), false);
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
        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.addContextData("context", "contextData");
        BasicParameterBuilder builder = new BasicParameterBuilder(adConfiguration, mContext.getResources(), false);
        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        Ext impExt = adRequestInput.getBidRequest().getImp().get(0).getExt();
        assertTrue(impExt.getMap().containsKey("context"));
        JSONObject contextDataJson = ((JSONObject) impExt.getMap().get("context")).getJSONObject("data");
        assertTrue(contextDataJson.has("context"));
        assertEquals("contextData", contextDataJson.getJSONArray("context").get(0));
    }

    private BidRequest getExpectedBidRequest(AdConfiguration adConfiguration, String uuid) {
        BidRequest bidRequest = new BidRequest();
        bidRequest.setId(uuid);
        boolean isVideo = adConfiguration.isAdType(AdConfiguration.AdUnitIdentifierType.VAST);
        bidRequest.getExt().put("prebid", Prebid.getJsonObjectForBidRequest(PrebidMobile.getPrebidServerAccountId(), isVideo));
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

    private Imp getExpectedImp(AdConfiguration adConfiguration, String uuid) {
        Imp imp = new Imp();

        imp.displaymanager = BasicParameterBuilder.DISPLAY_MANAGER_VALUE;
        imp.displaymanagerver = PrebidMobile.SDK_VERSION;

        if (!adConfiguration.isAdType(AdConfiguration.AdUnitIdentifierType.VAST)) {
            imp.secure = 1;
        }
        //Send 1 for interstitial/interstitial video and 0 for banners
        boolean isInterstitial = adConfiguration.isAdType(AdConfiguration.AdUnitIdentifierType.VAST) ||
                                 adConfiguration.isAdType(AdConfiguration.AdUnitIdentifierType.INTERSTITIAL);
        imp.instl = isInterstitial ? 1 : 0;

        // 0 == embedded, 1 == native
        imp.clickBrowser = !PrebidMobile.useExternalBrowser && mBrowserActivityAvailable ? 0 : 1;
        imp.id = uuid;
        imp.getExt().put("prebid", Prebid.getJsonObjectForImp(adConfiguration));

        if (adConfiguration.isAdType(AdConfiguration.AdUnitIdentifierType.VAST)) {
            imp.video = getExpectedVideoImpValues(imp, adConfiguration);
        } else {
            imp.banner = getExpectedBannerImpValues(imp, adConfiguration);
        }

        final String pbAdSlot = adConfiguration.getPbAdSlot();
        if (pbAdSlot != null) {
            JSONObject context = new JSONObject();
            JSONObject data = new JSONObject();
            Utils.addValue(data, "adslot", pbAdSlot);
            Utils.addValue(context, "data", data);

            imp.getExt().put("context", context);
        }

        return imp;
    }

    private Banner getExpectedBannerImpValues(Imp imp, AdConfiguration adConfiguration) {
        Banner banner = new Banner();
        banner.api = new int[]{3, 5, 6, 7};

        if (adConfiguration.isAdType(AdConfiguration.AdUnitIdentifierType.BANNER)) {
            for (AdSize size : adConfiguration.getAdSizes()) {
                banner.addFormat(size.width, size.height);
            }
        }
        else if (adConfiguration.isAdType(AdConfiguration.AdUnitIdentifierType.INTERSTITIAL)) {
            Configuration deviceConfiguration = mContext.getResources().getConfiguration();
            banner.addFormat(deviceConfiguration.screenWidthDp,
                             deviceConfiguration.screenHeightDp);
        }

        if (adConfiguration.isAdPositionValid()) {
            banner.pos = adConfiguration.getAdPositionValue();
        }
        return banner;
    }

    private Video getExpectedVideoImpValues(Imp imp, AdConfiguration adConfiguration) {
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
            Configuration deviceConfiguration = mContext.getResources().getConfiguration();
            video.w = deviceConfiguration.screenWidthDp;
            video.h = deviceConfiguration.screenHeightDp;
        }
        else {
            video.placement = adConfiguration.getPlacementTypeValue();
            for (AdSize size : adConfiguration.getAdSizes()) {
                video.w = size.width;
                video.h = size.height;
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
            user.ext = new ExtObject();
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