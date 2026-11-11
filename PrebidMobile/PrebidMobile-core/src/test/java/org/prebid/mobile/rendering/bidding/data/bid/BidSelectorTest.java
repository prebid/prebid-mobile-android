/*
 *    Copyright 2018-2026 Prebid.org, Inc.
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

package org.prebid.mobile.rendering.bidding.data.bid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.FetchDemandResult;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.plugin.PluginCapability;
import org.prebid.mobile.api.plugin.PrebidPluginInfo;
import org.prebid.mobile.api.rendering.BannerView;
import org.prebid.mobile.api.rendering.InterstitialAdUnit;
import org.prebid.mobile.api.rendering.RewardedAdUnit;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.rendering.bidding.interfaces.BannerEventHandler;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialEventHandler;
import org.prebid.mobile.rendering.bidding.interfaces.RewardedEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.modelcontrollers.BidRequester;
import org.prebid.mobile.rendering.utils.helpers.RefreshTimerTask;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BidSelectorTest {

    private static final String CONFIG_ID = "config-id";

    private AdUnitConfiguration adUnitConfiguration;
    private BidRequesterListener listener;

    @Before
    public void setUp() {
        adUnitConfiguration = new AdUnitConfiguration();
        adUnitConfiguration.setConfigId(CONFIG_ID);
        adUnitConfiguration.setAdFormat(AdFormat.BANNER);
        listener = mock(BidRequesterListener.class);
    }

    @After
    public void tearDown() {
        PluginCapability.BID_SELECTOR.getRegistry().removeAll();
        PrebidMobile.setFilterOutUncachedBids(false);
        PrebidMobile.setUseCacheForReportingWithRenderingApi(false);
        // The response handler folds tmaxrequest into these statics
        WhiteBox.setStaticVariableTo(BidLoader.class, "sTimeoutHasChanged", false);
        WhiteBox.setStaticVariableTo(PrebidMobile.class, "timeoutMillis", 2_000);
        WhiteBox.setStaticVariableTo(PrebidMobile.class, "isTimeoutModified", false);
    }

    // region Registration

    @Test
    public void registerAndUnregister() {
        StubSelector selector = new StubSelector("test-selector", bids -> bids.get(0));

        PrebidMobile.registerPlugin(selector, PluginCapability.BID_SELECTOR);

        assertTrue(PrebidMobile.containsPlugin(selector, PluginCapability.BID_SELECTOR));
        assertEquals(1, selector.registerCount);

        PrebidMobile.unregisterPlugin(selector, PluginCapability.BID_SELECTOR);

        assertFalse(PrebidMobile.containsPlugin(selector, PluginCapability.BID_SELECTOR));
        assertEquals(1, selector.unregisterCount);
    }

    @Test
    public void onlyOneSelectorIsRegistered() {
        StubSelector first = new StubSelector("first", bids -> bids.get(0));
        StubSelector second = new StubSelector("second", bids -> bids.get(0));

        PrebidMobile.registerPlugin(first, PluginCapability.BID_SELECTOR);
        PrebidMobile.registerPlugin(second, PluginCapability.BID_SELECTOR);

        assertTrue(PrebidMobile.containsPlugin(first, PluginCapability.BID_SELECTOR));
        // A second selector is ignored while one is registered
        assertFalse(PrebidMobile.containsPlugin(second, PluginCapability.BID_SELECTOR));
        assertEquals(0, second.registerCount);

        PrebidMobile.unregisterPlugin(first, PluginCapability.BID_SELECTOR);
        PrebidMobile.registerPlugin(second, PluginCapability.BID_SELECTOR);

        assertTrue(PrebidMobile.containsPlugin(second, PluginCapability.BID_SELECTOR));
    }

    // endregion

    // region Without a selector

    @Test
    public void withoutSelector_serverWinnerWins() throws Exception {
        BidResponse response = loadExpectingSuccess(response(serverWinner(), runnerUp()));

        assertEquals(0.75, response.getWinningBid().getPrice(), 0.001);
        assertEquals("openx", response.getTargeting().get("hb_bidder"));
        // The losing bid's keys are merged in, as before
        assertEquals("2.00", response.getTargeting().get("hb_pb_appnexus"));
    }

    // endregion

    // region Selection

    @Test
    public void selectorChoosesTheWinner() throws Exception {
        StubSelector selector = register(BidSelectorTest::highestPrice);

        BidResponse response = loadExpectingSuccess(response(serverWinner(), runnerUp()));

        assertEquals(2.00, response.getWinningBid().getPrice(), 0.001);
        // The selector is called once per auction, on the main thread
        assertEquals(1, selector.calls.size());
        assertEquals(2, selector.calls.get(0).size());
        assertTrue(selector.calledOnMainThread);
        assertEquals(CONFIG_ID, selector.lastContext.getConfigId());
        assertEquals(EnumSet.of(AdFormat.BANNER), selector.lastContext.getAdFormats());
    }

    @Test
    public void targetingComesFromTheSelectedBidOnly() throws Exception {
        register(BidSelectorTest::highestPrice);

        BidResponse response = loadExpectingSuccess(response(serverWinner(), runnerUp()));
        HashMap<String, String> targeting = response.getTargeting();

        // The generic keys are filled in from the selected bid's bidder-specific keys
        assertEquals("2.00", targeting.get("hb_pb"));
        assertEquals("appnexus", targeting.get("hb_bidder"));
        assertEquals("320x50", targeting.get("hb_size"));
        assertEquals("appnexus-cache-id", targeting.get("hb_cache_id"));
        assertEquals("prebid-cache.example.com", targeting.get("hb_cache_host"));
        assertEquals("/cache", targeting.get("hb_cache_path"));
        assertEquals("2.00", targeting.get("hb_pb_appnexus"));

        // Nothing comes from the server's winner, not even a key the selected bid lacks
        assertNull(targeting.get("hb_deal"));
        assertNull(targeting.get("hb_pb_openx"));
        assertNull(targeting.get("hb_bidder_openx"));
        assertNull(targeting.get("hb_deal_openx"));

        // Integrations that read the winning bid's own keys (e.g. the GAM event handlers) see the same set
        assertEquals(targeting, response.getWinningBid().getPrebid().getTargeting());
        assertTrue(response.hasWinningKeywords(response.getWinningBid().getPrebid()));
        assertEquals(response.getWinningBid().getJsonString(), response.getWinningBidJson());
    }

    @Test
    public void selectorChoosingNoBid_endsTheAuctionWithoutWinner() throws Exception {
        register(bids -> null);

        AdException error = loadExpectingError(response(serverWinner(), runnerUp()));

        // The SDK doesn't fall back to the server's winner
        assertEquals(FetchDemandResult.NO_BIDS, FetchDemandResult.parseErrorMessage(error.getMessage()));
    }

    @Test
    public void selectorChoosingABidItWasNotGiven_endsTheAuctionWithoutWinner() throws Exception {
        // An equal bid from another response, e.g. one the selector kept from an earlier auction
        Bid foreignBid = new BidResponse(response(runnerUp()).toString(), adUnitConfiguration).getAllBids().get(0);
        HashMap<String, String> foreignTargeting = new HashMap<>(foreignBid.getPrebid().getTargeting());
        register(bids -> foreignBid);

        AdException error = loadExpectingError(response(serverWinner(), runnerUp()));

        assertEquals(FetchDemandResult.NO_BIDS, FetchDemandResult.parseErrorMessage(error.getMessage()));
        assertEquals(foreignTargeting, foreignBid.getPrebid().getTargeting());
    }

    // endregion

    // region Eligible bids

    @Test
    public void originalApiWithFilterOutUncachedBids_offersCachedBidsOnly() throws Exception {
        adUnitConfiguration.setIsOriginalAdUnit(true);
        PrebidMobile.setFilterOutUncachedBids(true);
        StubSelector selector = register(bids -> bids.get(0));

        BidResponse response = loadExpectingSuccess(response(serverWinner(), runnerUp()));

        // The server's winner has no Prebid Cache entry, so the selector never sees it
        assertEquals(1, selector.calls.get(0).size());
        assertEquals(2.00, response.getWinningBid().getPrice(), 0.001);
        assertFalse(response.isTopBidFiltered());
    }

    @Test
    public void noCachedBidPromotion_whenTheSelectorChoosesNoBid() throws Exception {
        adUnitConfiguration.setIsOriginalAdUnit(true);
        PrebidMobile.setFilterOutUncachedBids(true);
        register(bids -> null);

        AdException error = loadExpectingError(response(serverWinner(), runnerUp()));

        // The cached runner-up is not promoted in place of the selector's decision
        assertEquals(FetchDemandResult.NO_BIDS, FetchDemandResult.parseErrorMessage(error.getMessage()));
    }

    @Test
    public void selectorIsNotCalled_whenNoBidIsCached() throws Exception {
        adUnitConfiguration.setIsOriginalAdUnit(true);
        PrebidMobile.setFilterOutUncachedBids(true);
        StubSelector selector = register(bids -> bids.get(0));

        AdException error = loadExpectingError(response(serverWinner()));

        assertTrue(selector.calls.isEmpty());
        assertEquals(FetchDemandResult.NO_CACHED_BIDS, FetchDemandResult.parseErrorMessage(error.getMessage()));
    }

    @Test
    public void renderingApiHasNoServerCacheRequirement() throws Exception {
        PrebidMobile.setFilterOutUncachedBids(true);
        StubSelector selector = register(bids -> bids.get(0));

        BidResponse response = loadExpectingSuccess(response(serverWinner(), runnerUp()));

        assertEquals(2, selector.calls.get(0).size());
        assertEquals(0.75, response.getWinningBid().getPrice(), 0.001);
    }

    // endregion

    // region Targeting validation

    @Test
    public void bidWithoutWinnerKeys_isInvalidForAnAdServer() throws Exception {
        register(bids -> findByPrice(bids, 3.00));

        AdException error = loadExpectingError(response(serverWinner(), bidWithoutTargeting()));

        FetchDemandResult result = FetchDemandResult.parseErrorMessage(error.getMessage());
        assertEquals(FetchDemandResult.INVALID_TARGETING, result);
        assertEquals(ResultCode.INVALID_TARGETING, result.toResultCode());
        assertEquals(FetchDemandResult.INVALID_TARGETING, FetchDemandResult.fromResultCode(ResultCode.INVALID_TARGETING));
    }

    @Test
    public void cacheIdIsRequired_whenTheCacheIsUsed() throws Exception {
        PrebidMobile.setUseCacheForReportingWithRenderingApi(true);
        // The server's winner has no hb_cache_id
        register(bids -> findByPrice(bids, 0.75));

        AdException error = loadExpectingError(response(serverWinner()));

        assertEquals(FetchDemandResult.INVALID_TARGETING, FetchDemandResult.parseErrorMessage(error.getMessage()));
    }

    @Test
    public void bidWithoutTargeting_winsWhenRenderedWithoutAnAdServer() throws Exception {
        adUnitConfiguration.setRendersWithoutAdServer(true);
        register(bids -> findByPrice(bids, 3.00));

        BidResponse response = loadExpectingSuccess(response(serverWinner(), bidWithoutTargeting()));

        assertEquals(3.00, response.getWinningBid().getPrice(), 0.001);
        // The server winner's keys are not used
        assertTrue(response.getTargeting().isEmpty());
    }

    @Test
    public void renderingAdUnitsWithoutAnAdServer_areMarked() {
        Context context = Robolectric.buildActivity(Activity.class).create().get();

        BannerView standaloneBanner = new BannerView(context, CONFIG_ID, new AdSize(320, 50));
        BannerEventHandler gamBannerHandler = mock(BannerEventHandler.class);
        when(gamBannerHandler.getAdSizeArray()).thenReturn(new AdSize[]{new AdSize(320, 50)});
        BannerView gamBanner = new BannerView(context, CONFIG_ID, gamBannerHandler);
        InterstitialAdUnit standaloneInterstitial = new InterstitialAdUnit(context, CONFIG_ID);
        InterstitialAdUnit gamInterstitial = new InterstitialAdUnit(context, CONFIG_ID, mock(InterstitialEventHandler.class));
        RewardedAdUnit standaloneRewarded = new RewardedAdUnit(context, CONFIG_ID);
        RewardedAdUnit gamRewarded = new RewardedAdUnit(context, CONFIG_ID, mock(RewardedEventHandler.class));

        assertTrue(configOf(standaloneBanner, "adUnitConfig").rendersWithoutAdServer());
        assertFalse(configOf(gamBanner, "adUnitConfig").rendersWithoutAdServer());
        assertTrue(configOf(standaloneInterstitial, "config").rendersWithoutAdServer());
        assertFalse(configOf(gamInterstitial, "config").rendersWithoutAdServer());
        assertTrue(configOf(standaloneRewarded, "config").rendersWithoutAdServer());
        assertFalse(configOf(gamRewarded, "config").rendersWithoutAdServer());

        standaloneBanner.destroy();
        gamBanner.destroy();
    }

    // endregion

    // region Selected bid targeting

    @Test
    public void serverWinnerKeepsItsKeys() {
        Map<String, String> targeting = map(
                "hb_pb", "0.75", "hb_bidder", "openx", "hb_size", "300x250",
                "hb_pb_openx", "0.75", "hb_bidder_openx", "openx", "hb_size_openx", "300x250"
        );

        assertEquals(targeting, SelectedBidTargeting.make(targeting).getTargeting());
    }

    @Test
    public void genericKeysAreFilledInFromBidderSpecificKeys() {
        Map<String, String> targeting = map(
                "hb_pb_rubicon", "1.20",
                "hb_bidder_rubicon", "rubicon",
                "hb_size_rubicon", "300x250",
                "hb_format_rubicon", "video",
                "hb_deal_rubicon", "deal-1",
                "hb_cache_id_rubicon", "cache-1",
                "hb_uuid_rubicon", "uuid-1",
                "hb_cache_host_rubico", "prebid-cache.example.com",
                "hb_cache_path_rubico", "/cache",
                "hb_env_rubicon", "mobile-app"
        );

        HashMap<String, String> expected = new HashMap<>(targeting);
        expected.putAll(map(
                "hb_pb", "1.20",
                "hb_bidder", "rubicon",
                "hb_size", "300x250",
                "hb_format", "video",
                "hb_deal", "deal-1",
                "hb_cache_id", "cache-1",
                "hb_uuid", "uuid-1",
                "hb_cache_host", "prebid-cache.example.com",
                "hb_cache_path", "/cache",
                "hb_env", "mobile-app"
        ));

        assertEquals(expected, SelectedBidTargeting.make(targeting).getTargeting());
    }

    @Test
    public void bidderAliasResolvesToItsOwnKeys() {
        HashMap<String, String> result = SelectedBidTargeting.make(map(
                "hb_pb_appnexus2", "1.50",
                "hb_bidder_appnexus2", "appnexus2"
        )).getTargeting();

        assertEquals("appnexus2", result.get("hb_bidder"));
        assertEquals("1.50", result.get("hb_pb"));
    }

    @Test
    public void truncatedBidderSpecificKeysAreResolved() {
        // Prebid Server truncates keys to 20 characters by default
        HashMap<String, String> result = SelectedBidTargeting.make(map(
                "hb_bidder_averylongb", "averylongbidder",
                "hb_pb_averylongbidde", "0.50",
                "hb_cache_id_averylon", "cache-1"
        )).getTargeting();

        assertEquals("averylongbidder", result.get("hb_bidder"));
        assertEquals("0.50", result.get("hb_pb"));
        assertEquals("cache-1", result.get("hb_cache_id"));
    }

    @Test
    public void keysOfOtherFamiliesAreNotMistakenForAKey() {
        HashMap<String, String> result = SelectedBidTargeting.make(map(
                "hb_bidder_openx", "openx",
                "hb_pb_cat_dur_openx", "1.00_food_30s"
        )).getTargeting();

        assertEquals("openx", result.get("hb_bidder"));
        assertNull(result.get("hb_pb"));
    }

    @Test
    public void bidWithoutBidderKeysIsLeftAsIs() {
        Map<String, String> targeting = map("custom_key", "value");

        assertEquals(targeting, SelectedBidTargeting.make(targeting).getTargeting());
    }

    @Test
    public void severalBiddersAreAmbiguous() {
        SelectedBidTargeting result = SelectedBidTargeting.make(map(
                "hb_bidder_openx", "openx",
                "hb_bidder_rubicon", "rubicon"
        ));

        assertNull(result.getTargeting());
        assertEquals("the bid names more than one bidder: openx, rubicon", result.getFailure());
    }

    @Test
    public void severalTruncatedFormsOfAKeyAreAmbiguous() {
        SelectedBidTargeting result = SelectedBidTargeting.make(map(
                "hb_bidder_appnexus", "appnexus",
                "hb_pb_appn", "1.00",
                "hb_pb_appnex", "2.00"
        ));

        assertNull(result.getTargeting());
        assertEquals("more than one key of the bid can be hb_pb: hb_pb_appn, hb_pb_appnex", result.getFailure());
    }

    // endregion

    // region Helpers

    private static final class StubSelector implements PrebidBidSelecting {

        private final PrebidPluginInfo pluginInfo;
        private final Function<List<Bid>, Bid> selection;

        final List<List<Bid>> calls = new ArrayList<>();
        BidSelectionContext lastContext;
        boolean calledOnMainThread;
        int registerCount;
        int unregisterCount;

        StubSelector(String name, Function<List<Bid>, Bid> selection) {
            this.pluginInfo = new PrebidPluginInfo(name, "1.0");
            this.selection = selection;
        }

        @NonNull
        @Override
        public PrebidPluginInfo getPluginInfo() {
            return pluginInfo;
        }

        @Nullable
        @Override
        public Bid selectBid(@NonNull List<Bid> bids, @NonNull BidSelectionContext context) {
            calls.add(new ArrayList<>(bids));
            lastContext = context;
            calledOnMainThread = Looper.myLooper() == Looper.getMainLooper();
            return selection.apply(bids);
        }

        @Override
        public void onRegister() {
            registerCount++;
        }

        @Override
        public void onUnregister() {
            unregisterCount++;
        }
    }

    private StubSelector register(Function<List<Bid>, Bid> selection) {
        StubSelector selector = new StubSelector("test-selector", selection);
        PrebidMobile.registerPlugin(selector, PluginCapability.BID_SELECTOR);
        return selector;
    }

    private static Bid highestPrice(List<Bid> bids) {
        Bid best = null;
        for (Bid bid : bids) {
            if (best == null || bid.getPrice() > best.getPrice()) {
                best = bid;
            }
        }
        return best;
    }

    @Nullable
    private static Bid findByPrice(List<Bid> bids, double price) {
        for (Bid bid : bids) {
            if (Math.abs(bid.getPrice() - price) < 0.001) {
                return bid;
            }
        }
        return null;
    }

    private static AdUnitConfiguration configOf(Object adUnit, String field) {
        return WhiteBox.getInternalState(adUnit, field);
    }

    private BidResponse loadExpectingSuccess(JSONObject response) {
        onResponse(response);

        ArgumentCaptor<BidResponse> captor = ArgumentCaptor.forClass(BidResponse.class);
        verify(listener).onFetchCompleted(captor.capture());
        verify(listener, never()).onError(any());
        return captor.getValue();
    }

    private AdException loadExpectingError(JSONObject response) {
        onResponse(response);

        ArgumentCaptor<AdException> captor = ArgumentCaptor.forClass(AdException.class);
        verify(listener).onError(captor.capture());
        verify(listener, never()).onFetchCompleted(any());
        return captor.getValue();
    }

    private void onResponse(JSONObject response) {
        BidLoader bidLoader = new BidLoader(adUnitConfiguration, listener);
        WhiteBox.setInternalState(bidLoader, "bidRequester", mock(BidRequester.class));
        WhiteBox.setInternalState(bidLoader, "refreshTimerTask", mock(RefreshTimerTask.class));

        ResponseHandler responseHandler = Reflection.getFieldOf(bidLoader, "responseHandler");
        BaseNetworkTask.GetUrlResult result = new BaseNetworkTask.GetUrlResult();
        result.responseString = response.toString();
        responseHandler.onResponse(result);
    }

    /** The winner Prebid Server picked: generic and bidder-specific keys, a deal, no Prebid Cache entry. */
    private static JSONObject serverWinner() throws JSONException {
        return bid("openx-bid", 0.75, map(
                "hb_pb", "0.70", "hb_bidder", "openx", "hb_size", "300x250", "hb_deal", "openx-deal",
                "hb_pb_openx", "0.70", "hb_bidder_openx", "openx", "hb_size_openx", "300x250", "hb_deal_openx", "openx-deal"
        ), false);
    }

    /** A higher bid that Prebid Server didn't pick: bidder-specific keys only, with a Prebid Cache entry. */
    private static JSONObject runnerUp() throws JSONException {
        return bid("appnexus-bid", 2.00, map(
                "hb_pb_appnexus", "2.00",
                "hb_bidder_appnexus", "appnexus",
                "hb_size_appnexus", "320x50",
                "hb_cache_id_appnexus", "appnexus-cache-id",
                "hb_cache_host_appnex", "prebid-cache.example.com",
                "hb_cache_path_appnex", "/cache"
        ), true);
    }

    /** A bid without any targeting keys. */
    private static JSONObject bidWithoutTargeting() throws JSONException {
        return bid("no-targeting-bid", 3.00, null, false);
    }

    private static JSONObject bid(String id, double price, @Nullable Map<String, String> targeting, boolean cached) throws JSONException {
        JSONObject prebid = new JSONObject();
        prebid.put("type", "banner");
        if (targeting != null) {
            prebid.put("targeting", new JSONObject(targeting));
        }
        if (cached) {
            prebid.put("cache", new JSONObject()
                    .put("bids", new JSONObject()
                            .put("url", "https://prebid-cache.example.com/cache?uuid=" + id)
                            .put("cacheId", id + "-cache-id")));
        }

        return new JSONObject()
                .put("id", id)
                .put("impid", "imp-id")
                .put("price", price)
                .put("adm", "<html></html>")
                .put("crid", "crid")
                .put("w", 320)
                .put("h", 50)
                .put("ext", new JSONObject().put("prebid", prebid));
    }

    private static JSONObject response(JSONObject... bids) throws JSONException {
        return new JSONObject()
                .put("id", "response-id")
                .put("cur", "USD")
                .put("seatbid", new JSONArray().put(new JSONObject()
                        .put("bid", new JSONArray(Arrays.asList(bids)))
                        .put("seat", "prebid")));
    }

    private static HashMap<String, String> map(String... keysAndValues) {
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    // endregion
}
