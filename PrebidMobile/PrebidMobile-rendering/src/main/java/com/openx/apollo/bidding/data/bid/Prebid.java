package com.openx.apollo.bidding.data.bid;

import com.openx.apollo.bidding.data.AdSize;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.networking.targeting.Targeting;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import static com.openx.apollo.utils.helpers.Utils.addValue;

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
            addValue(prebidObject, "is_rewarded_inventory", 1);
        }

        return prebidObject;
    }

    public static JSONObject getJsonObjectForApp(String sdkName, String sdkVersion){
        JSONObject prebid = new JSONObject();
        addValue(prebid, "source", sdkName);
        addValue(prebid, "version", sdkVersion);
        return prebid;
    }

    public static JSONObject getJsonObjectForBidRequest(String accountId, boolean isVideo) {
        JSONObject prebid = getPrebidObject(accountId);

        JSONObject cache = new JSONObject();
        addValue(cache, "bids", new JSONObject());
        if (isVideo) {
            addValue(cache, "vastxml", new JSONObject());
        }

        addValue(prebid, "cache", cache);
        addValue(prebid, "targeting", new JSONObject());

        if (!Targeting.getAccessControlList().isEmpty()) {
            JSONObject data = new JSONObject();
            addValue(data, "bidders", new JSONArray(Targeting.getAccessControlList()));
            addValue(prebid, "data", data);
        }

        return prebid;
    }

    public static JSONObject getJsonObjectForDeviceMinSizePerc(AdSize minSizePercentage) {
        JSONObject prebid = new JSONObject();
        JSONObject interstitial = new JSONObject();
        addValue(interstitial, "minwidthperc", minSizePercentage.width);
        addValue(interstitial, "minheightperc", minSizePercentage.height);

        addValue(prebid, "interstitial", interstitial);

        return prebid;
    }

    private static JSONObject getPrebidObject(String configId) {
        JSONObject prebid = new JSONObject();
        StoredRequest storedRequest = new StoredRequest(configId);
        addValue(prebid, "storedrequest", storedRequest.toJSONObject());
        return prebid;
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
