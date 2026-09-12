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

import android.content.Context;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.MobileSdkPassThrough;
import org.prebid.mobile.rendering.utils.helpers.Dips;
import org.prebid.mobile.rendering.utils.helpers.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BidResponse {

    private final static String TAG = BidResponse.class.getSimpleName();

    public static final String KEY_CACHE_ID = "hb_cache_id_local";
    public static final String KEY_RENDERER_NAME = "rendererName";
    public static final String KEY_RENDERER_VERSION = "rendererVersion";
    public static final String TYPE_VIDEO = "video";

    // Standard winner-marker keys (see hasWinningKeywords). These specifically identify "the"
    // winner, so they must never be attributed to a bid that isn't the (possibly
    // selector-chosen) winner -- otherwise getTargeting() could still point at a different bid
    // than getWinningBid() does.
    private static final Set<String> WINNING_BID_MARKER_KEYS = new HashSet<>(Arrays.asList("hb_pb", "hb_bidder", "hb_cache_id"));

    // ID of the bid request to which this is a response
    private String id;

    // Bid currency using ISO-4217 alpha codes.
    private String cur;

    //Bidder generated response ID to assist with logging/tracking.
    private String bidId;

    //Optional feature to allow a bidder to set data in the exchange’s cookie
    private String customData;

    // Reason for not bidding
    private int nbr;

    // Array of seatbid objects; 1+ required if a bid is to be made.
    private List<Seatbid> seatbids;
    private Ext ext;

    private boolean hasParseError = false;
    private boolean usesCache;
    private String parseError;
    private String winningBidJson;
    private AdUnitConfiguration adUnitConfiguration;
    @Nullable
    private JSONObject responseJson;

    private long creationTime;

    private MobileSdkPassThrough mobileSdkPassThrough;

    // Snapshotted from adUnitConfiguration at construction time -- adUnitConfiguration is a
    // mutable reference owned by the ad unit and can outlive this response (e.g. via
    // BidResponseCache or a slow-rendering flow), so reading it live on every call would let a
    // later selector change silently change the winner of an already-returned BidResponse.
    @Nullable
    private final PrebidBidSelecting bidSelector;

    // Resolved exactly once, right after seatbids are parsed (see resolveWinningBid()). A
    // publisher-supplied selector may be non-deterministic or stateful, so it must be evaluated
    // a single time per response -- otherwise getWinningBid(), hasBidSelectorRejectedAllBids(),
    // getTargeting(), and getWinningBidJson() could each observe a different "winner" for what
    // is supposed to be one frozen auction result.
    @Nullable
    private Bid resolvedWinningBid;

    public BidResponse(
        String json,
        AdUnitConfiguration adUnitConfiguration
    ) {
        seatbids = new ArrayList<>();
        usesCache = adUnitConfiguration.isOriginalAdUnit() || PrebidMobile.isUseCacheForReportingWithRenderingApi();
        this.adUnitConfiguration = adUnitConfiguration;
        this.bidSelector = adUnitConfiguration.getBidSelector();

        parseJson(json);
    }

    public String getId() {
        return id;
    }

    public List<Seatbid> getSeatbids() {
        return seatbids;
    }

    public String getCur() {
        return cur;
    }

    public Ext getExt() {
        if (ext == null) {
            ext = new Ext();
        }
        return ext;
    }

    public boolean hasParseError() {
        return hasParseError;
    }

    public String getParseError() {
        return parseError;
    }

    public String getBidId() {
        return bidId;
    }

    public String getCustomData() {
        return customData;
    }

    public int getNbr() {
        return nbr;
    }

    @Nullable
    public String getWinningBidJson() {
        return winningBidJson;
    }

    @NonNull
    public JSONObject getResponseJson() {
        return responseJson == null ? new JSONObject() : responseJson;
    }

    private void parseJson(String json) {
        winningBidJson = json;

        try {
            responseJson = new JSONObject(json);
            id = responseJson.optString("id");
            cur = responseJson.optString("cur");
            bidId = responseJson.optString("bidid");
            customData = responseJson.optString("customdata");
            nbr = responseJson.optInt("nbr", -1);

            MobileSdkPassThrough rootMobilePassThrough = null;
            if (responseJson.has("ext")) {
                ext = new Ext();
                JSONObject extJsonObject = responseJson.optJSONObject("ext");
                ext.put(extJsonObject);
                if (extJsonObject != null) {
                    rootMobilePassThrough = MobileSdkPassThrough.create(extJsonObject);
                }
            }

            JSONArray jsonSeatbids = responseJson.optJSONArray("seatbid");
            if (jsonSeatbids != null) {
                for (int i = 0; i < jsonSeatbids.length(); i++) {
                    Seatbid seatbid = Seatbid.fromJSONObject(jsonSeatbids.optJSONObject(i));
                    seatbids.add(seatbid);
                }
            }

            resolveWinningBid();

            MobileSdkPassThrough bidMobilePassThrough = null;
            Bid winningBid = resolvedWinningBid;
            if (winningBid != null) {
                bidMobilePassThrough = winningBid.getMobileSdkPassThrough();
            }

            mobileSdkPassThrough = MobileSdkPassThrough.combine(bidMobilePassThrough, rootMobilePassThrough);
            creationTime = System.currentTimeMillis();
        }
        catch (JSONException e) {
            hasParseError = true;
            parseError = "Failed to parse JSON string: " + e.getMessage();
            LogUtil.error(TAG, parseError);
        }
    }

    public long getCreationTime() {
        return creationTime;
    }

    @Nullable
    public Bid getWinningBid() {
        return resolvedWinningBid;
    }

    /**
     * True if a bid selector is active for this response and it decided that no bid should win.
     * Callers must treat this as final: no targeting keywords should be attached and the fetch
     * should be reported as having no bids, not as a success.
     */
    public boolean hasBidSelectorRejectedAllBids() {
        return bidSelector != null && resolvedWinningBid == null;
    }

    /**
     * Resolves and caches the winning bid exactly once, right after seatbids are parsed. See
     * the {@link #resolvedWinningBid} field doc for why this must not be re-evaluated per call.
     */
    private void resolveWinningBid() {
        if (bidSelector != null) {
            List<Bid> allBids = getAllBids();
            Bid selectedBid = bidSelector.selectBid(allBids);
            if (selectedBid != null && !allBids.contains(selectedBid)) {
                LogUtil.warning(TAG, "PrebidBidSelecting.selectBid() returned a bid that is not part of the provided bids. Ignoring it.");
                selectedBid = null;
            }
            resolvedWinningBid = selectedBid;
            if (resolvedWinningBid != null) {
                winningBidJson = resolvedWinningBid.getJsonString();
            }
            return;
        }

        for (Seatbid seatbid : seatbids) {
            for (Bid bid : seatbid.getBids()) {
                if (hasWinningKeywords(bid.getPrebid())) {
                    winningBidJson = bid.getJsonString();
                    resolvedWinningBid = bid;
                    return;
                }
            }
        }

        resolvedWinningBid = null;
    }

    @NonNull
    private List<Bid> getAllBids() {
        List<Bid> allBids = new ArrayList<>();
        if (seatbids == null) {
            return allBids;
        }
        for (Seatbid seatbid : seatbids) {
            allBids.addAll(seatbid.getBids());
        }
        return allBids;
    }

    @NonNull
    public HashMap<String, String> getTargeting() {
        HashMap<String, String> keywords = new HashMap<>();

        if (bidSelector != null) {
            Bid winningBid = getWinningBid();
            if (winningBid == null) {
                // The selector explicitly chose no winner -- no bid's targeting should be
                // attached at all, not even non-marker keys from the other bids.
                return keywords;
            }

            for (Seatbid seatbid : seatbids) {
                for (Bid bid : seatbid.getBids()) {
                    if (bid.getPrebid() == null || bid == winningBid) {
                        continue;
                    }
                    // Strip the standard winner markers from every bid that isn't the
                    // selector-chosen winner, or the ad server could still be targeted with a
                    // different bid than the one getWinningBid() reports.
                    HashMap<String, String> filtered = new HashMap<>(bid.getPrebid().getTargeting());
                    filtered.keySet().removeAll(WINNING_BID_MARKER_KEYS);
                    keywords.putAll(filtered);
                }
            }

            // The winner's own targeting always takes precedence over other bids' contributions.
            keywords.putAll(winningBid.getPrebid().getTargeting());
            return keywords;
        }

        for (Seatbid seatbid : seatbids) {
            for (Bid bid : seatbid.getBids()) {
                if (bid.getPrebid() != null) {
                    keywords.putAll(bid.getPrebid().getTargeting());
                }
            }
        }

        return keywords;
    }

    @NonNull
    public HashMap<String, String> getTargetingWithCacheId() {
        // required for future BidResponseCache access
        final HashMap<String, String> targeting = getTargeting();
        targeting.put(KEY_CACHE_ID, id);
        return targeting;
    }

    public boolean isVideo() {
        Bid bid = getWinningBid();
        if (bid == null) return false;

        String type = bid.getType();
        if (type != null && !type.isEmpty()) {
            return type.equals(TYPE_VIDEO);
        }

        return Utils.isVast(bid.getAdm());
    }

    public String getPreferredPluginRendererName() {
        Bid bid = getWinningBid();
        if (bid != null) {
            return bid.getPrebid().getMeta().get(KEY_RENDERER_NAME);
        }
        return null;
    }

    public String getPreferredPluginRendererVersion() {
        Bid bid = getWinningBid();
        if (bid != null) {
            return bid.getPrebid().getMeta().get(KEY_RENDERER_VERSION);
        }
        return null;
    }

    public AdUnitConfiguration getAdUnitConfiguration() {
        return adUnitConfiguration;
    }

    private boolean hasWinningKeywords(Prebid prebid) {
        if (prebid == null || prebid.getTargeting().isEmpty()) {
            return false;
        }
        HashMap<String, String> targeting = prebid.getTargeting();
        boolean result = targeting.containsKey("hb_pb") && targeting.containsKey("hb_bidder");
        if (usesCache) {
            result = result && targeting.containsKey("hb_cache_id");
        }
        return result;
    }

    @NonNull
    public Pair<Integer, Integer> getWinningBidWidthHeightPairDips(Context context) {
        final Bid winningBid = getWinningBid();
        if (winningBid == null) {
            return new Pair<>(0, 0);
        }

        final int width = Dips.dipsToIntPixels(winningBid.getWidth(), context);
        final int height = Dips.dipsToIntPixels(winningBid.getHeight(), context);
        return new Pair<>(width, height);
    }

    @Nullable
    public MobileSdkPassThrough getMobileSdkPassThrough() {
        return mobileSdkPassThrough;
    }

    public void setMobileSdkPassThrough(@Nullable MobileSdkPassThrough mobileSdkPassThrough) {
        this.mobileSdkPassThrough = mobileSdkPassThrough;
    }

    @Nullable
    public String getImpressionEventUrl() {
        Bid winningBid = getWinningBid();
        if (winningBid != null) {
            Prebid prebid = winningBid.getPrebid();
            return prebid.getImpEventUrl();
        }
        return null;
    }

    @Nullable
    public Integer getExpirationTimeSeconds() {
        Bid winningBid = getWinningBid();
        if (winningBid == null) return null;

        int expirationTime = winningBid.getExp();
        if (expirationTime <= 0) {
            return null;
        }

        return expirationTime;
    }

}
