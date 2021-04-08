package org.prebid.mobile.rendering.networking.urlBuilder;

import org.json.JSONObject;
import org.prebid.mobile.rendering.networking.parameters.AdRequestInput;
import org.prebid.mobile.rendering.utils.logger.OXLog;

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
