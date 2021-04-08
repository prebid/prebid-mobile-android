package org.prebid.mobile.rendering.models.openrtb.bidRequests;

import org.json.JSONException;
import org.json.JSONObject;

public class Regs extends BaseBid {

    public Integer coppa = null;
    private Ext mExt = null;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        toJSON(jsonObject, "coppa", this.coppa);
        toJSON(jsonObject, "ext", (mExt != null) ? mExt.getJsonObject() : null);
        return jsonObject;
    }

    public Ext getExt() {
        if (mExt == null) {
            mExt = new Ext();
        }
        return mExt;
    }
}
