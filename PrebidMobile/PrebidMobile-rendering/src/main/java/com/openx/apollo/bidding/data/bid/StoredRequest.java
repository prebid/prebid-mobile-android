package com.openx.apollo.bidding.data.bid;

import org.json.JSONException;
import org.json.JSONObject;

public class StoredRequest {
    private String mId;

    public StoredRequest(String id) {
        mId = id;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", mId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
