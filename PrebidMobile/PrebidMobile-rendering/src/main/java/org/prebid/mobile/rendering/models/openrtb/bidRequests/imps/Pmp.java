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
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.pmps.Deals;

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
