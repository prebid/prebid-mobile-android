package com.openx.apollo.bidding.data.bid;

import org.json.JSONObject;

public class Cache {
    private String mKey;
    private String mUrl;
    private Bids mBids;

    protected Cache() {
    }

    public String getKey() {
        return mKey;
    }

    public String getUrl() {
        return mUrl;
    }

    public Bids getBids() {
        if (mBids == null) {
            mBids = new Bids();
        }
        return mBids;
    }

    public static Cache fromJSONObject(JSONObject jsonObject) {
        Cache cache = new Cache();
        if (jsonObject == null) {
            return cache;
        }
        cache.mKey = jsonObject.optString("key");
        cache.mUrl = jsonObject.optString("url");
        cache.mBids = Bids.fromJSONObject(jsonObject.optJSONObject("bids"));

        return cache;
    }
}
