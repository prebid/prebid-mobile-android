package org.prebid.mobile.core;

import android.util.Pair;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.core.mockobjects.MockServer;
import org.prebid.mobile.unittestutils.BaseSetup;
import org.prebid.mobile.unittestutils.ServerResponses;
import org.prebid.mobile.unittestutils.TestConstants;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class BidManagerTest extends BaseSetup {
    public BannerAdUnit adUnit1;
    public BannerAdUnit adUnit2;
    public InterstitialAdUnit adUnit3;

    @Override
    public void setup() {
        super.setup();
        initializePrebid();
    }

    private void initializePrebid() {
        Prebid.setTestServer(MockServer.class.getName());
        //Configure Banner Ad-Units
        adUnit1 = new BannerAdUnit(TestConstants.bannerAdUnit1, TestConstants.configID1);
        adUnit1.addSize(320, 50);

        //Configure Banner Ad-Units
        adUnit2 = new BannerAdUnit(TestConstants.bannerAdUnit2, TestConstants.configID2);
        adUnit2.addSize(320, 200);

        //Configure Interstitial Ad-Units
        adUnit3 = new InterstitialAdUnit(TestConstants.interstitialAdUnit, TestConstants.configID3);

        TargetingParams.setGender(TargetingParams.GENDER.FEMALE);
        TargetingParams.setAge(25);
        TargetingParams.setLocationDecimalDigits(2);
        TargetingParams.setLocationEnabled(true);
        TargetingParams.setCustomTargeting("Test", "Prebid-Custom-1");
        TargetingParams.setCustomTargeting("Test2", "Prebid-Custom-2");
    }

    @Test
    public void testBidManagerAddingCacheIdToTopBid() throws Exception {
        Prebid.setAdServer(Prebid.AdServer.DFP);
        BannerAdUnit adUnit = new BannerAdUnit("Banner", "12345");
        adUnit.addSize(300, 250);
        ArrayList<BidResponse> responses = new ArrayList<BidResponse>();
        BidResponse topBid = new BidResponse(0.5, "Prebid_12345");
        BidResponse bid2 = new BidResponse(0.4, "Prebid_23456");
        BidResponse bid3 = new BidResponse(0.3, "Prebid_34567");
        responses.add(bid3);
        responses.add(bid2);
        responses.add(topBid);
        // test that for DFP, a hb_cache_id will be added to the top bid
        BidManager.bidResponseListener.onBidSuccess(adUnit, responses);
        ArrayList<BidResponse> sortedResponses = BidManager.getBidMap().get(adUnit.getCode());
        assertTrue(sortedResponses.size() == 3);
        assertTrue(sortedResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id", "Prebid_12345")));
        assertTrue(sortedResponses.get(1).getCustomKeywords().size() == 0);
        assertTrue(sortedResponses.get(2).getCustomKeywords().size() == 0);
        // test that for MoPub, nothing will be added for the bids
        Prebid.setAdServer(Prebid.AdServer.MOPUB);
        topBid.getCustomKeywords().clear();
        BidManager.bidResponseListener.onBidSuccess(adUnit, responses);
        sortedResponses = BidManager.getBidMap().get(adUnit.getCode());
        assertTrue(sortedResponses.size() == 3);
        assertTrue(sortedResponses.get(0).getCustomKeywords().size() == 0);
        assertTrue(sortedResponses.get(1).getCustomKeywords().size() == 0);
        assertTrue(sortedResponses.get(2).getCustomKeywords().size() == 0);
    }

    // Test case for checking the init call
    @Test
    public void testCachingOfBidsInit() throws Exception {
        // Check that at the beginning response for identifier is empty
        ArrayList<BidResponse> nullBidResponseForAdSlot1 = BidManager.getWinningBids(adUnit1.getCode());
        assertNull(nullBidResponseForAdSlot1);
        ArrayList<BidResponse> nullBidResponseForAdSlot2 = BidManager.getWinningBids(adUnit2.getCode());
        assertNull(nullBidResponseForAdSlot2);
        // Start running auctions,should get immediate response
        ArrayList<AdUnit> adUnits = new ArrayList<AdUnit>();
        adUnits.add(adUnit1);
        adUnits.add(adUnit2);
        // Set up test responses
        ArrayList<BidResponse> bids = new ArrayList<>();
        BidResponse testBid = new BidResponse(TestConstants.cpm1, ServerResponses.ut_url);
        bids.add(testBid);
        ArrayList<BidResponse> bids2 = new ArrayList<>();
        BidResponse testBid2 = new BidResponse(TestConstants.cpm2, ServerResponses.ut_url);
        bids2.add(testBid2);
        MockServer.addTestSetup(TestConstants.configID1, bids);
        MockServer.addTestSetup(TestConstants.configID2, bids2);
        // Init prebid with ad units
        Prebid.init(activity.getApplicationContext(), adUnits, TestConstants.accountId);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        // Check a response is cached
        ArrayList<BidResponse> bidForAdSlot1 = BidManager.getWinningBids(adUnit1.getCode());
        assertNotNull(bidForAdSlot1);
        assertEquals(TestConstants.cpm1, bidForAdSlot1.get(0).getCpm());

        ArrayList<BidResponse> bidForAdSlot2 = BidManager.getWinningBids(adUnit2.getCode());
        assertNotNull(bidForAdSlot2);
        assertEquals(TestConstants.cpm2, bidForAdSlot2.get(0).getCpm());
    }

    // Test case for checking the StartNewAuction call with response as varying CPM's and check if its stored correctly in the bidMap
    @Test
    public void testStartNewAuction() throws Exception {
        // Cache a bid
        ArrayList<AdUnit> adUnits = new ArrayList<AdUnit>();
        adUnits.add(adUnit1);
        Prebid.init(activity.getApplicationContext(), adUnits, TestConstants.accountId);

        // Here is where we call start new Auction.
        ArrayList<BidResponse> bids = new ArrayList<>();
        bids.add(new BidResponse(TestConstants.cpm3, ServerResponses.ut_url));
        MockServer.addTestSetup(TestConstants.configID1, bids);
        BidManager.startNewAuction(activity.getApplicationContext(), adUnit1);
        // Run tasks
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        ArrayList<BidResponse> bidForAdSlot_1 = BidManager.getWinningBids(adUnit1.getCode());
        assertNotNull(bidForAdSlot_1);
        assertEquals(TestConstants.cpm3, bidForAdSlot_1.get(0).getCpm());

        MockServer.clearSetUps();
        ArrayList<BidResponse> bids2 = new ArrayList<>();
        bids2.add(new BidResponse(TestConstants.cpm2, ServerResponses.ut_url));
        MockServer.addTestSetup(TestConstants.configID1, bids2);
        BidManager.startNewAuction(activity.getApplicationContext(), adUnit1);
        // Run tasks
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        bidForAdSlot_1 = BidManager.getWinningBids(adUnit1.getCode());
        assertNotNull(bidForAdSlot_1);
        assertEquals(TestConstants.cpm2, bidForAdSlot_1.get(0).getCpm());
    }


    @Test
    public void testNoBidForUnregisteredAd() throws Exception {
        // Cache a bid
        ArrayList<BidResponse> bids = new ArrayList<>();
        BidResponse testBid = new BidResponse(TestConstants.cpm3, ServerResponses.ut_url);
        testBid.addCustomKeyword("pb_cache_id", "14y3834yq5iu5");
        testBid.addCustomKeyword("hb_pb", "0.54");
        testBid.addCustomKeyword("hb_bidder", "mock_bidder");
        bids.add(testBid);
        MockServer.addTestSetup(TestConstants.configID1, bids);
        ArrayList<AdUnit> adUnits = new ArrayList<AdUnit>();
        adUnits.add(adUnit1);
        Prebid.init(activity.getApplicationContext(), adUnits, TestConstants.accountId);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        ArrayList<BidResponse> bidForAdUnit = BidManager.getWinningBids(adUnit1.getCode());
        assertNotNull(bidForAdUnit);
        BannerAdUnit randomAdUnit = new BannerAdUnit("Random", TestConstants.configID1);
        bidForAdUnit = BidManager.getWinningBids(randomAdUnit.getCode());
        assertNull(bidForAdUnit);
    }


    //Test covering if getKeyWord API is working fine as expected.
    @Test
    public void testKeywordString() throws Exception {
        //Registering a list of adUnits test
        ArrayList<BidResponse> bids = new ArrayList<>();
        BidResponse testBid = new BidResponse(TestConstants.cpm3, ServerResponses.ut_url);
        testBid.addCustomKeyword("pb_cache_id", "14y3834yq5iu5");
        testBid.addCustomKeyword("hb_pb", "0.54");
        testBid.addCustomKeyword("hb_bidder", "mock_bidder");
        bids.add(testBid);
        MockServer.addTestSetup(TestConstants.configID1, bids);
        ArrayList<AdUnit> adUnits = new ArrayList<AdUnit>();
        adUnits.add(adUnit1);
        Prebid.init(activity.getApplicationContext(), adUnits, TestConstants.accountId);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        ArrayList<Pair<String, String>> keywords = BidManager.getKeywordsForAdUnit(adUnit1.getCode(), activity.getApplicationContext());
        for (Pair keywordPair : keywords) {
            if ("pb_cache_id".equals(keywordPair.first)) {
                assertEquals("14y3834yq5iu5", keywordPair.second);
            } else if ("hb_pb".equals(keywordPair.first)) {
                assertEquals("0.54", keywordPair.second);
            } else if ("hb_bidder".equals(keywordPair.first)) {
                assertEquals("mock_bidder", keywordPair.second);
            }
        }
    }


    @Override
    public void tearDown() {
        super.tearDown();

        // Clear targeting since these are static settings
        TargetingParams.clearCustomKeywords();
        TargetingParams.setLocation(null);
        TargetingParams.setLocationDecimalDigits(-1);
        TargetingParams.setGender(TargetingParams.GENDER.UNKNOWN);
        TargetingParams.setLocationEnabled(true); // default is true
        TargetingParams.setLocation(null);

        // Reset the BidManager for next server Run
        BidManager.reset();
        MockServer.clearSetUps();
    }
}
