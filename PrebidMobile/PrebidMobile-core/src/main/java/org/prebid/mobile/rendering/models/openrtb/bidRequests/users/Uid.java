package org.prebid.mobile.rendering.models.openrtb.bidRequests.users;

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.BaseBid;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

/**
 * OpenRTB 2.6 User Identifier (UID) object.
 * <a href="https://github.com/InteractiveAdvertisingBureau/openrtb2.x/blob/main/2.6.md#3228---object-uid-">OpenRTB 2.6 Spec - Object: UID (3.2.28)</a>
 */
public class Uid extends BaseBid {

    public String id = null;
    public Integer atype = null;
    public Ext ext = null;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        toJSON(jsonObject, "id", this.id);
        toJSON(jsonObject, "atype", this.atype);
        if (ext != null) {
            JSONObject extJson = ext.getJsonObject();
            if (extJson.length() > 0) {
                toJSON(jsonObject, "ext", extJson);
            }
        }
        return jsonObject;
    }
}
