package com.openx.apollo.models.openrtb.bidRequests.imps;

import androidx.annotation.VisibleForTesting;

import com.openx.apollo.models.openrtb.bidRequests.BaseBid;
import com.openx.apollo.models.openrtb.bidRequests.imps.pmps.Format;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

public class Banner extends BaseBid {

    public Integer pos = null;
    public int[] api;

    private HashSet<Format> mFormats = new HashSet<>();

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        toJSON(jsonObject, "pos", this.pos);

        if (api != null) {
            JSONArray jsonArray = new JSONArray();
            for (int apiItem : api) {
                jsonArray.put(apiItem);
            }
            toJSON(jsonObject, "api", jsonArray);
        }

        if (mFormats.size() > 0) {
            JSONArray formatsArray = new JSONArray();
            for (Format format : mFormats) {
                formatsArray.put(format.getJsonObject());
            }
            toJSON(jsonObject, "format", formatsArray);
        }

        return jsonObject;
    }

    public void addFormat(int w, int h) {
        mFormats.add(new Format(w, h));
    }

    @VisibleForTesting
    public HashSet<Format> getFormats() {
        return mFormats;
    }
}
