/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
    public int[] api;

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

    public Integer plcmt = null;

    public Integer playbackend;

    public Integer startDelay;

    public int[] battr;

    public Boolean skippable = null;

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
        toJSON(jsonObject, "startdelay", startDelay);
        toJSON(jsonObject, "linearity", linearity);

        toJSON(jsonObject, "minbitrate", minbitrate);
        toJSON(jsonObject, "maxbitrate", maxbitrate);

        toJSON(jsonObject, "placement", placement);
        toJSON(jsonObject, "plcmt", plcmt);

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

        if (api != null) {
            JSONArray jsonArray = new JSONArray();
            for (int number : api) {
                jsonArray.put(number);
            }
            toJSON(jsonObject, "api", jsonArray);
        }

        toJSON(jsonObject, "pos", pos);

        if (battr != null) {
            JSONArray jsonArray = new JSONArray();
            for (int number : battr) {
                jsonArray.put(number);
            }
            toJSON(jsonObject, "battr", jsonArray);
        }

        if (skippable != null) {
            toJSON(jsonObject, "skip", skippable);
        }

        return jsonObject;
    }
}
