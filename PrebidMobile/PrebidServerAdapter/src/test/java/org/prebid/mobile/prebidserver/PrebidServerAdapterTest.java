package org.prebid.mobile.prebidserver;


import android.util.Pair;

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
import org.prebid.mobile.prebidserver.internal.Settings;
import org.prebid.mobile.unittestutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, manifest = Config.NONE)
public class PrebidServerAdapterTest extends BaseSetup {

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
        Prebid.setAdServer(Prebid.AdServer.DFP);
        JSONObject postData = adapter.getPostData(activity, adUnits);
        assertTrue(postData.has(Settings.REQUEST_DEVICE));
        assertTrue(postData.has(Settings.REQUEST_APP));
        assertTrue(postData.has(Settings.REQUEST_USER));
        assertTrue("request data is missing 'id' field", postData.has("id"));
        assertTrue("request data is missing 'imp' field", postData.has("imp"));
        assertTrue("'imp' array should have 2 objects", postData.getJSONArray("imp").length() == 2);
        assertTrue(postData.getJSONArray("imp").getJSONObject(0).get("id").equals("banner"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(0).has("ext"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(0).has("secure"));
        // todo add test back after stored request is added in production
//        assertTrue(postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").has("prebid"));
//        assertTrue(postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("prebid").has("storedrequest"));
//        assertTrue(postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("prebid").getJSONObject("storedrequest").get("id").equals("12345"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).get("id").equals("interstitial"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).has("ext"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).has("secure"));
        // todo add test back after stored request is added in production
//        assertTrue(postData.getJSONArray("imp").getJSONObject(1).getJSONObject("ext").has("prebid"));
//        assertTrue(postData.getJSONArray("imp").getJSONObject(1).getJSONObject("ext").getJSONObject("prebid").has("storedrequest"));
//        assertTrue(postData.getJSONArray("imp").getJSONObject(1).getJSONObject("ext").getJSONObject("prebid").getJSONObject("storedrequest").get("id").equals("23456"));
        assertTrue(postData.has("ext"));
        assertTrue(postData.getJSONObject("ext").has("prebid"));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").has("targeting"));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("targeting").getString("pricegranularity").equals("medium"));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("targeting").getInt("lengthmax") == Settings.REQUEST_KEY_LENGTH_MAX);
//        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").has("cache"));
        // Test with MoPub settings
        Prebid.setAdServer(Prebid.AdServer.MOPUB);
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
        // todo add test back after stored request is added in production
//        assertTrue(postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").has("prebid"));
//        assertTrue(postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("prebid").has("storedrequest"));
//        assertTrue(postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("prebid").getJSONObject("storedrequest").get("id").equals("12345"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).get("id").equals("interstitial"));
        assertTrue(postData.getJSONArray("imp").getJSONObject(1).has("ext"));
        assertTrue(!postData.getJSONArray("imp").getJSONObject(1).has("secure"));
        // todo add test back after stored request is added in production
//        assertTrue(postData.getJSONArray("imp").getJSONObject(1).getJSONObject("ext").has("prebid"));
//        assertTrue(postData.getJSONArray("imp").getJSONObject(1).getJSONObject("ext").getJSONObject("prebid").has("storedrequest"));
//        assertTrue(postData.getJSONArray("imp").getJSONObject(1).getJSONObject("ext").getJSONObject("prebid").getJSONObject("storedrequest").get("id").equals("23456"));
        assertTrue(postData.has("ext"));
        assertTrue(postData.getJSONObject("ext").has("prebid"));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").has("targeting"));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("targeting").getString("pricegranularity").equals("medium"));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("targeting").getInt("lengthmax") == Settings.REQUEST_KEY_LENGTH_MAX);
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").has("cache"));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("cache").has("bids"));
        assertTrue(postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("cache").getJSONObject("bids").length() == 0);


    }

    @Test
    public void testResponseProcessingForMoPub() throws Exception {
        Prebid.setAdServer(Prebid.AdServer.MOPUB);
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

            }
        };
        adapter.requestBid(activity, listener, units);
        adapter.onServerResponded(serverResponseJson);
    }

    @Test
    public void testResponseProcessingForDFP() throws Exception {
        // cached bid response
        Prebid.setAdServer(Prebid.AdServer.DFP);
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
                assertTrue(bidResponses.get(0).getCustomKeywords().size() == 8);
                assertTrue(bidResponses.get(0).getCustomKeywords().contains(new Pair<String, String>("hb_bidder", "appnexus")));
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
}
