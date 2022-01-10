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
import org.prebid.mobile.rendering.models.openrtb.bidRequests.devices.Geo;

public class Device extends BaseBid {

    // User Agent
    public String ua = null;

    private Integer dnt = null;
    public Integer lmt = null;
    public String ip = null;
    private String ipv6 = null;

    //TODO: ORTB2.5: auto detect this?
    public Integer devicetype = null;

    public String make = null;
    public String model = null;
    public String os = null;
    public String osv = null;

    //TODO: ORTB2.5: detect this? How?
    //Hardware version of the device (e.g., “5S” for iPhone 5S).
    public String hwv = null;

    public String flashver = null;
    public String language = null;
    public String carrier = null;
    public String mccmnc = null;
    public String ifa = null;
    public String didsha1 = null;
    public String didmd5 = null;
    public String dpidsha1 = null;
    public String dpidmd5 = null;
    //Physical height of the screen in pixels.
    public Integer h = null;
    //Physical width of the screen in pixels.
    public Integer w = null;

    //Screen size as pixels per linear inch.
    //TODO: ORTB2.5: auto detect?
    public Integer ppi = null;

    public Integer js = null;
    public Integer connectiontype = null;

    //The ratio of physical pixels to device independent pixels.
    public Float pxratio = null;

    public Geo geo = null;

    private Ext mExt;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        toJSON(jsonObject, "ua", ua);
        toJSON(jsonObject, "dnt", dnt);
        toJSON(jsonObject, "lmt", lmt);
        toJSON(jsonObject, "ip", ip);
        toJSON(jsonObject, "ipv6", ipv6);
        toJSON(jsonObject, "devicetype", devicetype);
        toJSON(jsonObject, "make", make);
        toJSON(jsonObject, "model", model);
        toJSON(jsonObject, "os", os);
        toJSON(jsonObject, "osv", osv);
        toJSON(jsonObject, "hwv", hwv);
        toJSON(jsonObject, "flashver", flashver);
        toJSON(jsonObject, "language", language);
        toJSON(jsonObject, "carrier", carrier);
        toJSON(jsonObject, "mccmnc", mccmnc);
        toJSON(jsonObject, "ifa", ifa);
        toJSON(jsonObject, "didsha1", didsha1);
        toJSON(jsonObject, "didmd5", didmd5);
        toJSON(jsonObject, "dpidsha1", dpidsha1);
        toJSON(jsonObject, "dpidmd5", dpidmd5);
        toJSON(jsonObject, "h", h);
        toJSON(jsonObject, "w", w);
        toJSON(jsonObject, "ppi", ppi);
        toJSON(jsonObject, "js", js);
        toJSON(jsonObject, "connectiontype", connectiontype);
        toJSON(jsonObject, "pxratio", pxratio);
        toJSON(jsonObject, "ext", (mExt != null) ? mExt.getJsonObject() : null);

        toJSON(jsonObject, "geo", (geo != null) ? geo.getJsonObject() : null);

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
        if (mExt == null) {
            mExt = new Ext();
        }
        return mExt;
    }
}
