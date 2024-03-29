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

package org.prebid.mobile.rendering.models.openrtb;

import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.*;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.source.Source;

import java.util.ArrayList;
import java.util.Iterator;

public class BidRequest extends BaseBid {

    private String id;
    private App app = null;
    private Device device = null;
    private ArrayList<Imp> imps = new ArrayList<>();
    private Regs regs = null;
    private User user = null;
    private Source source = null;
    @Nullable
    private JSONObject arbitraryJSONConfig;
    @Nullable
    private String ortbConfig;
    private Ext ext = null;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (arbitraryJSONConfig != null) {
            deepMerge(arbitraryJSONConfig, jsonObject);
        }
        if (imps != null && imps.size() > 0) {

            JSONArray jsonArray = new JSONArray();

            for (Imp i : imps) {
                jsonArray.put(i.getJsonObject());
            }

            toJSON(jsonObject, "imp", jsonArray);
        }

        toJSON(jsonObject, "id", !TextUtils.isEmpty(id) ? id : null);
        toJSON(jsonObject, "app", (app != null) ? app.getJsonObject() : null);
        toJSON(jsonObject, "device", (device != null) ? device.getJsonObject() : null);
        toJSON(jsonObject, "regs", (regs != null) ? regs.getJsonObject() : null);
        toJSON(jsonObject, "user", (user != null) ? user.getJsonObject() : null);
        toJSON(jsonObject, "source", source != null ? source.getJsonObject() : null);
        toJSON(jsonObject, "ext", ext != null ? ext.getJsonObject() : null);
        toJSON(jsonObject, "test", PrebidMobile.getPbsDebug() ? 1 : null);
        jsonObject = mergeOrtbConfig(jsonObject);
        return jsonObject;
    }

    private JSONObject mergeOrtbConfig(JSONObject bidRequestJson) {
        try {
            if (ortbConfig == null) {
                return bidRequestJson;
            }
            JSONObject ortbConfigObject = new JSONObject(ortbConfig);
            //remove protected fields
            if (ortbConfigObject.has("regs")) {
                ortbConfigObject.remove("regs");
            }
            if (ortbConfigObject.has("device")) {
                ortbConfigObject.remove("device");
            }
            if (ortbConfigObject.has("geo")) {
                ortbConfigObject.remove("geo");
            }
            if (ortbConfigObject.has("ext")) {
                if (ortbConfigObject.get("ext") instanceof JSONObject) {
                    ortbConfigObject.getJSONObject("ext").remove("gdpr");
                    ortbConfigObject.getJSONObject("ext").remove("us_privacy");
                    ortbConfigObject.getJSONObject("ext").remove("consent");
                }
            }
            return deepMerge(ortbConfigObject, bidRequestJson);
        } catch(Exception e) {
            LogUtil.error("ORTBConfig is not valid JSON");
            return bidRequestJson;
        }
    }

    /**
     * Merge "source" into "target". If fields have equal name, merge them recursively.
     * @return the merged object (target).
     */
    private static JSONObject deepMerge(JSONObject source, JSONObject target) throws JSONException {
        for (Iterator<String> it = source.keys(); it.hasNext(); ) {
            String key = it.next();
            Object value = source.get(key);
            if (!target.has(key)) {
                // new value for "key":
                target.put(key, value);
            } else {
                // existing value for "key" - recursively deep merge:
                if (value instanceof JSONObject) {
                    JSONObject valueJson = (JSONObject) value;
                    deepMerge(valueJson, target.getJSONObject(key));
                } else if (value instanceof JSONArray) {
                    if (target.get(key) instanceof JSONArray) {
                        JSONArray sourceArray = (JSONArray) value;
                        for (int i = 0; i < sourceArray.length(); i++) {
                            target.getJSONArray(key).put(sourceArray.getJSONObject(i));
                        }
                    }
                } else {
                    target.put(key, value);
                }
            }
        }
        return target;
    }

    // Accessors to prevent NPE while maintaining null if object is not set

    // App
    public App getApp() {
        if (app == null) {
            app = new App();
        }

        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    // Device
    public Device getDevice() {
        if (device == null) {
            device = new Device();
        }

        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    // Imp
    public ArrayList<Imp> getImp() {
        return imps;
    }

    public void setImp(ArrayList<Imp> imp) {
        imps = imp;
    }

    // Regs
    public Regs getRegs() {
        if (regs == null) {
            regs = new Regs();
        }

        return regs;
    }

    public void setRegs(Regs regs) {
        this.regs = regs;
    }

    // User
    public User getUser() {
        if (user == null) {
            user = new User();
        }

        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Source getSource() {
        if (source == null) {
            source = new Source();
        }

        return source;
    }

    public void setId(String id) {
        this.id = id;
    }

    @VisibleForTesting
    public String getId() {
        return id;
    }

    public Ext getExt() {
        if (ext == null) {
            ext = new Ext();
        }
        return ext;
    }

    @Nullable
    public JSONObject getArbitraryConfig() {
        return arbitraryJSONConfig;
    }

    public void setArbitraryConfig(@Nullable JSONObject config) {
        this.arbitraryJSONConfig = config;
    }

    @Nullable
    public String getOrtbConfig() {
        return ortbConfig;
    }

    public void setOrtbConfig(@Nullable String ortbConfig) {
        this.ortbConfig = ortbConfig;
    }
}
