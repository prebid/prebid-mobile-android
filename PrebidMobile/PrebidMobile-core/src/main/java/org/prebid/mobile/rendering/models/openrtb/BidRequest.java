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
import org.prebid.mobile.OpenRtbMerger;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.App;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.BaseBid;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Device;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Imp;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Regs;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.User;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.source.Source;

import java.util.ArrayList;

public class BidRequest extends BaseBid {

    private String id;
    private App app = null;
    private Device device = null;
    private ArrayList<Imp> imps = new ArrayList<>();
    private Regs regs = null;
    private User user = null;
    private Source source = null;
    @Nullable
    private String impOrtbConfig;
    @Deprecated
    @Nullable
    private String openRtb;
    private Ext ext = null;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (imps != null && imps.size() > 0) {

            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < imps.size(); i++) {
                Imp imp = imps.get(i);
                JSONObject impJson = imp.getJsonObject();
                if (i == 0) {
                    impJson = OpenRtbMerger.globalMerge(impJson, impOrtbConfig);
                }
                jsonArray.put(impJson);
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
        jsonObject = OpenRtbMerger.globalMerge(jsonObject, openRtb);
        jsonObject = OpenRtbMerger.globalMerge(jsonObject, TargetingParams.getGlobalOrtbConfig());
        return jsonObject;
    }

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
    public String getImpOrtbConfig() {
        return impOrtbConfig;
    }

    public void setImpOrtbConfig(@Nullable String impOrtbConfig) {
        this.impOrtbConfig = impOrtbConfig;
    }

    public void setOpenRtb(@Nullable String openRtb) {
        this.openRtb = openRtb;
    }
}
