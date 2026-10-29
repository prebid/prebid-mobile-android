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

package org.prebid.mobile.api.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.CacheManager;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.configuration.NativeAdUnitConfiguration;
import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
public class BidInfoTest {

    // A designated bid with distinct crid, adid and response id, so swapped fields fail the tests.
    private static final String WINNING_BID_RESPONSE = """
            {
              "id": "req-abc",
              "cur": "USD",
              "seatbid": [{
                "seat": "seat-1",
                "bid": [{
                  "id": "bid-1",
                  "impid": "imp-1",
                  "price": 3.14,
                  "crid": "creative-xyz",
                  "adid": "ad-123",
                  "ext": { "prebid": {
                    "targeting": { "hb_pb": "3.10", "hb_bidder": "somebidder", "hb_cache_id": "cache-1" },
                    "type": "banner"
                  }}
                }]
              }]
            }
            """;

    @Before
    public void setUp() {
        CacheManager.clear();
    }

    @After
    public void tearDown() {
        PrebidMobile.setFilterOutUncachedBids(false);
    }

    @Test
    public void createWithDesignatedBid_SurfacesBidEconomics() {
        BidInfo bidInfo = BidInfo.create(ResultCode.SUCCESS, parse(WINNING_BID_RESPONSE), null);

        assertEquals(Double.valueOf(3.14), bidInfo.getPrice());
        assertEquals("USD", bidInfo.getCurrency());
        assertEquals("creative-xyz", bidInfo.getCreativeId());
        assertEquals("ad-123", bidInfo.getAdId());
        assertEquals("req-abc", bidInfo.getRequestId());
    }

    @Test
    public void createWithResponseCurrency_ForwardsItAsSent() throws JSONException {
        JSONObject response = new JSONObject(WINNING_BID_RESPONSE).put("cur", "EUR");

        BidInfo bidInfo = create(response);

        assertEquals("EUR", bidInfo.getCurrency());
    }

    @Test
    public void createWithoutResponseCurrency_CurrencyIsNull() throws JSONException {
        JSONObject response = new JSONObject(WINNING_BID_RESPONSE);
        response.remove("cur");

        BidInfo bidInfo = create(response);

        assertNotNull(bidInfo.getPrice());
        assertNull(bidInfo.getCurrency());
    }

    @Test
    public void createWithoutBidPrice_PriceIsNullNotZero() throws JSONException {
        JSONObject response = new JSONObject(WINNING_BID_RESPONSE);
        getBid(response).remove("price");

        BidInfo bidInfo = create(response);

        assertNull(bidInfo.getPrice());
        assertEquals("creative-xyz", bidInfo.getCreativeId());
    }

    @Test
    public void createWithoutDesignatedBid_BidFieldsAreNullAndRequestIdIsSet() throws JSONException {
        JSONObject response = new JSONObject(WINNING_BID_RESPONSE);
        getBid(response).getJSONObject("ext").getJSONObject("prebid").getJSONObject("targeting").remove("hb_bidder");
        BidResponse bidResponse = parse(response.toString());
        assertNull(bidResponse.getWinningBid());

        BidInfo bidInfo = BidInfo.create(ResultCode.SUCCESS, bidResponse, null);

        assertEquals("cache-1", bidInfo.getTargetingKeywords().get("hb_cache_id"));
        assertNull(bidInfo.getPrice());
        assertNull(bidInfo.getCurrency());
        assertNull(bidInfo.getCreativeId());
        assertNull(bidInfo.getAdId());
        assertEquals("req-abc", bidInfo.getRequestId());
    }

    @Test
    public void createWithPromotedRunnerUp_DescribesPromotedBid() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("BidResponseTest/top_bid_uncached_runner_up_cached.json");
        PrebidMobile.setFilterOutUncachedBids(true);

        BidInfo bidInfo = BidInfo.create(ResultCode.SUCCESS, parse(responseString), null);

