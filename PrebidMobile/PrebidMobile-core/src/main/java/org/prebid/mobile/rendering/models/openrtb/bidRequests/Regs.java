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
import org.prebid.mobile.LogUtil;

import java.util.ArrayList;

public class Regs extends BaseBid {

    public Integer coppa = null;
    private Ext ext = null;
    @Nullable
    private String gppString;
    @Nullable
    private JSONArray gppSid;

    /**
     * When you add a new field to this list, don't forget to add it to the {@link org.prebid.mobile.OpenRtbMerger}.
     */
    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        toJSON(jsonObject, "gpp", gppString);
        toJSON(jsonObject, "gpp_sid", gppSid);
        toJSON(jsonObject, "coppa", this.coppa);
        toJSON(jsonObject, "ext", (ext != null) ? ext.getJsonObject() : null);
        return jsonObject;
    }

    public Ext getExt() {
        if (ext == null) {
            ext = new Ext();
        }
        return ext;
    }

    public void setGppString(@Nullable String gppString) {
        this.gppString = gppString;
    }

    public void setGppSid(@Nullable String gppSid) {
        if (gppSid != null && !gppSid.isEmpty()) {
            try {
                String[] splitResult = gppSid.split("_");
                ArrayList<Integer> list = new ArrayList<>(splitResult.length);
                for (String version : splitResult) {
                    if (!version.isEmpty()) {
                        list.add(Integer.valueOf(version));
                    }
                }
                if (!list.isEmpty()) {
                    this.gppSid = new JSONArray(list);
                }
            } catch (Exception exception) {
                LogUtil.error("Can't parse GPP Sid. Current value: " + gppSid);
            }
        }
    }

}
