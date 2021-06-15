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
