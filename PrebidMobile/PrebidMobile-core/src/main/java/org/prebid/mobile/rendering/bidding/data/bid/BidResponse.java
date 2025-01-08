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
import java.util.HashMap;
import java.util.List;

public class BidResponse {
    private final static String TAG = BidResponse.class.getSimpleName();
    public static final String KEY_CACHE_ID = "hb_cache_id_local";
    public static final String KEY_RENDERER_NAME = "rendererName";
    public static final String KEY_RENDERER_VERSION = "rendererVersion";

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

    public BidResponse(
        String json,
        AdUnitConfiguration adUnitConfiguration
    ) {
        seatbids = new ArrayList<>();
        usesCache = adUnitConfiguration.isOriginalAdUnit() || PrebidMobile.isUseCacheForReportingWithRenderingApi();
        this.adUnitConfiguration = adUnitConfiguration;

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

            MobileSdkPassThrough bidMobilePassThrough = null;
            Bid winningBid = getWinningBid();
            if (winningBid != null) {
                bidMobilePassThrough = winningBid.getMobileSdkPassThrough();
            }

            mobileSdkPassThrough = MobileSdkPassThrough.combine(bidMobilePassThrough, rootMobilePassThrough);
            creationTime = System.currentTimeMillis();
        }
        catch (JSONException e) {
            hasParseError = true;
            parseError = "Failed to parse JSON String: " + e.getMessage();
            LogUtil.error(TAG, parseError);
        }
    }

    public long getCreationTime() {
        return creationTime;
    }

    @Nullable
    public Bid getWinningBid() {
        if (seatbids == null) {
            return null;
        }

        for (Seatbid seatbid : seatbids) {
            for (Bid bid : seatbid.getBids()) {
                if (hasWinningKeywords(bid.getPrebid())) {
                    winningBidJson = bid.getJsonString();
                    return bid;
                }
            }
        }

        return null;
    }

    @NonNull
    public HashMap<String, String> getTargeting() {
        HashMap<String, String> keywords = new HashMap<>();
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
        if (bid != null) {
            return Utils.isVast(bid.getAdm());
        }
        return false;
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
