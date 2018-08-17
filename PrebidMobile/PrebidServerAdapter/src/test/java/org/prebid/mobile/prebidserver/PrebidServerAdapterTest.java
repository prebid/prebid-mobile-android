package org.prebid.mobile.prebidserver;


import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.core.AdUnit;
import org.prebid.mobile.core.BannerAdUnit;
import org.prebid.mobile.core.BidManager;
import org.prebid.mobile.core.BidResponse;
import org.prebid.mobile.core.CacheManager;
import org.prebid.mobile.core.ErrorCode;
import org.prebid.mobile.core.InterstitialAdUnit;
import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.core.TargetingParams;
import org.prebid.mobile.prebidserver.internal.Settings;
import org.prebid.mobile.unittestutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class PrebidServerAdapterTest extends BaseSetup {
    @Test
    public void testGDPRSettings() throws Exception {
        // init state, no GDPR settings
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        ArrayList<AdUnit> adUnits = new ArrayList<>();
        BannerAdUnit bannerAdUnit = new BannerAdUnit("banner", "12345");
        bannerAdUnit.addSize(320, 50);
        adUnits.add(bannerAdUnit);
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit("interstitial", "23456");
        adUnits.add(interstitialAdUnit);
        Prebid.init(activity, adUnits, "34567", Prebid.AdServer.DFP, Prebid.Host.APPNEXUS);
        JSONObject postData = adapter.getPostData(activity, adUnits);
        try {
            postData.getJSONObject("user").getJSONObject("ext");
        } catch (JSONException e) {
            assertEquals("No value for ext", e.getMessage());
        }
        try {
            postData.getJSONObject("regs").getJSONObject("ext").getBoolean("gdpr");
        } catch (JSONException e) {
            assertEquals("No value for gdpr", e.getMessage());
        }
        // set GDPR values
        TargetingParams.setSubjectToGDPR(activity, true);
        TargetingParams.setGDPRConsentString(activity, "hello world");
        postData = adapter.getPostData(activity, adUnits);
        assertEquals(1, postData.getJSONObject("regs").getJSONObject("ext").getInt("gdpr"));
        assertEquals("hello world", postData.getJSONObject("user").getJSONObject("ext").getString("consent"));
    }

    @Test
    public void testPostDataGenerationKeywords() throws Exception {
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        ArrayList<AdUnit> adUnits = new ArrayList<>();
        BannerAdUnit bannerAdUnit = new BannerAdUnit("banner", "12345");
        bannerAdUnit.addSize(320, 50);
        adUnits.add(bannerAdUnit);
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit("interstitial", "23456");
        adUnits.add(interstitialAdUnit);
        // add keywords for user
        TargetingParams.setUserTargeting("state", "ny");
        TargetingParams.setUserTargeting("state", "nj");
        // Test with DFP settings
        Prebid.init(activity, adUnits, "34567", Prebid.AdServer.DFP, Prebid.Host.APPNEXUS);
        JSONObject postData = adapter.getPostData(activity, adUnits);
        assertEquals("state=ny,state=nj,", postData.getJSONObject("user").getString("keywords"));
        try {
            postData.getJSONObject("app").getString("keywords");
        } catch (JSONException e) {
            assertEquals("No value for keywords", e.getMessage());
        }
        TargetingParams.removeUserKeyword("state");
        postData = adapter.getPostData(activity, adUnits);
        try {
            postData.getJSONObject("user").getString("keywords");
        } catch (JSONException e) {
            assertEquals("No value for keywords", e.getMessage());
        }
    }

    @Test
    public void testPostDataGeneration() throws Exception {
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        ArrayList<AdUnit> adUnits = new ArrayList<>();
        BannerAdUnit bannerAdUnit = new BannerAdUnit("banner", "12345");
        bannerAdUnit.addSize(320, 50);
        adUnits.add(bannerAdUnit);
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit("interstitial", "23456");
        adUnits.add(interstitialAdUnit);
        // Test with DFP settings
        Prebid.init(activity, adUnits, "34567", Prebid.AdServer.DFP, Prebid.Host.APPNEXUS);
        setAdServer(Prebid.AdServer.DFP);
        JSONObject postData = adapter.getPostData(activity, adUnits);
        assertTrue(postData.has(Settings.REQUEST_DEVICE));
        assertTrue(postData.has(Settings.REQUEST_APP));
        assertTrue(postData.has(Settings.REQUEST_USER));
        assertTrue("request data is missing 'id' field", postData.has("id"));
        assertTrue("request data is missing 'imp' field", postData.has("imp"));
        assertTrue("'imp' array should have 2 objects", postData.getJSONArray("imp").length() == 2);
        assertTrue(postData.getJSONArray("imp").getJSONObject(0).get("id").equals("banner"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(0).has("ext"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).has("secure"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").has("prebid"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("prebid").has("storedrequest"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("prebid").getJSONObject("storedrequest").get("id").equals("12345"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).get("id").equals("interstitial"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).has("ext"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).has("secure"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).has("instl"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).getJSONObject("ext").has("prebid"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).getJSONObject("ext").getJSONObject("prebid").has("storedrequest"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).getJSONObject("ext").getJSONObject("prebid").getJSONObject("storedrequest").get("id").equals("23456"));
        assertTrue(postData.has("ext"));
        assertTrue(postData.getJSONObject("ext").has("prebid"));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").has("storedrequest"));
        assertTrue(!postData.getJSONObject("ext").getJSONObject("prebid").has("cache"));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("storedrequest").getString("id").equals(Prebid.getAccountId()));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").has("targeting"));
        // Test with MoPub settings
        Prebid.init(activity, adUnits, "12345", Prebid.AdServer.MOPUB, Prebid.Host.APPNEXUS);
        setAdServer(Prebid.AdServer.MOPUB);
        Prebid.shouldLoadOverSecureConnection(false);
        postData = adapter.getPostData(activity, adUnits);
        assertTrue(postData.has(Settings.REQUEST_DEVICE));
        assertTrue(postData.has(Settings.REQUEST_APP));
        assertTrue(postData.has(Settings.REQUEST_USER));
        assertTrue("request data is missing 'id' field", postData.has("id"));
        assertTrue("request data is missing 'imp' field", postData.has("imp"));
        assertTrue("'imp' array should have 2 objects", postData.getJSONArray("imp").length() == 2);
        assertTrue(postData.getJSONArray("imp").getJSONObject(0).get("id").equals("banner"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(0).has("ext"));
        assertTrue(!postData.getJSONArray("imp").getJSONObject(0).has("secure"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").has("prebid"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("prebid").has("storedrequest"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("prebid").getJSONObject("storedrequest").get("id").equals("12345"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).get("id").equals("interstitial"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).has("ext"));
        assertTrue(!postData.getJSONArray("imp").getJSONObject(1).has("secure"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).getJSONObject("ext").has("prebid"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).getJSONObject("ext").getJSONObject("prebid").has("storedrequest"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).getJSONObject("ext").getJSONObject("prebid").getJSONObject("storedrequest").get("id").equals("23456"));
        assertTrue(postData.has("ext"));
        assertTrue(postData.getJSONObject("ext").has("prebid"));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").has("storedrequest"));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("storedrequest").getString("id").equals(Prebid.getAccountId()));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").has("cache"));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("cache").has("bids"));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("cache").getJSONObject("bids").length() == 0);
    }

    private void setAdServer(Prebid.AdServer adServer) {
        try {
            Field adServerField = Prebid.class.getDeclaredField("adServer");
            adServerField.setAccessible(true);
            adServerField.set(null, adServer);
            Field useLocalCacheField = Prebid.class.getDeclaredField("useLocalCache");
            useLocalCacheField.setAccessible(true);
            if (adServer == Prebid.AdServer.MOPUB) {
                useLocalCacheField.set(null, false);
            } else {
                useLocalCacheField.set(null, true);
            }
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
    }

    @Test
    public void testSingleBidResponseProcessingForMoPub() throws Exception {
        setAdServer(Prebid.AdServer.MOPUB);
        // cached bid response
        String serverResponse = "{\"id\":\"c7043990-2ac9-4ee5-a3d5-f3c032edeb4f\",\"seatbid\":[{\"bid\":[{\"id\":\"6051533834469542107\",\"impid\":\"Banner_300x250\",\"price\":0.5,\"adm\":\"<script type=\\\"application/javascript\\\" src=\\\"https://nym1-ib.adnxs.com/ab?e=wqT_3QLsB6DsAwAAAwDWAAUBCKHJmdMFEKCGu4213fG-KRi2sNeng8GDl0AqNgkAAAECCOA_EQEHNAAA4D8ZAAAAgOtR4D8hERIAKREJADERG6Aw8ub8BDi-B0C-B0gCUNbLkw5Y4YBIYABokUB4y9EEgAEBigEDVVNEkgUG8FKYAawCoAH6AagBAbABALgBAsABA8gBAtABAtgBAOABAPABAIoCOnVmKCdhJywgNDk0NDcyLCAxNTE2NjU5ODczKTt1ZigncicsIDI5NjgxMTEwLDIeAPCQkgL9ASFvamFvMHdpNjBJY0VFTmJMa3c0WUFDRGhnRWd3QURnQVFBUkl2Z2RROHViOEJGZ0FZTllCYUFCd0pIakdod0dBQVNTSUFjYUhBWkFCQVpnQkFhQUJBYWdCQTdBQkFMa0JLWXVJZ3dBQTREX0JBU21MaUlNQUFPQV95UUZUSEs5RHZNdnhQOWtCQUFBQQEDJDhEX2dBUUQxQVEBDixDWUFnQ2dBZ0MxQWcFEAA5CQjwUERBQWdESUFnRGdBZ0RvQWdENEFnQ0FBd1NRQXdDWUF3R29BN3JRaHdTNkF4RmtaV1poZFd4MEkwNVpUVEk2TXpnek1BLi6aAjkhX2d0c19naTIAAfBMNFlCSUlBUW9BRG9SWkdWbVlYVnNkQ05PV1UweU9qTTRNekEu0gJ2NzcxODkxNSw3ODY5OTM5LDc4OTQzNTEsMTgyMjk1LDIzMzg2OTkFCAg3MDAJCAAyCQgIODAzBRAIODA3BQgEOTQJEAg5NDQJEAA1CSgIOTUyCRAJUGg5MDAz2ALoB-ACx9MB8gIQCgZBRFZfSUQSBjQl_hzyAhEKBkNQRwETHAcxOTc3OTMzAScIBUNQBRPwtzg1MTM1OTSAAwGIAwGQAwCYAxSgAwGqAwDAA6wCyAMA2AMA4AMA6AMC-AMAgAQAkgQJL29wZW5ydGIymAQAogQPMjA3LjIzNy4xNTAuMjQ2qASetA-yBAwIABAAGAAgADAAOAC4BADABADIBADSBBFkZWZhdWx0I05ZTTI6MzgzMNoEAggB4AQA8ATWy5MOggUZb3JnLnByZWJpZC5tb2JpbGUuZGVtb2FwcIgFAZgFAKAF______8BA7ABqgUkYzcwNDM5OTAtMmFjOS00ZWU1LWEzZDUtZjNjMDMyZWRlYjRmwAUAyQVpehTwP9IFCQkJDFAAANgFAeAFAfAFAfoFBAgAEACQBgA.&s=66498e1c32a2420c27f1e24d1d279a9577bdbcde&pp=${AUCTION_PRICE}&\\\"></script>\",\"adid\":\"29681110\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=29681110\",\"cid\":\"958\",\"crid\":\"29681110\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"targeting\":{\"hb_bidder\":\"appnexus\",\"hb_bidder_appnexus\":\"appnexus\",\"hb_cache_id\":\"8d472803-909b-40d9-8dc9-916433c810f0\",\"hb_cache_id_appnexus\":\"8d472803-909b-40d9-8dc9-916433c810f0\",\"hb_creative_loadtype\":\"html\",\"hb_pb\":\"0.50\",\"hb_pb_appnexus\":\"0.50\",\"hb_size\":\"300x250\",\"hb_size_appnexus\":\"300x250\"},\"type\":\"banner\"},\"bidder\":{\"appnexus\":{\"brand_id\":1,\"auction_id\":2989764441633899500,\"bidder_id\":2}}}}],\"seat\":\"appnexus\"}],\"ext\":{\"responsetimemillis\":{\"appnexus\":38}}}";
        JSONObject serverResponseJson = new JSONObject(serverResponse);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        ArrayList<AdUnit> units = new ArrayList<>();
        final BannerAdUnit bannerAdUnit = new BannerAdUnit("Banner_300x250", "12345");
        bannerAdUnit.addSize(300, 250);
        units.add(bannerAdUnit);
        BidManager.BidResponseListener listener = new BidManager.BidResponseListener() {
            @Override
            public void onBidSuccess(AdUnit bidRequest, ArrayList<BidResponse> bidResponses) {
                assertTrue(bidRequest.equals(bannerAdUnit));
                assertTrue(bidResponses.size() == 1);
                assertTrue(bidResponses.get(0).getCreative().equals("8d472803-909b-40d9-8dc9-916433c810f0"));
                assertTrue(bidResponses.get(0).getCpm() == 0.50);
                assertTrue(bidResponses.get(0).getCustomKeywords().size() == 9);
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_bidder", "appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_bidder_appnexus", "appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id", "8d472803-909b-40d9-8dc9-916433c810f0")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id_appnexus", "8d472803-909b-40d9-8dc9-916433c810f0")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_creative_loadtype", "html")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_pb", "0.50")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_pb_appnexus", "0.50")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_size", "300x250")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_size_appnexus", "300x250")));
            }

            @Override
            public void onBidFailure(AdUnit bidRequest, ErrorCode reason) {
                fail("this should never be called.");
            }
        };
        adapter.requestBid(activity, listener, units);
        adapter.onServerResponded(serverResponseJson);
    }

    @Test
    public void testMultipleBidsResponseProcessingForMoPub() throws Exception {
        setAdServer(Prebid.AdServer.MOPUB);
        // cached bid response
        String serverResponse = "{\"id\":\"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\"seatbid\":[{\"bid\":[{\"id\":\"4009307468250838284\",\"impid\":\"Banner_300x250\",\"price\":0.5,\"adm\":\"<script></script>\",\"adid\":\"73501515\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=73501515\",\"cid\":\"958\",\"crid\":\"73501515\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"targeting\":{\"hb_bidder\":\"appnexus\",\"hb_cache_id\":\"random-value-appnexus\",\"hb_cache_id_appnexus\":\"random-value-appnexus\",\"hb_bidder_appnexus\":\"appnexus\",\"hb_creative_loadtype\":\"html\",\"hb_env\":\"mobile-app\",\"hb_env_appnexus\":\"mobile-app\",\"hb_pb\":\"0.50\",\"hb_pb_appnexus\":\"0.50\",\"hb_size\":\"300x250\",\"hb_size_appnexus\":\"300x250\"},\"type\":\"banner\"},\"bidder\":{\"appnexus\":{\"brand_id\":1,\"auction_id\":7466795334738195000,\"bidder_id\":2,\"bid_ad_type\":0}}}}],\"seat\":\"appnexus\"},{\"bid\":[{\"id\":\"4009307468250838284\",\"impid\":\"Banner_300x250\",\"price\":0.4,\"adm\":\"<script></script>\",\"adid\":\"73501515\",\"adomain\":[\"rubicon.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=73501515\",\"cid\":\"958\",\"crid\":\"73501515\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"targeting\":{\"hb_cache_id_rubicon\":\"random-value-rubicon\",\"hb_bidder_rubicon\":\"rubicon\",\"hb_creative_loadtype\":\"html\",\"hb_env_rubicon\":\"mobile-app\",\"hb_pb_rubicon\":\"0.40\",\"hb_size_rubicon\":\"300x250\"},\"type\":\"banner\"}}}],\"seat\":\"rubicon\"}],\"ext\":{\"responsetimemillis\":{\"appnexus\":19}}}";
        JSONObject serverResponseJson = new JSONObject(serverResponse);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        ArrayList<AdUnit> units = new ArrayList<>();
        final BannerAdUnit bannerAdUnit = new BannerAdUnit("Banner_300x250", "12345");
        bannerAdUnit.addSize(300, 250);
        units.add(bannerAdUnit);
        BidManager.BidResponseListener listener = new BidManager.BidResponseListener() {
            @Override
            public void onBidSuccess(AdUnit bidRequest, ArrayList<BidResponse> bidResponses) {
                assertTrue(bidRequest.equals(bannerAdUnit));
                assertTrue(bidResponses.size() == 2);
                assertTrue(bidResponses.get(0).getCreative().equals("random-value-appnexus"));
                assertTrue(bidResponses.get(1).getCreative().equals("random-value-rubicon"));
                assertTrue(bidResponses.get(0).getCpm() == 0.50);
                assertTrue(bidResponses.get(1).getCpm() == 0.40);
                assertTrue(bidResponses.get(0).getCustomKeywords().size() == 11);
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_bidder", "appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_bidder_appnexus", "appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id", "random-value-appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id_appnexus", "random-value-appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_creative_loadtype", "html")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_pb", "0.50")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_pb_appnexus", "0.50")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_size", "300x250")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_size_appnexus", "300x250")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_env", "mobile-app")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_env_appnexus", "mobile-app")));
                assertTrue(bidResponses.get(1).getCustomKeywords().size() == 6);
                assertTrue(bidResponses.get(1).getCustomKeywords().contains(new Pair<String, String>("hb_bidder_rubicon", "rubicon")));
                assertTrue(bidResponses.get(1).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id_rubicon", "random-value-rubicon")));
                assertTrue(bidResponses.get(1).getCustomKeywords().contains(new Pair<String, String>("hb_creative_loadtype", "html")));
                assertTrue(bidResponses.get(1).getCustomKeywords().contains(new Pair<String, String>("hb_pb_rubicon", "0.40")));
                assertTrue(bidResponses.get(1).getCustomKeywords().contains(new Pair<String, String>("hb_size_rubicon", "300x250")));

            }

            @Override
            public void onBidFailure(AdUnit bidRequest, ErrorCode reason) {
                fail("this should never be called.");
            }
        };
        adapter.requestBid(activity, listener, units);
        adapter.onServerResponded(serverResponseJson);
    }

    @Test
    public void testMultipleBidsForTheSameBidderResponseProcessingForMoPub() throws Exception {
        setAdServer(Prebid.AdServer.MOPUB);
        // cached bid response
        String serverResponse = "{\"id\":\"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\"seatbid\":[{\"bid\":[{\"id\":\"964206900338444811\",\"impid\":\"Banner_300x250\",\"price\":15,\"adm\":\"hello world\",\"adid\":\"28477710\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=28477710\",\"cid\":\"958\",\"crid\":\"28477710\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"targeting\":{\"hb_bidder\":\"appnexus\",\"hb_bidder_appnexus\":\"appnexus\",\"hb_cache_id\":\"random-value-appnexus\",\"hb_cache_id_appnexus\":\"random-value-appnexus\",\"hb_creative_loadtype\":\"html\",\"hb_env\":\"mobile-app\",\"hb_env_appnexus\":\"mobile-app\",\"hb_pb\":\"15.00\",\"hb_pb_appnexus\":\"15.00\",\"hb_size\":\"300x250\",\"hb_size_appnexus\":\"300x250\"},\"type\":\"banner\"},\"bidder\":{\"appnexus\":{\"brand_id\":1,\"auction_id\":7451460372041783000,\"bidder_id\":2,\"bid_ad_type\":0}}}},{\"id\":\"6786335593489123644\",\"impid\":\"Banner_300x250\",\"price\":0.5,\"adm\":\"hello world\",\"adid\":\"68501584\",\"adomain\":[\"peugeot.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=68501584\",\"cid\":\"958\",\"crid\":\"68501584\",\"cat\":[\"IAB2-8\",\"IAB2-1\",\"IAB2-2\",\"IAB2-19\",\"IAB2-5\",\"IAB2-18\",\"IAB2-23\",\"IAB2-11\",\"IAB2\",\"IAB2-15\",\"IAB2-9\",\"IAB2-12\",\"IAB2-7\",\"IAB2-21\",\"IAB2-22\",\"IAB2-14\",\"IAB2-17\",\"IAB2-6\",\"IAB2-20\",\"IAB2-10\",\"IAB2-13\",\"IAB2-16\",\"IAB2-4\",\"IAB2-3\"],\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"type\":\"banner\"},\"bidder\":{\"appnexus\":{\"brand_id\":3264,\"auction_id\":7451460372041783000,\"bidder_id\":2,\"bid_ad_type\":0}}}}],\"seat\":\"appnexus\"}],\"ext\":{\"responsetimemillis\":{\"appnexus\":235}}}";
        JSONObject serverResponseJson = new JSONObject(serverResponse);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        ArrayList<AdUnit> units = new ArrayList<>();
        final BannerAdUnit bannerAdUnit = new BannerAdUnit("Banner_300x250", "12345");
        bannerAdUnit.addSize(300, 250);
        units.add(bannerAdUnit);
        BidManager.BidResponseListener listener = new BidManager.BidResponseListener() {
            @Override
            public void onBidSuccess(AdUnit bidRequest, ArrayList<BidResponse> bidResponses) {
                assertTrue(bidRequest.equals(bannerAdUnit));
                assertTrue(bidResponses.size() == 1);
                assertTrue(bidResponses.get(0).getCreative().equals("random-value-appnexus"));
                assertTrue(bidResponses.get(0).getCpm() == 15);
                assertTrue(bidResponses.get(0).getCustomKeywords().size() == 11);
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_bidder", "appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_bidder_appnexus", "appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id", "random-value-appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id_appnexus", "random-value-appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_creative_loadtype", "html")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_pb", "15.00")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_pb_appnexus", "15.00")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_size", "300x250")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_size_appnexus", "300x250")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_env", "mobile-app")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_env_appnexus", "mobile-app")));
            }

            @Override
            public void onBidFailure(AdUnit bidRequest, ErrorCode reason) {
                fail("this should never be called.");
            }
        };
        adapter.requestBid(activity, listener, units);
        adapter.onServerResponded(serverResponseJson);
    }

    @Test
    public void testNoCacheIdResponseProcessingForMoPub() throws Exception {
        setAdServer(Prebid.AdServer.MOPUB);
        // cached bid response
        String serverResponse = "{\"id\":\"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\"seatbid\":[{\"bid\":[{\"id\":\"4009307468250838284\",\"impid\":\"Banner_300x250\",\"price\":0.5,\"adm\":\"<script></script>\",\"adid\":\"73501515\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=73501515\",\"cid\":\"958\",\"crid\":\"73501515\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"targeting\":{\"hb_bidder\":\"appnexus\",\"hb_bidder_appnexus\":\"appnexus\",\"hb_creative_loadtype\":\"html\",\"hb_env\":\"mobile-app\",\"hb_env_appnexus\":\"mobile-app\",\"hb_pb\":\"0.50\",\"hb_pb_appnexus\":\"0.50\",\"hb_size\":\"300x250\",\"hb_size_appnexus\":\"300x250\"},\"type\":\"banner\"},\"bidder\":{\"appnexus\":{\"brand_id\":1,\"auction_id\":7466795334738195000,\"bidder_id\":2,\"bid_ad_type\":0}}}}],\"seat\":\"appnexus\"},{\"bid\":[{\"id\":\"4009307468250838284\",\"impid\":\"Banner_300x250\",\"price\":0.4,\"adm\":\"<script></script>\",\"adid\":\"73501515\",\"adomain\":[\"rubicon.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=73501515\",\"cid\":\"958\",\"crid\":\"73501515\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"targeting\":{\"hb_bidder\":\"rubicon\",\"hb_bidder_rubicon\":\"rubicon\",\"hb_creative_loadtype\":\"html\",\"hb_env\":\"mobile-app\",\"hb_env_rubicon\":\"mobile-app\",\"hb_pb\":\"0.50\",\"hb_pb_rubicon\":\"0.50\",\"hb_size\":\"300x250\",\"hb_size_rubicon\":\"300x250\"},\"type\":\"banner\"}}}],\"seat\":\"rubicon\"}],\"ext\":{\"responsetimemillis\":{\"appnexus\":19}}}";
        JSONObject serverResponseJson = new JSONObject(serverResponse);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        ArrayList<AdUnit> units = new ArrayList<>();
        final BannerAdUnit bannerAdUnit = new BannerAdUnit("Banner_300x250", "12345");
        bannerAdUnit.addSize(300, 250);
        units.add(bannerAdUnit);
        BidManager.BidResponseListener listener = new BidManager.BidResponseListener() {
            @Override
            public void onBidSuccess(AdUnit bidRequest, ArrayList<BidResponse> bidResponses) {
                fail("this should never be called.");
            }

            @Override
            public void onBidFailure(AdUnit bidRequest, ErrorCode reason) {
                assertTrue(bidRequest.equals(bannerAdUnit));
                assertEquals(ErrorCode.NO_BIDS, reason);
            }
        };
        adapter.requestBid(activity, listener, units);
        adapter.onServerResponded(serverResponseJson);

    }

    @Test
    public void testNoCacheIdForTopBidButHasCacheIdForLowerBidsResponseProcessingForMoPub() throws Exception {
        setAdServer(Prebid.AdServer.MOPUB);
        // cached bid response
        String serverResponse = "{\"id\":\"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\"seatbid\":[{\"bid\":[{\"id\":\"4009307468250838284\",\"impid\":\"Banner_300x250\",\"price\":0.5,\"adm\":\"<script></script>\",\"adid\":\"73501515\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=73501515\",\"cid\":\"958\",\"crid\":\"73501515\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"targeting\":{\"hb_bidder\":\"appnexus\",\"hb_cache_id_appnexus\":\"random-value-appnexus\",\"hb_bidder_appnexus\":\"appnexus\",\"hb_creative_loadtype\":\"html\",\"hb_env\":\"mobile-app\",\"hb_env_appnexus\":\"mobile-app\",\"hb_pb\":\"0.50\",\"hb_pb_appnexus\":\"0.50\",\"hb_size\":\"300x250\",\"hb_size_appnexus\":\"300x250\"},\"type\":\"banner\"},\"bidder\":{\"appnexus\":{\"brand_id\":1,\"auction_id\":7466795334738195000,\"bidder_id\":2,\"bid_ad_type\":0}}}}],\"seat\":\"appnexus\"},{\"bid\":[{\"id\":\"4009307468250838284\",\"impid\":\"Banner_300x250\",\"price\":0.4,\"adm\":\"<script></script>\",\"adid\":\"73501515\",\"adomain\":[\"rubicon.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=73501515\",\"cid\":\"958\",\"crid\":\"73501515\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"targeting\":{\"hb_cache_id_rubicon\":\"random-value-rubicon\",\"hb_bidder_rubicon\":\"rubicon\",\"hb_creative_loadtype\":\"html\",\"hb_env_rubicon\":\"mobile-app\",\"hb_pb_rubicon\":\"0.40\",\"hb_size_rubicon\":\"300x250\"},\"type\":\"banner\"}}}],\"seat\":\"rubicon\"}],\"ext\":{\"responsetimemillis\":{\"appnexus\":19}}}";
        JSONObject serverResponseJson = new JSONObject(serverResponse);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        ArrayList<AdUnit> units = new ArrayList<>();
        final BannerAdUnit bannerAdUnit = new BannerAdUnit("Banner_300x250", "12345");
        bannerAdUnit.addSize(300, 250);
        units.add(bannerAdUnit);
        BidManager.BidResponseListener listener = new BidManager.BidResponseListener() {
            @Override
            public void onBidSuccess(AdUnit bidRequest, ArrayList<BidResponse> bidResponses) {
                fail("this should never be called.");
            }

            @Override
            public void onBidFailure(AdUnit bidRequest, ErrorCode reason) {
                assertTrue(bidRequest.equals(bannerAdUnit));
                assertEquals(ErrorCode.NO_BIDS, reason);
            }
        };
        adapter.requestBid(activity, listener, units);
        adapter.onServerResponded(serverResponseJson);
    }

    @Test
    public void testSingleResponseProcessingForDFP() throws Exception {
        // cached bid response
        setAdServer(Prebid.AdServer.DFP);
        CacheManager.init(activity);
        String serverResponse = "{\"id\":\"c7043990-2ac9-4ee5-a3d5-f3c032edeb4f\",\"seatbid\":[{\"bid\":[{\"id\":\"783432982278071921\",\"impid\":\"Banner_300x250\",\"price\":0.5,\"adm\":\"<script type=\\\"application/javascript\\\" src=\\\"https://nym1-ib.adnxs.com/ab?e=wqT_3QLrB6DrAwAAAwDWAAUBCMmnndMFELjCi9vSqcHVPhi2sNeng8GDl0AqNgkAAAECCOA_EQEHNAAA4D8ZAAAAgOtR4D8hERIAKREJADERG6Aw8ub8BDi-B0C-B0gCUNbLkw5Y4YBIYABokUB48c8EgAEBigEDVVNEkgUG8FKYAawCoAH6AagBAbABALgBAsABA8gBAtABAtgBAOABAPABAIoCOnVmKCdhJywgNDk0NDcyLCAxNTE2NzIxMDk3KTt1ZigncicsIDI5NjgxMTEwLDIeAPCQkgL9ASFxelVwZ1FpNjBJY0VFTmJMa3c0WUFDRGhnRWd3QURnQVFBUkl2Z2RROHViOEJGZ0FZTllCYUFCd0pIakFsd0dBQVNTSUFjQ1hBWkFCQVpnQkFhQUJBYWdCQTdBQkFMa0JLWXVJZ3dBQTREX0JBU21MaUlNQUFPQV95UUhXQWhrSXdDZndQOWtCQUFBQQEDJDhEX2dBUUQxQVEBDixDWUFnQ2dBZ0MxQWcFEAA5CQjwUERBQWdESUFnRGdBZ0RvQWdENEFnQ0FBd1NRQXdDWUF3R29BN3JRaHdTNkF4RmtaV1poZFd4MEkwNVpUVEk2TXpnd01RLi6aAjkhX0F0bl9naTIAAfA-NFlCSUlBUW9BRG9SWkdWbVlYVnNkQ05PV1UweU9qTTRNREUu0gJ1MTAwMTIzODQsMTQ2NzE1NywxNzUwNjEzDQh0NCwyMTQ0MjUyLDI1MjA5NywxMDYwMDUsMTQyMzUxAQjwYzM5MTIzLDIxMTcyMjgsNzcxODkxNSw3ODY5OTM5LDc4OTQzNTEsMTgyMjk1LDIzMzg2OTnYAugH4ALH0wHyAhAKBkFEVl9JRBIGNDk0NDcy8gIRCgZDUEdfSUQSBzE5Nzc5MzMBJwgFQ1ABJvC3Bzg1MTM1OTSAAwGIAwGQAwCYAxSgAwGqAwDAA6wCyAMA2AMA4AMA6AMC-AMAgAQAkgQJL29wZW5ydGIymAQAogQPMjA3LjIzNy4xNTAuMjQ2qASavA-yBAwIABAAGAAgADAAOAC4BADABADIBADSBBFkZWZhdWx0I05ZTTI6MzgwMdoEAggB4AQA8ATWy5MOggUZb3JnLnByZWJpZC5tb2JpbGUuZGVtb2FwcIgFAZgFAKAF_____wUDsAGqBSRjNzA0Mzk5MC0yYWM5LTRlZTUtYTNkNS1mM2MwMzJlZGViNGbABQDJBWl5FPA_0gUJCQkMUAAA2AUB4AUB8AUB-gUECAAQAJAGAA..&s=600eb78580716409e28a1a448675a3655a79ca3f&pp=${AUCTION_PRICE}\\\"></script>\",\"adid\":\"29681110\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=29681110\",\"cid\":\"958\",\"crid\":\"29681110\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"targeting\":{\"hb_bidder\":\"appnexus\",\"hb_bidder_appnexus\":\"appnexus\",\"hb_creative_loadtype\":\"html\",\"hb_pb\":\"0.50\",\"hb_pb_appnexus\":\"0.50\",\"hb_size\":\"300x250\",\"hb_size_appnexus\":\"300x250\"},\"type\":\"banner\"},\"bidder\":{\"appnexus\":{\"brand_id\":1,\"auction_id\":4515708880367575600,\"bidder_id\":2}}}}],\"seat\":\"appnexus\"}],\"ext\":{\"responsetimemillis\":{\"appnexus\":36}}}";
        JSONObject serverResponseJson = new JSONObject(serverResponse);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        ArrayList<AdUnit> units = new ArrayList<>();
        final BannerAdUnit bannerAdUnit = new BannerAdUnit("Banner_300x250", "12345");
        bannerAdUnit.addSize(300, 250);
        units.add(bannerAdUnit);
        BidManager.BidResponseListener listener = new BidManager.BidResponseListener() {
            @Override
            public void onBidSuccess(AdUnit bidRequest, ArrayList<BidResponse> bidResponses) {
                assertTrue(bidRequest.equals(bannerAdUnit));
                assertTrue(bidResponses.size() == 1);
                assertTrue(bidResponses.get(0).getCreative().startsWith("Prebid_"));
                String creativeId = bidResponses.get(0).getCreative();
                assertTrue(bidResponses.get(0).getCpm() == 0.50);
                assertTrue(bidResponses.get(0).getCustomKeywords().size() == 9);
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_bidder", "appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id", creativeId)));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_bidder_appnexus", "appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id_appnexus", creativeId)));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_creative_loadtype", "html")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_pb", "0.50")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_pb_appnexus", "0.50")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_size", "300x250")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_size_appnexus", "300x250")));
            }

            @Override
            public void onBidFailure(AdUnit bidRequest, ErrorCode reason) {
                fail("this should never be called.");
            }
        };
        adapter.requestBid(activity, listener, units);
        adapter.onServerResponded(serverResponseJson);
    }

    @Test
    public void testMultipleResponseProcessingForDFP() throws Exception {
        // cached bid response
        setAdServer(Prebid.AdServer.DFP);
        CacheManager.init(activity);
        String serverResponse = "{\"id\":\"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\"seatbid\":[{\"bid\":[{\"id\":\"4009307468250838284\",\"impid\":\"Banner_300x250\",\"price\":0.5,\"adm\":\"<script></script>\",\"adid\":\"73501515\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=73501515\",\"cid\":\"958\",\"crid\":\"73501515\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"targeting\":{\"hb_bidder\":\"appnexus\",\"hb_bidder_appnexus\":\"appnexus\",\"hb_creative_loadtype\":\"html\",\"hb_env\":\"mobile-app\",\"hb_env_appnexus\":\"mobile-app\",\"hb_pb\":\"0.50\",\"hb_pb_appnexus\":\"0.50\",\"hb_size\":\"300x250\",\"hb_size_appnexus\":\"300x250\"},\"type\":\"banner\"},\"bidder\":{\"appnexus\":{\"brand_id\":1,\"auction_id\":7466795334738195000,\"bidder_id\":2,\"bid_ad_type\":0}}}}],\"seat\":\"appnexus\"},{\"bid\":[{\"id\":\"4009307468250838284\",\"impid\":\"Banner_300x250\",\"price\":0.4,\"adm\":\"<script></script>\",\"adid\":\"73501515\",\"adomain\":[\"rubicon.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=73501515\",\"cid\":\"958\",\"crid\":\"73501515\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"targeting\":{\"hb_bidder_rubicon\":\"rubicon\",\"hb_creative_loadtype\":\"html\",\"hb_env_rubicon\":\"mobile-app\",\"hb_pb_rubicon\":\"0.40\",\"hb_size_rubicon\":\"300x250\"},\"type\":\"banner\"}}}],\"seat\":\"rubicon\"}],\"ext\":{\"responsetimemillis\":{\"appnexus\":19}}}";
        JSONObject serverResponseJson = new JSONObject(serverResponse);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        ArrayList<AdUnit> units = new ArrayList<>();
        final BannerAdUnit bannerAdUnit = new BannerAdUnit("Banner_300x250", "12345");
        bannerAdUnit.addSize(300, 250);
        units.add(bannerAdUnit);
        BidManager.BidResponseListener listener = new BidManager.BidResponseListener() {
            @Override
            public void onBidSuccess(AdUnit bidRequest, ArrayList<BidResponse> bidResponses) {
                assertTrue(bidRequest.equals(bannerAdUnit));
                assertTrue(bidResponses.size() == 2);
                assertTrue(bidResponses.get(0).getCreative().startsWith("Prebid_"));
                assertTrue(bidResponses.get(1).getCreative().startsWith("Prebid_"));
                assertTrue(bidResponses.get(0).getCpm() == 0.50);
                assertTrue(bidResponses.get(1).getCpm() == 0.40);
                assertTrue(bidResponses.get(0).getCustomKeywords().size() == 11);
                String creativeId = bidResponses.get(0).getCreative();
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_bidder", "appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_bidder_appnexus", "appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id", creativeId)));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id_appnexus", creativeId)));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_creative_loadtype", "html")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_env", "mobile-app")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_env_appnexus", "mobile-app")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_pb", "0.50")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_pb_appnexus", "0.50")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_size", "300x250")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_size_appnexus", "300x250")));
                assertTrue(bidResponses.get(1).getCustomKeywords().size() == 6);
                creativeId = bidResponses.get(1).getCreative();
                assertTrue(bidResponses.get(1).getCustomKeywords().contains(new Pair<String, String>("hb_bidder_rubicon", "rubicon")));
                assertTrue(bidResponses.get(1).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id_rubicon", creativeId)));
                assertTrue(bidResponses.get(1).getCustomKeywords().contains(new Pair<String, String>("hb_creative_loadtype", "html")));
                assertTrue(bidResponses.get(1).getCustomKeywords().contains(new Pair<String, String>("hb_env_rubicon", "mobile-app")));
                assertTrue(bidResponses.get(1).getCustomKeywords().contains(new Pair<String, String>("hb_pb_rubicon", "0.40")));
                assertTrue(bidResponses.get(1).getCustomKeywords().contains(new Pair<String, String>("hb_size_rubicon", "300x250")));
                assertTrue(!bidResponses.get(1).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id", creativeId)));
            }

            @Override
            public void onBidFailure(AdUnit bidRequest, ErrorCode reason) {
                fail("this should never be called.");
            }
        };
        adapter.requestBid(activity, listener, units);
        adapter.onServerResponded(serverResponseJson);
    }

    @Test
    public void testMultipleResponseForTheSameBidderProcessingForDFP() throws Exception {
        // cached bid response
        setAdServer(Prebid.AdServer.DFP);
        CacheManager.init(activity);
        String serverResponse = "{\"id\":\"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\"seatbid\":[{\"bid\":[{\"id\":\"3829649260126183529\",\"impid\":\"Banner_300x250\",\"price\":15,\"adm\":\"hello world\",\"adid\":\"68501584\",\"adomain\":[\"peugeot.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=68501584\",\"cid\":\"958\",\"crid\":\"68501584\",\"cat\":[\"IAB2-3\",\"IAB2-6\",\"IAB2-15\",\"IAB2-10\",\"IAB2-11\",\"IAB2-12\",\"IAB2-19\",\"IAB2-4\",\"IAB2-13\",\"IAB2\",\"IAB2-17\",\"IAB2-8\",\"IAB2-16\",\"IAB2-18\",\"IAB2-9\",\"IAB2-22\",\"IAB2-14\",\"IAB2-7\",\"IAB2-20\",\"IAB2-1\",\"IAB2-2\",\"IAB2-5\",\"IAB2-23\",\"IAB2-21\"],\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"targeting\":{\"hb_bidder\":\"appnexus\",\"hb_bidder_appnexus\":\"appnexus\",\"hb_creative_loadtype\":\"html\",\"hb_env\":\"mobile-app\",\"hb_env_appnexus\":\"mobile-app\",\"hb_pb\":\"15.00\",\"hb_pb_appnexus\":\"15.00\",\"hb_size\":\"300x250\",\"hb_size_appnexus\":\"300x250\"},\"type\":\"banner\"},\"bidder\":{\"appnexus\":{\"brand_id\":3264,\"auction_id\":8160292782530908000,\"bidder_id\":2,\"bid_ad_type\":0}}}},{\"id\":\"1389685956420146597\",\"impid\":\"Banner_300x250\",\"price\":3.21,\"adm\":\"hello world\",\"adid\":\"28477710\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=28477710\",\"cid\":\"958\",\"crid\":\"28477710\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"type\":\"banner\"},\"bidder\":{\"appnexus\":{\"brand_id\":1,\"auction_id\":8160292782530908000,\"bidder_id\":2,\"bid_ad_type\":0}}}},{\"id\":\"1106673435110511367\",\"impid\":\"Banner_300x250\",\"price\":0.033505,\"adm\":\"Hello world\",\"adid\":\"103077040\",\"adomain\":[\"audible.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=103077040\",\"cid\":\"1437\",\"crid\":\"103077040\",\"cat\":[\"IAB22-4\",\"IAB22\"],\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"type\":\"banner\"},\"bidder\":{\"appnexus\":{\"brand_id\":12,\"auction_id\":8160292782530908000,\"bidder_id\":101,\"bid_ad_type\":0}}}}],\"seat\":\"appnexus\"}],\"ext\":{\"responsetimemillis\":{\"appnexus\":258}}}";
        JSONObject serverResponseJson = new JSONObject(serverResponse);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        ArrayList<AdUnit> units = new ArrayList<>();
        final BannerAdUnit bannerAdUnit = new BannerAdUnit("Banner_300x250", "12345");
        bannerAdUnit.addSize(300, 250);
        units.add(bannerAdUnit);
        BidManager.BidResponseListener listener = new BidManager.BidResponseListener() {
            @Override
            public void onBidSuccess(AdUnit bidRequest, ArrayList<BidResponse> bidResponses) {
                assertTrue(bidRequest.equals(bannerAdUnit));
                assertTrue(bidResponses.size() == 1);
                assertTrue(bidResponses.get(0).getCreative().startsWith("Prebid_"));
                assertTrue(bidResponses.get(0).getCpm() == 15);
                assertTrue(bidResponses.get(0).getCustomKeywords().size() == 11);
                String creativeId = bidResponses.get(0).getCreative();
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_bidder", "appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_bidder_appnexus", "appnexus")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id", creativeId)));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id_appnexus", creativeId)));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_creative_loadtype", "html")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_env", "mobile-app")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_env_appnexus", "mobile-app")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_pb", "15.00")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_pb_appnexus", "15.00")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_size", "300x250")));
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_size_appnexus", "300x250")));
            }

            @Override
            public void onBidFailure(AdUnit bidRequest, ErrorCode reason) {
                fail("this should never be called.");
            }
        };
        adapter.requestBid(activity, listener, units);
        adapter.onServerResponded(serverResponseJson);
    }


    @Test
    public void testMultipleBidsWithSamePriceResponseProcessingForDFP() throws Exception {
        // cached bid response
        setAdServer(Prebid.AdServer.DFP);
        CacheManager.init(activity);
        String serverResponse = "{\"id\":\"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\"seatbid\":[{\"bid\":[{\"id\":\"4009307468250838284\",\"impid\":\"Banner_300x250\",\"price\":0.5,\"adm\":\"<script></script>\",\"adid\":\"73501515\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=73501515\",\"cid\":\"958\",\"crid\":\"73501515\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"targeting\":{\"hb_bidder\":\"appnexus\",\"hb_bidder_appnexus\":\"appnexus\",\"hb_creative_loadtype\":\"html\",\"hb_env\":\"mobile-app\",\"hb_env_appnexus\":\"mobile-app\",\"hb_pb\":\"0.50\",\"hb_pb_appnexus\":\"0.50\",\"hb_size\":\"300x250\",\"hb_size_appnexus\":\"300x250\"},\"type\":\"banner\"},\"bidder\":{\"appnexus\":{\"brand_id\":1,\"auction_id\":7466795334738195000,\"bidder_id\":2,\"bid_ad_type\":0}}}}],\"seat\":\"appnexus\"},{\"bid\":[{\"id\":\"4009307468250838284\",\"impid\":\"Banner_300x250\",\"price\":0.5,\"adm\":\"<script></script>\",\"adid\":\"73501515\",\"adomain\":[\"rubicon.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=73501515\",\"cid\":\"958\",\"crid\":\"73501515\",\"w\":300,\"h\":250,\"ext\":{\"prebid\":{\"targeting\":{\"hb_bidder_rubicon\":\"rubicon\",\"hb_creative_loadtype\":\"html\",\"hb_env_rubicon\":\"mobile-app\",\"hb_pb_rubicon\":\"0.50\",\"hb_size_rubicon\":\"300x250\"},\"type\":\"banner\"}}}],\"seat\":\"rubicon\"}],\"ext\":{\"responsetimemillis\":{\"appnexus\":19}}}s";
        JSONObject serverResponseJson = new JSONObject(serverResponse);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        ArrayList<AdUnit> units = new ArrayList<>();
        final BannerAdUnit bannerAdUnit = new BannerAdUnit("Banner_300x250", "12345");
        bannerAdUnit.addSize(300, 250);
        units.add(bannerAdUnit);
        BidManager.BidResponseListener listener = new BidManager.BidResponseListener() {
            @Override
            public void onBidSuccess(AdUnit bidRequest, ArrayList<BidResponse> bidResponses) {
                assertTrue(bidRequest.equals(bannerAdUnit));
                assertTrue(bidResponses.size() == 2);
                assertTrue(bidResponses.get(0).getCreative().startsWith("Prebid_"));
                assertTrue(bidResponses.get(1).getCreative().startsWith("Prebid_"));
                assertTrue(bidResponses.get(0).getCpm() == 0.50);
                assertTrue(bidResponses.get(1).getCpm() == 0.50);
                for (int i = 0; i < bidResponses.size(); i++) {
                    boolean isTopBid = false;
                    for (Pair<String, String> pair : bidResponses.get(i).getCustomKeywords()) {
                        if (pair.first.equals("hb_bidder")) {
                            isTopBid = true;
                        }
                    }
                    String creativeId = bidResponses.get(i).getCreative();
                    if (isTopBid) {
                        assertTrue(bidResponses.get(i).getCustomKeywords().size() == 11);
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_bidder", "appnexus")));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_bidder_appnexus", "appnexus")));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id", creativeId)));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id_appnexus", creativeId)));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_creative_loadtype", "html")));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_env", "mobile-app")));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_env_appnexus", "mobile-app")));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_pb", "0.50")));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_pb_appnexus", "0.50")));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_size", "300x250")));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_size_appnexus", "300x250")));
                    } else {
                        assertTrue(bidResponses.get(i).getCustomKeywords().size() == 6);
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_bidder_rubicon", "rubicon")));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id_rubicon", creativeId)));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_creative_loadtype", "html")));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_env_rubicon", "mobile-app")));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_pb_rubicon", "0.50")));
                        assertTrue(bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_size_rubicon", "300x250")));
                        assertTrue(!bidResponses.get(i).getCustomKeywords().contains(new Pair<String, String>("hb_cache_id", creativeId)));
                    }
                }


            }

            @Override
            public void onBidFailure(AdUnit bidRequest, ErrorCode reason) {
                fail("this should never be called.");
            }
        };
        adapter.requestBid(activity, listener, units);
        adapter.onServerResponded(serverResponseJson);
    }

    @Test
    public void testRequestTimeOutMillis() {
        // assert default value
        assertEquals(500, Settings.connectionTimeOutMillis);
        // test setter
        Settings.setConnectionTimeOutMillis(1000);
        assertEquals(1000, Settings.getConnectionTimeOutMillis());
    }

    @Test
    public void testGetArrayListForBatchCall() throws Exception {
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        // pass in a single ad unit
        BannerAdUnit adUnit = new BannerAdUnit("B", "0");
        adUnit.addSize(300, 250);
        ArrayList<AdUnit> adUnits = new ArrayList<>();
        adUnits.add(adUnit);
        ArrayList<ArrayList<AdUnit>> results = adapter.batchAdUnits(adUnits);
        assertTrue(results.size() == 1);
        assertTrue(results.get(0).size() == 1);
        // pass in 13 ad units
        BannerAdUnit adUnit1 = new BannerAdUnit("B1", "1");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit2 = new BannerAdUnit("B2", "2");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit3 = new BannerAdUnit("B3", "3");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit4 = new BannerAdUnit("B4", "4");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit5 = new BannerAdUnit("B5", "5");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit6 = new BannerAdUnit("B6", "6");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit7 = new BannerAdUnit("B7", "7");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit8 = new BannerAdUnit("B8", "8");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit9 = new BannerAdUnit("B9", "9");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit10 = new BannerAdUnit("B10", "10");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit11 = new BannerAdUnit("B11", "11");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit12 = new BannerAdUnit("B12", "12");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit13 = new BannerAdUnit("B13", "13");
        adUnit.addSize(300, 250);
        adUnits.add(adUnit1);
        adUnits.add(adUnit2);
        adUnits.add(adUnit3);
        adUnits.add(adUnit4);
        adUnits.add(adUnit5);
        adUnits.add(adUnit6);
        adUnits.add(adUnit7);
        adUnits.add(adUnit8);
        adUnits.add(adUnit9);
        adUnits.add(adUnit10);
        adUnits.add(adUnit11);
        adUnits.add(adUnit12);
        adUnits.add(adUnit13);
        results = adapter.batchAdUnits(adUnits);
        assertTrue(results.size() == 2);
        assertTrue(results.get(0).size() == 10);
        assertTrue(results.get(1).size() == 4);
        BannerAdUnit adUnit14 = new BannerAdUnit("B1", "1");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit15 = new BannerAdUnit("B2", "2");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit16 = new BannerAdUnit("B3", "3");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit17 = new BannerAdUnit("B4", "4");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit18 = new BannerAdUnit("B5", "5");
        adUnit.addSize(300, 250);
        BannerAdUnit adUnit19 = new BannerAdUnit("B6", "6");
        adUnit.addSize(300, 250);
        adUnits.add(adUnit14);
        adUnits.add(adUnit15);
        adUnits.add(adUnit16);
        adUnits.add(adUnit17);
        adUnits.add(adUnit18);
        adUnits.add(adUnit19);
        results = adapter.batchAdUnits(adUnits);
        assertTrue(results.size() == 2);
        assertTrue(results.get(0).size() == 10);
        assertTrue(results.get(1).size() == 10);
    }

    @Test
    public void testGetHost() throws Exception {
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        // default value is AppNexus Secure host
        assertEquals(Settings.APPNEXUS_REQUEST_URL_SECURE, adapter.getHost());
        Field shouldSecureField = Prebid.class.getDeclaredField("secureConnection");
        shouldSecureField.setAccessible(true);
        shouldSecureField.set(null, false);
        assertEquals(Settings.APPNEXUS_REQUEST_URL_NON_SECURE, adapter.getHost());
        Field host = Prebid.class.getDeclaredField("host");
        host.setAccessible(true);
        host.set(null, Prebid.Host.RUBICON);
        assertEquals(Settings.RUBICON_REQUEST_URL_NON_SECURE, adapter.getHost());
        shouldSecureField.set(null, true);
        assertEquals(Settings.RUBICON_REQUEST_URL_SECURE, adapter.getHost());
    }
}
