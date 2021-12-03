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

package org.prebid.mobile.rendering.models.openrtb.bidRequests.source;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.BaseBid;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

public class Source extends BaseBid {
    private String mTid;
    private Ext mExt;

    public void setExt(Ext ext) {
        mExt = ext;
    }

    public void setTid(String tid) {
        mTid = tid;
    }

    public Ext getExt() {
        if (mExt == null) {
            mExt = new Ext();
        }
        return mExt;
    }

    public String getTid() {
        return mTid;
    }

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        toJSON(jsonObject, "tid", !TextUtils.isEmpty(mTid) ? mTid : null);
        toJSON(jsonObject, "ext", (mExt != null) ? mExt.getJsonObject() : null);

        return jsonObject;
    }
}
