package org.prebid.mobile.rendering.models.openrtb.bidRequests.imps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.BaseBid;

public class Video extends BaseBid {

    public String[] mimes = null;

    public Integer minduration = null;
    public Integer maxduration = null;

    public int[] protocols;

    //TODO: ORTB2.5: Auto detect? how?
    public Integer w = null;
    //TODO: ORTB2.5: Auto detect? how?
    public Integer h = null;

    public Integer linearity = null;

    public Integer minbitrate = null;
    public Integer maxbitrate = null;

    public int[] playbackmethod;

    public int[] delivery;
    public Integer pos = null;

    public Integer placement = null;

    public Integer playbackend;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        if (mimes != null) {

            JSONArray jsonArray = new JSONArray();

            for (String mime : mimes) {

                jsonArray.put(mime);
            }

            toJSON(jsonObject, "mimes", jsonArray);
        }

        toJSON(jsonObject, "minduration", minduration);
        toJSON(jsonObject, "maxduration", maxduration);

        toJSON(jsonObject, "playbackend", playbackend);

        if (protocols != null) {

            JSONArray jsonArray = new JSONArray();

            for (int protocol : protocols) {

                jsonArray.put(protocol);
            }

            toJSON(jsonObject, "protocols", jsonArray);
        }

        toJSON(jsonObject, "w", w);
        toJSON(jsonObject, "h", h);
        //toJSON(jsonObject, "startdelay", this.startdelay);
        toJSON(jsonObject, "linearity", linearity);

        toJSON(jsonObject, "minbitrate", minbitrate);
        toJSON(jsonObject, "maxbitrate", maxbitrate);

        toJSON(jsonObject, "placement", placement);

        if (playbackmethod != null) {

            JSONArray jsonArray = new JSONArray();

            for (int method : playbackmethod) {

                jsonArray.put(method);
            }

            toJSON(jsonObject, "playbackmethod", jsonArray);
        }

        if (delivery != null) {

            JSONArray jsonArray = new JSONArray();

            for (int del : delivery) {

                jsonArray.put(del);
            }

            toJSON(jsonObject, "delivery", jsonArray);
        }

        toJSON(jsonObject, "pos", pos);

        return jsonObject;
    }
}