        assertTrue(bidInfo.isTopBidFiltered());
        assertEquals(Double.valueOf(0.10), bidInfo.getPrice());
        assertEquals("USD", bidInfo.getCurrency());
    }

    @Test
    public void createWithoutBidResponse_BidEconomicsAreNull() {
        BidInfo bidInfo = BidInfo.create(ResultCode.NO_BIDS, null, null);

        assertNull(bidInfo.getPrice());
        assertNull(bidInfo.getCurrency());
        assertNull(bidInfo.getCreativeId());
        assertNull(bidInfo.getAdId());
        assertNull(bidInfo.getRequestId());
    }

    @Test
    public void createNativeBidInfoWithExpiration_CachedBidExpiresAfterExp() {
        BidInfo bidInfo = BidInfo.create(ResultCode.SUCCESS, createNativeBidResponse(1), createNativeConfiguration());

        assertNotNull(bidInfo.getNativeCacheId());
        assertTrue(CacheManager.isValid(bidInfo.getNativeCacheId()));

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(CacheManager.isValid(bidInfo.getNativeCacheId()));
    }

    @Test
    public void createNativeBidInfoWithoutExpiration_CachedBidUsesDefaultExpiry() {
        BidInfo bidInfo = BidInfo.create(ResultCode.SUCCESS, createNativeBidResponse(null), createNativeConfiguration());

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertTrue(CacheManager.isValid(bidInfo.getNativeCacheId()));
    }

    @Test
    public void createNativeBidInfo_BidAlreadyCached_ReusesCacheIdWithoutSaving() {
        String cacheId = CacheManager.save("{\"adm\":\"native\"}");
        BidResponse bidResponse = createNativeBidResponse(null);
        when(bidResponse.getNativeCacheId()).thenReturn(cacheId);

        BidInfo bidInfo = BidInfo.create(ResultCode.SUCCESS, bidResponse, createNativeConfiguration());

        assertEquals(cacheId, bidInfo.getNativeCacheId());
        assertEquals(cacheId, bidInfo.getTargetingKeywords().get(BidResponse.KEY_CACHE_ID));
        HashMap<String, String> savedValues = Reflection.getStaticFieldOf(CacheManager.class, "savedValues");
        assertEquals(1, savedValues.size());
    }

    @Test
    public void createNativeBidInfoTwice_SameResponse_CachesBidOnce() throws IOException {
        AdUnitConfiguration configuration = createNativeConfiguration();
        BidResponse bidResponse = new BidResponse(
                ResourceUtils.convertResourceToString("BidResponseTest/keywords_all_with_cache_id.json"),
                configuration
        );

        BidInfo first = BidInfo.create(ResultCode.SUCCESS, bidResponse, configuration);
        BidInfo second = BidInfo.create(ResultCode.SUCCESS, bidResponse, configuration);

        assertNotNull(first.getNativeCacheId());
        assertEquals(first.getNativeCacheId(), second.getNativeCacheId());
        HashMap<String, String> savedValues = Reflection.getStaticFieldOf(CacheManager.class, "savedValues");
        assertEquals(1, savedValues.size());
    }

    private static BidResponse createNativeBidResponse(Integer exp) {
        BidResponse bidResponse = mock(BidResponse.class);
        when(bidResponse.getTargeting()).thenReturn(new HashMap<>());
        when(bidResponse.getWinningBidJson()).thenReturn("{\"adm\":\"native\"}");
        when(bidResponse.getExpirationTimeSeconds()).thenReturn(exp);
        return bidResponse;
    }

    private static AdUnitConfiguration createNativeConfiguration() {
        AdUnitConfiguration configuration = new AdUnitConfiguration();
        configuration.setNativeConfiguration(new NativeAdUnitConfiguration());
        return configuration;
    }

    private static BidInfo create(JSONObject response) {
        return BidInfo.create(ResultCode.SUCCESS, parse(response.toString()), null);
    }

    private static BidResponse parse(String json) {
        AdUnitConfiguration configuration = new AdUnitConfiguration();
        configuration.setIsOriginalAdUnit(true);
        return new BidResponse(json, configuration);
    }

    private static JSONObject getBid(JSONObject response) throws JSONException {
        return response.getJSONArray("seatbid").getJSONObject(0).getJSONArray("bid").getJSONObject(0);
    }

}
