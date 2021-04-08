package org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.pmps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.BaseBid;

public class Deals extends BaseBid {
    public String id = null;
    public Float bidfloor;
    public String bidfloorcur = null;
    public Integer at= null;

    public String[] wseat = null;
    public String[] wadomain = null;

    //ext
    JSONObject jsonObject;

    public JSONObject getJsonObject() throws JSONException {
        this.jsonObject = new JSONObject();
        toJSON(jsonObject, "id", this.id);
        toJSON(jsonObject, "bidfloor", this.bidfloor);
        toJSON(jsonObject, "bidfloorcur", this.bidfloorcur);
        toJSON(jsonObject, "at", this.at);

        if (wseat != null) {

            JSONArray jsonArray = new JSONArray();

            for (String seat : wseat) {

                jsonArray.put(seat);
            }

            toJSON(jsonObject, "wseat", jsonArray);
        }

        if (wadomain != null) {

            JSONArray jsonArray = new JSONArray();

            for (String domain : wadomain) {

                jsonArray.put(domain);
            }

            toJSON(jsonObject, "wadomain", jsonArray);
        }

        return jsonObject;

    }
}
