package com.openx.apollo.models.openrtb.bidRequests.imps;

import com.openx.apollo.models.openrtb.bidRequests.BaseBid;
import com.openx.apollo.models.openrtb.bidRequests.imps.pmps.Deals;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Pmp extends BaseBid {
    public Integer private_auction= null;

    public List<Deals> deals = new ArrayList<>();

    //deals
    //ext

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        toJSON(jsonObject, "private_auction", this.private_auction);

        if (deals != null && deals.size() > 0) {

            JSONArray jsonArray = new JSONArray();

            for (Deals i : deals) {
                jsonArray.put(i.getJsonObject());
            }

            toJSON(jsonObject, "deals", jsonArray);
        }

        return jsonObject;
    }
}
