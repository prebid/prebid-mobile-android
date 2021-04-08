package org.prebid.mobile.rendering.bidding.data.bid;

import org.json.JSONArray;
import org.json.JSONObject;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.networking.targeting.Targeting;
import org.prebid.mobile.rendering.utils.helpers.Utils;

import java.util.HashMap;
import java.util.Iterator;

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

    public static JSONObject getJsonObjectForApp(String sdkName, String sdkVersion){
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
