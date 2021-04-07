package com.openx.apollo.bidding.data.bid;

import org.json.JSONObject;

public class Bids {
    private String mUrl;
    private String mCacheId;

    protected Bids() {
    }

    public String getUrl() {
        return mUrl;
    }

    public String getCacheId() {
        return mCacheId;
    }

    public static Bids fromJSONObject(JSONObject jsonObject) {
        Bids bids = new Bids();
        if (jsonObject == null) {
            return bids;
        }
        bids.mUrl = jsonObject.optString("url");
        bids.mCacheId = jsonObject.optString("cacheId");
        return bids;
    }
}
