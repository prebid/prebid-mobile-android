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

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Banner;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Pmp;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.imps.Video;

public class Imp extends BaseBid {
    public String id = null;
    public String displaymanager = null;
    public String displaymanagerver = null;
    public Integer instl = null;
    public Integer rewarded = null;
    public String tagid = null;
    public Integer secure = null;
    public Banner banner = null;
    public Video video = null;
    public Pmp pmp = null;
    public Native nativeObj;
    private Ext ext = null;

    public Integer clickBrowser = null;

    JSONObject jsonObject;

    public JSONObject getJsonObject() throws JSONException {
        this.jsonObject = new JSONObject();
        toJSON(jsonObject, "id", id);
        toJSON(jsonObject, "displaymanager", displaymanager);
        toJSON(jsonObject, "displaymanagerver", displaymanagerver);
        toJSON(jsonObject, "instl", instl);
        toJSON(jsonObject, "tagid", tagid);
        toJSON(jsonObject, "rwdd", rewarded);
        toJSON(jsonObject, "clickbrowser", clickBrowser);

        toJSON(jsonObject, "secure", secure);

        toJSON(jsonObject, "banner", (banner != null) ? banner.getJsonObject() : null);
        toJSON(jsonObject, "video", (video != null) ? video.getJsonObject() : null);
        toJSON(jsonObject, "native", (nativeObj != null) ? nativeObj.getJsonObject() : null);
        toJSON(jsonObject, "pmp", (pmp != null) ? pmp.getJsonObject() : null);
        toJSON(jsonObject, "ext", (ext != null) ? ext.getJsonObject() : null);

        return jsonObject;
    }

    public Ext getExt() {
        if (ext == null) {
            ext = new Ext();
        }

        return ext;
    }

    public Banner getBanner() {
        if (banner == null) {
            banner = new Banner();
        }
        return banner;
    }

    public Video getVideo() {
        if (video == null) {
            video = new Video();
        }
        return video;
    }

    public Native getNative() {
        if (nativeObj == null) {
            nativeObj = new Native();
        }
        return nativeObj;
    }

}
