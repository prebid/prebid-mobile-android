package com.openx.apollo.networking.urlBuilder;

import com.openx.apollo.networking.parameters.AdRequestInput;
import com.openx.apollo.utils.helpers.Utils;
import com.openx.apollo.utils.logger.OXLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Hashtable;

public class URLComponents {
    private static final String TAG = URLComponents.class.getSimpleName();
    private static final String MISC_OPENRTB = "openrtb";

    private final String mBaseUrl;
    public final AdRequestInput mAdRequestInput;

    public URLComponents(String baseUrl, AdRequestInput adRequestInput) {
        mBaseUrl = baseUrl;
        mAdRequestInput = adRequestInput;
    }

    public String getFullUrl() {
        String fullUrl = mBaseUrl;
        String queryArgString = getQueryArgString();
        if (Utils.isNotBlank(queryArgString)) {
            fullUrl += "?" + queryArgString;
        }

        return (fullUrl);
    }

    public String getQueryArgString() {
        Hashtable<String, String> tempQueryArgs = new Hashtable<>();

        // If BidRequest object available, put into query arg hashtable
        try {
            JSONObject bidRequestJson = mAdRequestInput.getBidRequest().getJsonObject();

            if (bidRequestJson.length() > 0) {
                tempQueryArgs.put(MISC_OPENRTB, bidRequestJson.toString());
            }
        }
        catch (JSONException e) {
            OXLog.error(TAG, "Failed to add OpenRTB query arg");
        }

        StringBuilder queryArgString = new StringBuilder();
        for (String key : tempQueryArgs.keySet()) {
            String value = tempQueryArgs.get(key);
            value = value.trim();
            try {
                value = URLEncoder.encode(value, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                OXLog.error(TAG, "Failed to encode value: " + value + " from key: " + key);
                continue;
            }
            //URL encoder turns spaces to +. SDK to convert + to %20
            value = value.replace("+", "%20");
            queryArgString.append(key)
                          .append("=")
                          .append(value)
                          .append("&");
        }

        // Strip off the last "&"
        if (Utils.isNotBlank(queryArgString.toString())) {
            queryArgString = new StringBuilder(queryArgString.substring(0, queryArgString.length() - 1));
        }

        return queryArgString.toString();
    }

    public String getBaseUrl() {
        return mBaseUrl;
    }

    public AdRequestInput getAdRequestInput() {
        return mAdRequestInput;
    }
}
