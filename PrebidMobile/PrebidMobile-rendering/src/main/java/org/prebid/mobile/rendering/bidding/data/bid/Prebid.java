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

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.networking.targeting.Targeting;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.rendering.utils.helpers.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.prebid.mobile.rendering.utils.helpers.Utils.addValue;

public class Prebid {

    private Cache mCache;
    private HashMap<String, String> mTargeting = new HashMap<>();
    private String mType;

    protected Prebid() {
    }

    public Cache getCache() {
        if (mCache == null) {
            mCache = new Cache();
        }
        return mCache;
    }

    public HashMap<String, String> getTargeting() {
        return mTargeting;
    }

    public String getType() {
        return mType;
    }

    public static Prebid fromJSONObject(JSONObject jsonObject) {
        Prebid prebid = new Prebid();
        if (jsonObject == null) {
            return prebid;
        }
        prebid.mCache = Cache.fromJSONObject(jsonObject.optJSONObject("cache"));
        prebid.mType = jsonObject.optString("type");
        toHashMap(prebid.mTargeting, jsonObject.optJSONObject("targeting"));
        return prebid;
    }

    public static JSONObject getJsonObjectForImp(AdConfiguration adUnitConfiguration) {
        final JSONObject prebidObject = getPrebidObject(adUnitConfiguration.getConfigId());

        if (adUnitConfiguration.isRewarded()) {
            Utils.addValue(prebidObject, "is_rewarded_inventory", 1);
        }

        return prebidObject;
    }

    public static JSONObject getJsonObjectForApp(String sdkName, String sdkVersion) {
        JSONObject prebid = new JSONObject();
        Utils.addValue(prebid, "source", sdkName);
        Utils.addValue(prebid, "version", sdkVersion);
        return prebid;
    }

    public static JSONObject getJsonObjectForBidRequest(String accountId, boolean isVideo) {
        JSONObject prebid = getPrebidObject(accountId);

        JSONObject cache = new JSONObject();
        Utils.addValue(cache, "bids", new JSONObject());
        if (isVideo) {
            Utils.addValue(cache, "vastxml", new JSONObject());
        }

        Utils.addValue(prebid, "cache", cache);
        Utils.addValue(prebid, "targeting", new JSONObject());

        if (!Targeting.getAccessControlList().isEmpty()) {
            JSONObject data = new JSONObject();
            Utils.addValue(data, "bidders", new JSONArray(Targeting.getAccessControlList()));
            Utils.addValue(prebid, "data", data);
        }

        return prebid;
    }

    public static JSONObject getJsonObjectForDeviceMinSizePerc(AdSize minSizePercentage) {
        JSONObject prebid = new JSONObject();
        JSONObject interstitial = new JSONObject();
        Utils.addValue(interstitial, "minwidthperc", minSizePercentage.width);
        Utils.addValue(interstitial, "minheightperc", minSizePercentage.height);

        Utils.addValue(prebid, "interstitial", interstitial);

        return prebid;
    }

    private static JSONObject getPrebidObject(String configId) {
        JSONObject prebid = new JSONObject();
        StoredRequest storedRequest = new StoredRequest(configId);
        addValue(prebid, "storedrequest", storedRequest.toJSONObject());

        addStoredAuctionResponse(prebid);
        addStoredBidResponse(prebid);

        return prebid;
    }

    private static void addStoredAuctionResponse(JSONObject prebid) {
        final String storedAuctionResponse = PrebidRenderingSettings.getStoredAuctionResponse();
        if (!TextUtils.isEmpty(storedAuctionResponse)) {
            JSONObject storedAuctionResponseJson = new JSONObject();
            Utils.addValue(storedAuctionResponseJson, "id", storedAuctionResponse);
            Utils.addValue(prebid, "storedauctionresponse", storedAuctionResponseJson);
        }
    }

    private static void addStoredBidResponse(JSONObject prebid) {
        final Map<String, String> storedBidResponseMap = PrebidRenderingSettings.getStoredBidResponseMap();
        if (!storedBidResponseMap.isEmpty()) {
            JSONArray bidResponseArray = new JSONArray();

            for (Map.Entry<String, String> entry : storedBidResponseMap.entrySet()) {
                final String bidder = entry.getKey();
                final String bidId = entry.getValue();
                if (!TextUtils.isEmpty(bidder) && !TextUtils.isEmpty(bidId)) {
                    JSONObject storedBid = new JSONObject();
                    Utils.addValue(storedBid, "bidder", bidder);
                    Utils.addValue(storedBid, "id", bidId);
                }
            }

            Utils.addValue(prebid, "storedbidresponse", bidResponseArray);
        }
    }

    private static void toHashMap(HashMap<String, String> hashMap, JSONObject jsonObject) {
        if (jsonObject == null || hashMap == null) {
            return;
        }
        Iterator<String> jsonIterator = jsonObject.keys();
        while (jsonIterator.hasNext()) {
            String key = jsonIterator.next();
            hashMap.put(key, jsonObject.optString(key));
        }
    }
}
