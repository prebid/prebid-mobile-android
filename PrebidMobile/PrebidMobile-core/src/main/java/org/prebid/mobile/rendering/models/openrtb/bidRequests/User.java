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

package org.prebid.mobile.rendering.models.openrtb.bidRequests;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.DataObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.devices.Geo;

import java.util.ArrayList;

public class User extends BaseBid {

    public String keywords = null;
    public Geo geo = null;
    public String customData = null;
    public String id = null;
    public Ext ext = null;
    public String buyerUid = null;
    public ArrayList<DataObject> dataObjects = new ArrayList<>();

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        toJSON(jsonObject, "id", this.id);
        toJSON(jsonObject, "buyeruid", this.buyerUid);
        toJSON(jsonObject, "keywords", this.keywords);
        toJSON(jsonObject, "customdata", this.customData);
        toJSON(jsonObject, "geo", (geo != null) ? this.geo.getJsonObject() : null);

        if (ext != null) {
            JSONObject extJson = this.ext.getJsonObject();
            if (extJson.length() > 0) {
                toJSON(jsonObject, "ext", extJson);
            }
        }
        JSONArray dataJsonArray = getDataJsonArray();
        if (dataJsonArray != null) {
            toJSON(jsonObject, "data", dataJsonArray);
        }

        return jsonObject;
    }

    // Accessors to prevent NPE while maintaining null if object is not set

    // Geo
    public Geo getGeo() {
        if (geo == null) {
            geo = new Geo();
        }

        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    public Ext getExt() {
        if (ext == null) {
            ext = new Ext();
        }
        return ext;
    }

    public void setExt(Ext ext) {
        this.ext = ext;
    }

    @Nullable
    private JSONArray getDataJsonArray() {
        if (dataObjects.isEmpty()) return null;

        JSONArray result = new JSONArray();
        for (DataObject dataObject : dataObjects) {
            result.put(dataObject.getJsonObject());
        }
        return result;
    }

}
