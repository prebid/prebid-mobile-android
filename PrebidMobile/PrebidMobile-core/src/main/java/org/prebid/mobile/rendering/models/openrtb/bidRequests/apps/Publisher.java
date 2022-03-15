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

package org.prebid.mobile.rendering.models.openrtb.bidRequests.apps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.BaseBid;

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
