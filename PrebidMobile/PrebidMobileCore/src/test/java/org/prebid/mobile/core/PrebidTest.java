package org.prebid.mobile.core;

import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.mopub.mobileads.MoPubView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.core.mockobjects.MockCustomEvent;
import org.prebid.mobile.core.mockobjects.MockServer;
import org.prebid.mobile.unittestutils.BaseSetup;
import org.prebid.mobile.unittestutils.ServerResponsesBuilder;
import org.prebid.mobile.unittestutils.TestConstants;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.prebid.mobile.core.PrebidDemandSettings.PREBID_BIDDER;
import static org.prebid.mobile.core.PrebidDemandSettings.PREBID_CACHE_ID;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class PrebidTest extends BaseSetup {

    @Before
    public void setUp() {
        super.setup();
        setTestServer(MockServer.class.getName());

    }

    private void setTestServer(String serverName) {
        try {
            Field prebidServerField = Prebid.class.getDeclaredField("PREBID_SERVER");
            prebidServerField.setAccessible(true);
            prebidServerField.set(null, serverName);
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
    }

    @Test
    public void testPrebidAttachForMoPub() throws Exception {
        // set up bid response
        ArrayList<BidResponse> bids = new ArrayList<>();
        BidResponse testBid = new BidResponse(TestConstants.cpm1, ServerResponsesBuilder.ut_url);
        testBid.addCustomKeyword("hb_cache_id", "14y3834yq5iu5");
        testBid.addCustomKeyword("hb_pb", "1.37");
        testBid.addCustomKeyword("hb_bidder", "mock_bidder");
        testBid.addCustomKeyword("hb_creative_loadtype", "html");
        bids.add(testBid);
        MockServer.addTestSetup(TestConstants.configID1, bids);
        // set up banner ad unit and init prebid
        BannerAdUnit adUnit = new BannerAdUnit(TestConstants.bannerAdUnit1, TestConstants.configID1);
        adUnit.addSize(300, 250);
        ArrayList<AdUnit> adUnits = new ArrayList<>();
        adUnits.add(adUnit);
        Prebid.init(activity, adUnits, TestConstants.accountId);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        // call API to apply bids
        MoPubView moPubView = new MoPubView(activity);
        assertTrue(TextUtils.isEmpty(moPubView.getKeywords()));
        Prebid.attachBids(moPubView, TestConstants.bannerAdUnit1, activity);
        assertEquals("hb_cache_id:14y3834yq5iu5,hb_pb:1.37,hb_bidder:mock_bidder,hb_creative_loadtype:html,", moPubView.getKeywords());
        // set up new bid responses
        bids.remove(testBid);
        testBid = new BidResponse(TestConstants.cpm2, ServerResponsesBuilder.ut_url);
        testBid.addCustomKeyword("hb_cache_id", "i35u4jiet98u");
        testBid.addCustomKeyword("hb_pb", "0.51");
        testBid.addCustomKeyword("hb_bidder", "mock_bidder");
        testBid.addCustomKeyword("hb_creative_loadtype", "html");
        bids.add(testBid);
        MockServer.addTestSetup(TestConstants.configID1, bids);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        // call API to update bids
        Prebid.attachBids(moPubView, TestConstants.bannerAdUnit1, activity);
        // assert that keywords are updated with new bid info
        assertEquals("hb_cache_id:i35u4jiet98u,hb_pb:0.51,hb_bidder:mock_bidder,hb_creative_loadtype:html,", moPubView.getKeywords());
        // set up new bid responses
        bids.remove(testBid);
        testBid = new BidResponse(TestConstants.cpm3, ServerResponsesBuilder.ut_url);
        testBid.addCustomKeyword("hb_cache_id", "23r89hiufhc");
        testBid.addCustomKeyword("hb_pb", "0.54");
        testBid.addCustomKeyword("hb_bidder", "mock_bidder");
        testBid.addCustomKeyword("hb_creative_loadtype", "demand_sdk");
        bids.add(testBid);
        MockServer.addTestSetup(TestConstants.configID1, bids);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        Prebid.attachBids(moPubView, TestConstants.bannerAdUnit1, activity);
        // assert that if custom event is not present, keywords won't be attached
        assertTrue(TextUtils.isEmpty(moPubView.getKeywords()));
        // set up new bid responses
        bids.remove(testBid);
        testBid = new BidResponse(TestConstants.cpm1, ServerResponsesBuilder.ut_url);
        testBid.addCustomKeyword("hb_cache_id", "423u9285ru");
        testBid.addCustomKeyword("hb_pb", "1.37");
        testBid.addCustomKeyword("hb_bidder", "audienceNetwork");
        testBid.addCustomKeyword("hb_creative_loadtype", "demand_sdk");
        bids.add(testBid);
        MockServer.addTestSetup(TestConstants.configID1, bids);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        // set up mock custom event, and enable demand
        PrebidDemandSettings.PREBID_MOPUB_CUSTOM_EVENT_BANNER = MockCustomEvent.class.getName();
        PrebidDemandSettings.demandSet.add(PrebidDemandSettings.Demand.FACEBOOK);
        Prebid.attachBids(moPubView, TestConstants.bannerAdUnit1, activity);
        // assert keywords are updated
        assertEquals("hb_cache_id:423u9285ru,hb_pb:1.37,hb_bidder:audienceNetwork,hb_creative_loadtype:demand_sdk,", moPubView.getKeywords());
        // assert cache id is saved locally on the object
        assertTrue(moPubView.getLocalExtras().keySet().contains(PREBID_CACHE_ID));
        assertTrue(moPubView.getLocalExtras().keySet().contains(PREBID_BIDDER));
        assertTrue(moPubView.getLocalExtras().get(PREBID_CACHE_ID).equals("423u9285ru"));
        assertTrue(moPubView.getLocalExtras().get(PREBID_BIDDER).equals("audienceNetwork"));
        // set up new bid responses
        bids.remove(testBid);
        testBid = new BidResponse(TestConstants.cpm2, ServerResponsesBuilder.ut_url);
        testBid.addCustomKeyword("hb_cache_id", "341u9582u8u");
        testBid.addCustomKeyword("hb_pb", "0.51");
        testBid.addCustomKeyword("hb_bidder", "audienceNetwork");
        testBid.addCustomKeyword("hb_creative_loadtype", "demand_sdk");
        bids.add(testBid);
        MockServer.addTestSetup(TestConstants.configID1, bids);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        Prebid.attachBids(moPubView, TestConstants.bannerAdUnit1, activity);
        // assert keywords are updated
        assertEquals("hb_cache_id:341u9582u8u,hb_pb:0.51,hb_bidder:audienceNetwork,hb_creative_loadtype:demand_sdk,", moPubView.getKeywords());
        // assert cache id is updated on the object
        assertTrue(moPubView.getLocalExtras().get(PREBID_CACHE_ID).equals("341u9582u8u"));
    }

    @Test
    public void testPrebidAttachBidDoesNotAffectOriginalKeywordsOnMoPub() throws Exception {
        // set up bid response
        ArrayList<BidResponse> bids = new ArrayList<>();
        BidResponse testBid = new BidResponse(TestConstants.cpm1, ServerResponsesBuilder.ut_url);
        testBid.addCustomKeyword("hb_cache_id", "14y3834yq5iu5");
        testBid.addCustomKeyword("hb_pb", "1.37");
        testBid.addCustomKeyword("hb_bidder", "mock_bidder");
        testBid.addCustomKeyword("hb_creative_loadtype", "html");
        bids.add(testBid);
        MockServer.addTestSetup(TestConstants.configID1, bids);
        // set up banner ad unit and init prebid
        BannerAdUnit adUnit = new BannerAdUnit(TestConstants.bannerAdUnit1, TestConstants.configID1);
        adUnit.addSize(300, 250);
        ArrayList<AdUnit> adUnits = new ArrayList<>();
        adUnits.add(adUnit);
        Prebid.init(activity, adUnits, TestConstants.accountId);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        // call API to apply bids
        MoPubView moPubView = new MoPubView(activity);
        moPubView.setKeywords("hello:world,goodbye:world");
        Prebid.attachBids(moPubView, TestConstants.bannerAdUnit1, activity);
        assertEquals("hb_cache_id:14y3834yq5iu5,hb_pb:1.37,hb_bidder:mock_bidder,hb_creative_loadtype:html,hello:world,goodbye:world", moPubView.getKeywords());
        // update mock server
        bids.remove(testBid);
        testBid = new BidResponse(TestConstants.cpm2, ServerResponsesBuilder.ut_url);
        testBid.addCustomKeyword("hb_cache_id", "341u9582u8u");
        testBid.addCustomKeyword("hb_pb", "0.51");
        testBid.addCustomKeyword("hb_bidder", "audienceNetwork");
        testBid.addCustomKeyword("hb_creative_loadtype", "html");
        bids.add(testBid);
        MockServer.addTestSetup(TestConstants.configID1, bids);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        Prebid.attachBids(moPubView, TestConstants.bannerAdUnit1, activity);
        assertEquals("hb_cache_id:341u9582u8u,hb_pb:0.51,hb_bidder:audienceNetwork,hb_creative_loadtype:html,hello:world,goodbye:world", moPubView.getKeywords());
    }

    @Test
    public void testAttachBidWithDelay() throws Exception {
        // set up bid response
        ArrayList<BidResponse> bids = new ArrayList<>();
        BidResponse testBid = new BidResponse(TestConstants.cpm1, ServerResponsesBuilder.ut_url);
        testBid.addCustomKeyword("hb_cache_id", "14y3834yq5iu5");
        testBid.addCustomKeyword("hb_pb", "1.37");
        testBid.addCustomKeyword("hb_bidder", "mock_bidder");
        testBid.addCustomKeyword("hb_creative_loadtype", "html");
        bids.add(testBid);
        MockServer.addTestSetup(TestConstants.configID1, bids);
        MockServer.setDelayedMillis(2000); // delay response for 2 seconds
        // set up banner ad unit and init prebid
        BannerAdUnit adUnit = new BannerAdUnit(TestConstants.bannerAdUnit1, TestConstants.configID1);
        adUnit.addSize(300, 250);
        ArrayList<AdUnit> adUnits = new ArrayList<>();
        adUnits.add(adUnit);
        Prebid.init(activity, adUnits, TestConstants.accountId);
        uiScheduler.unPause();
        bgScheduler.unPause();
        // call API to apply bids
        MoPubView moPubView = new MoPubView(activity);
        assertTrue(TextUtils.isEmpty(moPubView.getKeywords()));
        Prebid.attachBidsWhenReady(moPubView, TestConstants.bannerAdUnit1, new Prebid.OnAttachCompleteListener() {
            @Override
            public void onAttachComplete(Object adObj) {
                assertEquals("com.mopub.mobileads.MoPubView", adObj.getClass().getName());
                assertEquals("hb_cache_id:14y3834yq5iu5,hb_pb:1.37,hb_bidder:mock_bidder,hb_creative_loadtype:html,", ((MoPubView) adObj).getKeywords());
            }
        }, 3000, activity);
        Prebid.attachBidsWhenReady(moPubView, TestConstants.bannerAdUnit1, new Prebid.OnAttachCompleteListener() {
            @Override
            public void onAttachComplete(Object adObj) {
                assertEquals("com.mopub.mobileads.MoPubView", adObj.getClass().getName());
                assertEquals("", ((MoPubView) adObj).getKeywords());
            }
        }, 1000, activity);

    }

    @Test
    public void testPrebidAttachForDFP() throws Exception {
        ArrayList<BidResponse> bids = new ArrayList<>();
        BidResponse testBid = new BidResponse(TestConstants.cpm1, ServerResponsesBuilder.ut_url);
        testBid.addCustomKeyword("hb_cache_id", "14y3834yq5iu5");
        testBid.addCustomKeyword("hb_pb", "1.37");
        testBid.addCustomKeyword("hb_bidder", "mock_bidder");
        testBid.addCustomKeyword("hb_creative_loadtype", "html");
        bids.add(testBid);
        MockServer.addTestSetup(TestConstants.configID1, bids);
        // set up banner ad unit and init prebid
        BannerAdUnit adUnit = new BannerAdUnit(TestConstants.bannerAdUnit1, TestConstants.configID1);
        adUnit.addSize(300, 250);
        ArrayList<AdUnit> adUnits = new ArrayList<>();
        adUnits.add(adUnit);
        Prebid.init(activity, adUnits, TestConstants.accountId);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        // call API to apply bids
        PublisherAdRequest.Builder requestBuilder = new PublisherAdRequest.Builder();
        PublisherAdRequest adRequest = requestBuilder.build();
        Prebid.attachBids(adRequest, TestConstants.bannerAdUnit1, activity);
        Bundle customTargeting = adRequest.getCustomTargeting();
        assertEquals("14y3834yq5iu5", customTargeting.getString("hb_cache_id"));
        assertEquals("1.37", customTargeting.getString("hb_pb"));
        assertEquals("mock_bidder", customTargeting.getString("hb_bidder"));
        assertEquals("html", customTargeting.getString("hb_creative_loadtype"));
        // Update mock server
        bids.remove(testBid);
        testBid = new BidResponse(TestConstants.cpm2, ServerResponsesBuilder.ut_url);
        testBid.addCustomKeyword("hb_cache_id", "airuq3948u9qu9");
        testBid.addCustomKeyword("hb_pb", "0.51");
        testBid.addCustomKeyword("hb_bidder", "mock_bidder");
        testBid.addCustomKeyword("hb_creative_loadtype", "html");
        bids.add(testBid);
        MockServer.addTestSetup(TestConstants.configID1, bids);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        Prebid.attachBids(adRequest, TestConstants.bannerAdUnit1, activity);
        customTargeting = adRequest.getCustomTargeting();
        assertEquals("airuq3948u9qu9", customTargeting.getString("hb_cache_id"));
        assertEquals("0.51", customTargeting.getString("hb_pb"));
        assertEquals("mock_bidder", customTargeting.getString("hb_bidder"));
        assertEquals("html", customTargeting.getString("hb_creative_loadtype"));
        // Update mock server
        bids.remove(testBid);
        testBid = new BidResponse(TestConstants.cpm3, ServerResponsesBuilder.ut_url);
        testBid.addCustomKeyword("hb_cache_id", "yq3rhuh3c88");
        testBid.addCustomKeyword("hb_pb", "0.54");
        testBid.addCustomKeyword("hb_bidder", "audienceNetwork");
        testBid.addCustomKeyword("hb_creative_loadtype", "demand_sdk");
        bids.add(testBid);
        MockServer.addTestSetup(TestConstants.configID1, bids);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        requestBuilder = new PublisherAdRequest.Builder();
        requestBuilder.addKeyword("prebid_banner");
        adRequest = requestBuilder.build();
        Prebid.attachBids(adRequest, TestConstants.bannerAdUnit1, activity);
        customTargeting = adRequest.getCustomTargeting();
        assertEquals(null, customTargeting.getString("hb_cache_id"));
        assertEquals(null, customTargeting.getString("hb_pb"));
        assertEquals(null, customTargeting.getString("hb_bidder"));
        assertEquals(null, customTargeting.getString("hb_creative_loadtype"));
    }
}
