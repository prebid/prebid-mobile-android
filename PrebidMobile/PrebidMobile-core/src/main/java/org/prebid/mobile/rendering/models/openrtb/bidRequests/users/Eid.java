package org.prebid.mobile.rendering.models.openrtb.bidRequests.users;

import com.google.android.gms.common.util.CollectionUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.BaseBid;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenRTB 2.6 Extended Identifier (EID) object
 * <a href="https://github.com/InteractiveAdvertisingBureau/openrtb2.x/blob/main/2.6.md#3227---object-eid-">OpenRTB 2.6 Spec - Object: EID (3.2.27)</a>
 */
public class Eid extends BaseBid {

    public String source = null;
    public List<Uid> uids = new ArrayList<>();
    public String inserter = null;
    public String matcher = null;
    public Integer mm = null;
    public Ext ext = null;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        toJSON(jsonObject, "source", this.source);

        if (!CollectionUtils.isEmpty(uids)) {
            JSONArray uidsArray = new JSONArray();
            for (Uid uid: uids) {
                uidsArray.put(uid.getJsonObject());
            }
            toJSON(jsonObject, "uids", uidsArray);
        }

        toJSON(jsonObject, "inserter", this.inserter);
        toJSON(jsonObject, "matcher", this.matcher);
        toJSON(jsonObject, "mm", this.mm);

        if (ext != null) {
            JSONObject extJson = ext.getJsonObject();
            if (extJson.length() > 0) {
                toJSON(jsonObject, "ext", extJson);
            }
        }

        return jsonObject;
    }
}
