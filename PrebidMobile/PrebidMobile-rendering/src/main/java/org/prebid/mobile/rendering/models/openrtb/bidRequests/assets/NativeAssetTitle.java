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

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

public class NativeAssetTitle extends NativeAsset {
    private Integer mLen;
    private Ext mTitleExt;

    public Integer getLen() {
        return mLen;
    }

    public void setLen(Integer len) {
        mLen = len;
    }

    public Ext getTitleExt() {
        if (mTitleExt == null) {
            mTitleExt = new Ext();
        }

        return mTitleExt;
    }

    @Override
    public JSONObject getAssetJsonObject() throws JSONException {
        JSONObject jsonObject = getParentJsonObject();
        JSONObject titleAssetJson = new JSONObject();

        toJSON(titleAssetJson, "len", mLen);
        toJSON(titleAssetJson, "ext", mTitleExt != null ? mTitleExt.getJsonObject() : null);
        toJSON(jsonObject, "title", titleAssetJson);

        return jsonObject;
    }
}
