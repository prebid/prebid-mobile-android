package com.openx.apollo.networking.urlBuilder;

import com.openx.apollo.networking.parameters.AdRequestInput;
import com.openx.apollo.utils.logger.OXLog;

import org.json.JSONObject;

public class BidUrlComponents extends URLComponents {
    private final static String TAG = BidUrlComponents.class.getSimpleName();

    public BidUrlComponents(String baseUrl, AdRequestInput adRequestInput) {
        super(baseUrl, adRequestInput);
    }

    @Override
    public String getQueryArgString() {
        String openrtb = "";
        try {
            JSONObject bidRequestJson = mAdRequestInput.getBidRequest().getJsonObject();
            if (bidRequestJson.length() > 0) {
                openrtb = bidRequestJson.toString();
            }
        }
        catch (Exception e) {
            OXLog.error(TAG, "Failed to add OpenRTB query arg");
        }

        return openrtb;
    }
}
