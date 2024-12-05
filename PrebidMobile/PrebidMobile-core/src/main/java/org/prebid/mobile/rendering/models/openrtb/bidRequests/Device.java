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

import android.os.Build;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.devices.Geo;

public class Device extends BaseBid {

    @Nullable
    private static String deviceName = null;

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
    public String hwv = getDeviceName();

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

    private Ext ext;

    /**
     * When you add a new field to this list, don't forget to add it to the {@link org.prebid.mobile.OpenRtbMerger}.
     */
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
        toJSON(jsonObject, "ext", (ext != null) ? ext.getJsonObject() : null);

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
        if (ext == null) {
            ext = new Ext();
        }
        return ext;
    }

    @Nullable
    private static String getDeviceName() {
        if (deviceName == null) {
            deviceName = parseDeviceName();
            if (deviceName.isBlank()) return null;
            return deviceName;
        }

        if (deviceName.isBlank()) {
            return null;
        }

        return deviceName;
    }

    private static String parseDeviceName() {
        try {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;

            if (manufacturer.equals(Build.UNKNOWN)) {
                manufacturer = "";
            }
            if (model.equals(Build.UNKNOWN)) {
                model = "";
            }

            String result;
            if (manufacturer.isBlank() && model.isBlank()) {
                result = "";
            } else if (model.isBlank()) {
                result = manufacturer;
            } else if (manufacturer.isBlank()) {
                result = model;
            } else {
                if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
                    result = model;
                } else {
                    result = manufacturer + " " + model;
                }
            }
            return capitalizeFirstLetter(result);
        } catch (Throwable any) {
            LogUtil.error("Can't get device name: " + any.getMessage());
        }
        return "";
    }

    private static String capitalizeFirstLetter(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public enum DeviceType {
        MobileOrTablet(1),
        SMARTPHONE(4),
        TABLET(5);

        public final int value;

        DeviceType(int type) {
            this.value = type;
        }
    }
}
