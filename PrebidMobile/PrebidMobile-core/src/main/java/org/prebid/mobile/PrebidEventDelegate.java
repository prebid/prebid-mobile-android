package org.prebid.mobile;

import org.json.JSONObject;

public interface PrebidEventDelegate {

    void onBidResponse(JSONObject request, JSONObject response);

}
