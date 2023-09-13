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

import static org.prebid.mobile.rendering.utils.helpers.Utils.addValue;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.PluginRendererList;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.mapper.PluginRendererListMapper;
import org.prebid.mobile.rendering.utils.helpers.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Prebid {

    private Cache cache;
    private HashMap<String, String> targeting = new HashMap<>();
    private HashMap<String, String> meta = new HashMap<>();
    private String type;
    private String winEventUrl;
    private String impEventUrl;

    protected Prebid() {
    }

    public Cache getCache() {
        if (cache == null) {
            cache = new Cache();
        }
        return cache;
    }

    public HashMap<String, String> getTargeting() {
        return targeting;
    }

    public HashMap<String, String> getMeta() {
        return meta;
    }

    public String getType() {
        return type;
    }

    public static Prebid fromJSONObject(JSONObject jsonObject) {
        Prebid prebid = new Prebid();
        if (jsonObject == null) {
            return prebid;
        }
        prebid.cache = Cache.fromJSONObject(jsonObject.optJSONObject("cache"));
        prebid.type = jsonObject.optString("type");
        parseEvents(prebid, jsonObject.optJSONObject("events"));
        toHashMap(prebid.targeting, jsonObject.optJSONObject("targeting"));
        toHashMap(prebid.meta, jsonObject.optJSONObject("meta"));

        return prebid;
    }

    public String getWinEventUrl() {
        return winEventUrl;
    }

    public String getImpEventUrl() {
        return impEventUrl;
    }

    public static JSONObject getJsonObjectForImp(AdUnitConfiguration adUnitConfiguration) {
        final JSONObject prebidObject = getPrebidObject(adUnitConfiguration.getConfigId());

        if (adUnitConfiguration.isRewarded()) {
            Utils.addValue(prebidObject, "is_rewarded_inventory", 1);
        }

        return prebidObject;
    }

    public static JSONObject getJsonObjectForApp(
        String sdkName,
        String sdkVersion
    ) {
        JSONObject prebid = new JSONObject();
        Utils.addValue(prebid, "source", sdkName);
        Utils.addValue(prebid, "version", sdkVersion);
        return prebid;
    }

    public static JSONObject getJsonObjectForBidRequest(
            String accountId,
            boolean isVideo,
            AdUnitConfiguration config
    ) {
        JSONObject prebid = getPrebidObject(accountId);

        JSONObject cache = new JSONObject();
        Utils.addValue(cache, "bids", new JSONObject());
        if (isVideo) {
            Utils.addValue(cache, "vastxml", new JSONObject());
        }

        if (PrebidMobile.isUseCacheForReportingWithRenderingApi() || config.isOriginalAdUnit()) {
            Utils.addValue(prebid, "cache", cache);
        }

        JSONObject targeting = new JSONObject();

        if (config.isOriginalAdUnit() && config.getAdFormats().size() > 1) {
            Utils.addValue(targeting, "includeformat", "true");
        }

        if(PrebidMobile.getIncludeWinnersFlag()){
            Utils.addValue(targeting, "includewinners", "true");
        }

        if(PrebidMobile.getIncludeBidderKeysFlag()){
            Utils.addValue(targeting, "includebidderkeys", "true");
        }

        Utils.addValue(prebid, "targeting", targeting);

        if (!TargetingParams.getAccessControlList().isEmpty()) {
            JSONObject data = new JSONObject();
            Utils.addValue(data, "bidders", new JSONArray(TargetingParams.getAccessControlList()));
            Utils.addValue(prebid, "data", data);
        }

        setPluginRendererList(prebid, config);

        return prebid;
    }

    public static JSONObject getJsonObjectForDeviceMinSizePerc(AdSize minSizePercentage) {
        JSONObject prebid = new JSONObject();
        JSONObject interstitial = new JSONObject();
        Utils.addValue(interstitial, "minwidthperc", minSizePercentage.getWidth());
        Utils.addValue(interstitial, "minheightperc", minSizePercentage.getHeight());

        Utils.addValue(prebid, "interstitial", interstitial);

        return prebid;
    }

    private static void setPluginRendererList(JSONObject prebid, AdUnitConfiguration adUnitConfiguration) {
        List<PrebidMobilePluginRenderer> plugins = PrebidMobilePluginRegister.getInstance().getRTBListOfRenderersFor(adUnitConfiguration);
        PluginRendererListMapper mapper = new PluginRendererListMapper();
        PluginRendererList rendererList = new PluginRendererList();
        rendererList.setList(mapper.map(plugins));

        boolean isDefaultPluginRenderer = rendererList.get().size() == 1
                && rendererList.get().get(0).getName().equals(PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME);

        if (!adUnitConfiguration.isOriginalAdUnit() && !isDefaultPluginRenderer) {
            try {
                Utils.addValue(prebid, "sdk", rendererList.getJsonObject());
            } catch (JSONException e) {
                LogUtil.error("setPluginRendererList", e.getMessage());
            }
        }
    }

    private static void parseEvents(
        @NonNull Prebid prebid,
        @Nullable JSONObject eventsJson
    ) {
        if (eventsJson == null) return;

        try {
            prebid.winEventUrl = eventsJson.getString("win");
            prebid.impEventUrl = eventsJson.getString("imp");
        } catch (JSONException ignored) {}
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
        final String storedAuctionResponse = PrebidMobile.getStoredAuctionResponse();
        if (!TextUtils.isEmpty(storedAuctionResponse)) {
            JSONObject storedAuctionResponseJson = new JSONObject();
            Utils.addValue(storedAuctionResponseJson, "id", storedAuctionResponse);
            Utils.addValue(prebid, "storedauctionresponse", storedAuctionResponseJson);
        }
    }

    private static void addStoredBidResponse(JSONObject prebid) {
        final Map<String, String> storedBidResponseMap = PrebidMobile.getStoredBidResponses();
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
