package org.prebid.mobile.drprebid.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DemandTestResponse {
    private int statusCode;
    private String responseText;


    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public static DemandTestResponse fromRtbResponse(String response) throws JSONException {
        if (TextUtils.isEmpty(response)) {
            return null;
        } else {
            JSONObject responseJson = new JSONObject(response);
            DemandTestResponse testResponse = new DemandTestResponse();

            Map<String, String> bidderResponseStatuses = new HashMap<>();

            JSONObject ext = responseJson.getJSONObject("ext");
            JSONObject responseMillis = ext.getJSONObject("responsetimemillis");

            Iterator<String> responseIterator = responseMillis.keys();

            while (responseIterator.hasNext()) {
                String key = responseIterator.next();
                bidderResponseStatuses.put(key, "0");
            }


            return testResponse;
        }
    }
}
