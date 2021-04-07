package com.openx.apollo.models.openrtb.bidRequests.apps;

import com.openx.apollo.models.openrtb.bidRequests.BaseBid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Publisher extends BaseBid {

    public String id = null;
    public String name = null;
    public String[] cat = null;

    public String domain = null;

    public JSONObject getJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();

        toJSON(jsonObject, "id", id);
        toJSON(jsonObject, "name", name);

        if (cat != null) {

            JSONArray jsonArray = new JSONArray();

            for (String catItem : cat) {

                jsonArray.put(catItem);
            }

            toJSON(jsonObject, "cat", jsonArray);
        }

        toJSON(jsonObject, "domain", domain);

        return jsonObject;
    }
}
