package com.openx.apollo.models.openrtb.bidRequests;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by matthew.rolufs on 6/23/15.
 */
public class BaseBid implements Serializable
{
    protected void toJSON(JSONObject jsonObject, String key, Object value) throws JSONException {
        jsonObject.putOpt(key, value);
    }
}
