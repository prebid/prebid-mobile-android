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

package org.prebid.mobile.rendering.models.openrtb.bidRequests.assets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.BaseBid;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

public abstract class NativeAsset extends BaseBid {
    private boolean mRequired;
    private Ext mAssetExt;

    public void setRequired(boolean required) {
        mRequired = required;
    }

    public boolean isRequired() {
        return mRequired;
    }

    public Ext getAssetExt() {
        if (mAssetExt == null) {
            mAssetExt = new Ext();
        }
        return mAssetExt;
    }

    public abstract JSONObject getAssetJsonObject() throws JSONException;

    protected JSONObject getParentJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        toJSON(jsonObject, "required", mRequired ? 1 : 0);
        toJSON(jsonObject, "ext", mAssetExt != null ? mAssetExt.getJsonObject() : null);
        return jsonObject;
    }

    protected <T> boolean isArrayValid(T[] array) {
        return array != null && array.length > 0;
    }

    protected <T> JSONArray createJsonArray(T[] array) {
        JSONArray jsonArray = new JSONArray();

        for (T element : array) {
            jsonArray.put(element);
        }

        return jsonArray;
    }
}
