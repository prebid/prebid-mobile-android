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

package org.prebid.mobile.rendering.bidding.data.bid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.Position;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.core.BuildConfig;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.MobileSdkPassThrough;
import org.prebid.mobile.test.utils.ResourceUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class BidResponseTest {

    // MARK: - bidSelector

    private static class StubBidSelector implements PrebidBidSelecting {
        interface Selection {
            Bid select(List<Bid> bids);
        }

        private final Selection selection;

        StubBidSelector(Selection selection) {
            this.selection = selection;
        }

        @Override
        public Bid selectBid(List<Bid> bids) {
            return selection.select(bids);
        }
    }

    @Test
    public void noSelectorByDefault_defaultMarkerLogicPicksWinner() throws IOException {
        String responseString = twoBidsResponseJson(
            bidJson("marked", 0.75, "openx", null),
            null
        );
        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        BidResponse bidResponse = new BidResponse(responseString, adUnitConfiguration);

        assertNull(adUnitConfiguration.getBidSelector());
        assertNotNull(bidResponse.getWinningBid());
        assertEquals("openx", bidResponse.getWinningBid().getPrebid().getTargeting().get("hb_bidder"));
    }

    @Test
    public void selectorIsInvokedExactlyOncePerResponse() throws IOException {
        // A stateful/non-deterministic selector must still be evaluated exactly once per
        // response -- otherwise getWinningBid(), hasBidSelectorRejectedAllBids(), and
        // getTargeting() could each observe a different "winner" for what is supposed to be one
        // frozen auction result. This selector flips its answer on every invocation, so calling
        // it more than once would make these methods disagree.
        String responseString = twoBidsResponseJson(
            bidJson("marked", 0.75, "openx", null),
            bidJson("unmarked", 2.00, null, null)
        );
        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        int[] callCount = {0};
        adUnitConfiguration.setBidSelector(new StubBidSelector(bids -> {
            callCount[0]++;
            // Odd calls pick bid 0, even calls pick bid 1 (or null on a third call) -- any
            // re-invocation changes the answer.
            if (callCount[0] == 1) return bids.get(0);
            if (callCount[0] == 2) return bids.get(1);
            return null;
        }));

        BidResponse bidResponse = new BidResponse(responseString, adUnitConfiguration);

        Bid winningBid = bidResponse.getWinningBid();
        boolean rejected = bidResponse.hasBidSelectorRejectedAllBids();
        HashMap<String, String> targeting = bidResponse.getTargeting();
        String winningBidJson = bidResponse.getWinningBidJson();

        assertEquals(1, callCount[0]);
        assertNotNull(winningBid);
        assertFalse(rejected);
        // All accessors must agree with the single resolved winner.
        assertEquals(winningBid.getJsonString(), winningBidJson);
        assertEquals(winningBid.getPrice(), bidResponse.getWinningBid().getPrice(), 0.001);
    }

    private static StubBidSelector highestPriceSelector() {
        return new StubBidSelector(bids -> {
            Bid best = null;
            for (Bid bid : bids) {
                if (best == null || bid.getPrice() > best.getPrice()) {
                    best = bid;
                }
            }
            return best;
        });
    }

    @Test
    public void selectorOverridesDefaultWinner() throws IOException {
        String responseString = twoBidsResponseJson(
            bidJson("marked", 0.75, "openx", null),
            bidJson("unmarked", 2.00, null, null)
        );
        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();

        // Without a selector, the marker-driven bid wins.
        BidResponse defaultResponse = new BidResponse(responseString, adUnitConfiguration);
        assertEquals("openx", defaultResponse.getWinningBid().getPrebid().getTargeting().get("hb_bidder"));

        // The selector must be set on the config *before* the response is constructed --
        // BidResponse snapshots it at construction time (see selectorIsSnapshotAtConstruction_notReadLive).
        adUnitConfiguration.setBidSelector(highestPriceSelector());
        BidResponse bidResponse = new BidResponse(responseString, adUnitConfiguration);

        assertEquals(2.00, bidResponse.getWinningBid().getPrice(), 0.001);
    }

    @Test
    public void selectorReturningNull_resultsInNoWinningBid() throws IOException {
        String responseString = twoBidsResponseJson(bidJson("marked", 0.75, "openx", null), null);
        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        adUnitConfiguration.setBidSelector(new StubBidSelector(bids -> null));

        BidResponse bidResponse = new BidResponse(responseString, adUnitConfiguration);

        assertNull(bidResponse.getWinningBid());
        assertTrue(bidResponse.hasBidSelectorRejectedAllBids());
    }

    @Test
    public void selectorReturningForeignBid_isRejected() throws IOException {
        // A bid from an entirely separate response must never become the winner.
        BidResponse staleResponse = new BidResponse(
            twoBidsResponseJson(bidJson("stale", 5.00, "stale_bidder", null), null),
            new AdUnitConfiguration()
        );
        Bid staleBid = staleResponse.getSeatbids().get(0).getBids().get(0);

        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        adUnitConfiguration.setBidSelector(new StubBidSelector(bids -> staleBid));
        BidResponse bidResponse = new BidResponse(
            twoBidsResponseJson(bidJson("marked", 0.75, "openx", null), null),
            adUnitConfiguration
        );

        assertNull(bidResponse.getWinningBid());
    }

    @Test
    public void selectorPickingUnmarkedBid_doesNotLeakOldWinnerMarkers() throws IOException {
        String responseString = twoBidsResponseJson(
            bidJson("marked", 0.75, "openx", null),
            bidJson("unmarked", 2.00, null, null)
        );
        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        adUnitConfiguration.setBidSelector(highestPriceSelector());
        BidResponse bidResponse = new BidResponse(responseString, adUnitConfiguration);

        assertEquals(2.00, bidResponse.getWinningBid().getPrice(), 0.001);
        // The "marked" bid is no longer the winner -- its hb_bidder/hb_pb markers must not
        // survive into the merged targeting, or the ad server would be targeted with a
        // different bid than the one getWinningBid() actually reports.
        assertNull(bidResponse.getTargeting().get("hb_bidder"));
        assertNull(bidResponse.getTargeting().get("hb_pb"));
    }

    @Test
    public void selectorReturningNull_targetingIsEmpty() throws IOException {
        // Even non-marker/custom keys from other bids must not leak through once the selector
        // has decided that no bid should win -- "null is final" means the whole targeting map
        // is empty, not just stripped of hb_pb/hb_bidder/hb_cache_id.
        String responseString = twoBidsResponseJson(
            bidJson("marked", 0.75, "openx", null),
            bidJson("unmarked", 2.00, null, null)
        );
        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        adUnitConfiguration.setBidSelector(new StubBidSelector(bids -> null));
        BidResponse bidResponse = new BidResponse(responseString, adUnitConfiguration);

        assertTrue(bidResponse.getTargeting().isEmpty());
    }

    @Test
    public void selectorIsSnapshotAtConstruction_notReadLive() throws IOException {
        String responseString = twoBidsResponseJson(
            bidJson("marked", 0.75, "openx", null),
            bidJson("unmarked", 2.00, null, null)
        );
        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        adUnitConfiguration.setBidSelector(highestPriceSelector());

        BidResponse bidResponse = new BidResponse(responseString, adUnitConfiguration);
        assertEquals(2.00, bidResponse.getWinningBid().getPrice(), 0.001);

        // Mutating the (mutable, shared) AdUnitConfiguration *after* the response was
        // constructed must not change this already-returned response's winner -- a BidResponse
        // represents a frozen snapshot of one auction's outcome, e.g. for BidResponseCache or a
        // slow-rendering flow that queries it again later.
        adUnitConfiguration.setBidSelector(null);
        assertEquals(2.00, bidResponse.getWinningBid().getPrice(), 0.001);

        adUnitConfiguration.setBidSelector(new StubBidSelector(bids -> null));
        assertEquals(2.00, bidResponse.getWinningBid().getPrice(), 0.001);
    }

    @Test
    public void defaultLogic_stillReportsWinnerMarkers() throws IOException {
        // Characterization: when no selector is active, the actual winner's own markers must
        // still flow through untouched -- the leak fix only strips markers from bids that
        // are *not* the winner.
        String responseString = twoBidsResponseJson(bidJson("marked", 0.75, "openx", null), null);
        BidResponse bidResponse = new BidResponse(responseString, new AdUnitConfiguration());

        assertEquals("openx", bidResponse.getTargeting().get("hb_bidder"));
        assertEquals("0.75", bidResponse.getTargeting().get("hb_pb"));
    }

    private static String bidJson(String id, double price, String bidder, String dealId) {
        StringBuilder prebid = new StringBuilder("{\"type\":\"banner\"");
        if (bidder != null) {
            prebid.append(",\"targeting\":{\"hb_bidder\":\"").append(bidder).append("\",\"hb_pb\":\"").append(price).append("\"}");
        }
        prebid.append("}");

        StringBuilder bid = new StringBuilder("{");
        bid.append("\"id\":\"test-bid-id-").append(id).append("\",");
        bid.append("\"impid\":\"test-imp-id-").append(id).append("\",");
        bid.append("\"price\":").append(price).append(",");
        bid.append("\"adm\":\"<html></html>\",");
        bid.append("\"w\":300,\"h\":250,");
        if (dealId != null) {
            bid.append("\"dealid\":\"").append(dealId).append("\",");
        }
        bid.append("\"ext\":{\"prebid\":").append(prebid).append("}");
        bid.append("}");
        return bid.toString();
    }

    private static String twoBidsResponseJson(String bid1Json, String bid2Json) {
        String bids = bid2Json == null ? bid1Json : (bid1Json + "," + bid2Json);
        return "{\"id\":\"response-id\",\"seatbid\":[{\"bid\":[" + bids + "],\"seat\":\"openx\"}],\"cur\":\"USD\"}";
    }

    @After
    public void tearDown() {
        PrebidMobile.setUseCacheForReportingWithRenderingApi(false);
    }

    @Test
    public void whenInstantiatedWithValidJson_NoParseError() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("bidding_response_obj.json");
        BidResponse bidResponse = new BidResponse(responseString, new AdUnitConfiguration());

        assertFalse(bidResponse.hasParseError());
        assertNotNull(bidResponse.getExt());
        assertNotNull(bidResponse.getSeatbids());
        assertEquals(1, bidResponse.getSeatbids().size());
        assertEquals("id", bidResponse.getId());
        assertEquals("USD", bidResponse.getCur());
        assertEquals("bidid", bidResponse.getBidId());
        assertEquals("custom", bidResponse.getCustomData());
        assertEquals(1, bidResponse.getNbr());
        assertEquals(Integer.valueOf(300), bidResponse.getExpirationTimeSeconds());
        assertNull(bidResponse.getMobileSdkPassThrough());
    }

    @Test
    public void whenInstantiatedWithInvalidJson_ParseError() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("bidding_response_obj.json").replaceFirst(",", "");
        BidResponse bidResponse = new BidResponse(responseString, new AdUnitConfiguration());
        assertTrue(bidResponse.hasParseError());
    }

    @Test
    public void whenInstantiatedWithNoBids_NoBids() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("bidding_response_no_bids_obj.json");
        BidResponse bidResponse = new BidResponse(responseString, new AdUnitConfiguration());

        assertFalse(bidResponse.hasParseError());
        assertNull(bidResponse.getParseError());
        assertEquals("id", bidResponse.getId());
        assertNotNull(bidResponse.getExt());
        assertNull(bidResponse.getExpirationTimeSeconds());
    }

    @Test
    public void whenInstantiatedWithoutWinningKeywords_NoBids() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("bidding_response_no_winning_keywords_obj.json");
        BidResponse bidResponse = new BidResponse(responseString, new AdUnitConfiguration());

        assertFalse(bidResponse.hasParseError());
        assertNull(bidResponse.getParseError());
        assertEquals("id", bidResponse.getId());
        assertNotNull(bidResponse.getExt());
    }

    @Test
    public void testMobileSdkPassThrough_checkFieldsUnification_returnUnifiedFields() throws IOException {
        if (!BuildConfig.DEBUG) {
            String responseString = ResourceUtils.convertResourceToString("BidResponseTest/mobile_sdk_pass_through.json");

            BidResponse subject = new BidResponse(responseString, new AdUnitConfiguration());
            MobileSdkPassThrough mobileSdkPassThrough = subject.getMobileSdkPassThrough();

            assertNotNull(mobileSdkPassThrough);
            assertTrue(mobileSdkPassThrough.isMuted);
            assertEquals((Double) 0.1, mobileSdkPassThrough.closeButtonArea);
            assertEquals(Position.TOP_LEFT, mobileSdkPassThrough.closeButtonPosition);
            assertEquals((Double) 0.2, mobileSdkPassThrough.skipButtonArea);
            assertEquals(Position.TOP_RIGHT, mobileSdkPassThrough.skipButtonPosition);
            assertEquals((Integer) 15, mobileSdkPassThrough.skipDelay);

            /* This field presents in both MobileSdkPassThrough objects */
            assertEquals((Integer) 11, mobileSdkPassThrough.maxVideoDuration);
        }
    }

    @Test
    public void testWinningBidKeywords_withoutOneKeyword_noBids() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("BidResponseTest/keywords_not_all.json");

        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        BidResponse subject = new BidResponse(responseString, adUnitConfiguration);

        assertNull(subject.getWinningBid());
        assertFalse(subject.hasParseError());
        assertNull(subject.getParseError());
    }

    @Test
    public void testWinningBidKeywords_empty_noBids() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("BidResponseTest/keywords_empty.json");

        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        BidResponse subject = new BidResponse(responseString, adUnitConfiguration);

        assertFalse(subject.hasParseError());
        assertNull(subject.getParseError());
        assertNull(subject.getWinningBid());
    }

    @Test
    public void testWinningBidKeywords_allKeywords_noParseError() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("BidResponseTest/keywords_all_without_cache_id.json");

        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        BidResponse subject = new BidResponse(responseString, adUnitConfiguration);

        assertFalse(subject.hasParseError());
        assertNotNull(subject.getWinningBid());
    }

    @Test
    public void testWinningBidKeywords_originalAdUnit_withoutCacheId_parseError() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("BidResponseTest/keywords_all_without_cache_id.json");

        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        adUnitConfiguration.setIsOriginalAdUnit(true);
        BidResponse subject = new BidResponse(responseString, adUnitConfiguration);

        assertFalse(subject.hasParseError());
        assertNull(subject.getWinningBid());
    }

    @Test
    public void testWinningBidKeywords_originalAdUnit_withCacheId_noParseError() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("BidResponseTest/keywords_all_with_cache_id.json");

        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        adUnitConfiguration.setIsOriginalAdUnit(true);
        BidResponse subject = new BidResponse(responseString, adUnitConfiguration);

        assertFalse(subject.hasParseError());
        assertNotNull(subject.getWinningBid());
    }

    @Test
    public void testWinningBidKeywords_useCacheInRenderingApi_withoutCacheId_noParseError() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("BidResponseTest/keywords_all_without_cache_id.json");

        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        BidResponse subject = new BidResponse(responseString, adUnitConfiguration);

        assertFalse(subject.hasParseError());
        assertNotNull(subject.getWinningBid());
    }

    @Test
    public void testWinningBidKeywords_useCacheInRenderingApi_withCacheId_noParseError() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("BidResponseTest/keywords_all_with_cache_id.json");

        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        PrebidMobile.setUseCacheForReportingWithRenderingApi(true);
        BidResponse subject = new BidResponse(responseString, adUnitConfiguration);

        assertFalse(subject.hasParseError());
        assertNotNull(subject.getWinningBid());
        assertNull(subject.getExpirationTimeSeconds());
    }

    @Test
    public void testBidType_banner() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("BidResponseTest/bid_type_banner.json");

        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        BidResponse subject = new BidResponse(responseString, adUnitConfiguration);

        assertFalse(subject.isVideo());
    }

    @Test
    public void testBidType_video() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("BidResponseTest/bid_type_video.json");

        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        BidResponse subject = new BidResponse(responseString, adUnitConfiguration);

        assertTrue(subject.isVideo());
    }

    @Test
    public void testBidType_noType_htmlContent() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("BidResponseTest/bid_type_none_with_html_content.json");

        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        BidResponse subject = new BidResponse(responseString, adUnitConfiguration);

        assertFalse(subject.isVideo());
    }

    @Test
    public void testBidType_noType_videoContent() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("BidResponseTest/bid_type_none_with_vast_content.json");

        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        BidResponse subject = new BidResponse(responseString, adUnitConfiguration);

        assertTrue(subject.isVideo());
    }

}