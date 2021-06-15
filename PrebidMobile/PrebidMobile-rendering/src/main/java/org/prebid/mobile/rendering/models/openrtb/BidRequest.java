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

import androidx.annotation.VisibleForTesting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
    private String mId;
    private App mApp = null;
    private Device mDevice = null;
    private ArrayList<Imp> mImps = new ArrayList<>();
    private Regs mRegs = null;
    private User mUser = null;
    private Source mSource = null;

    private Ext mExt = null;

    public JSONObject getJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        if (mImps != null && mImps.size() > 0) {

            JSONArray jsonArray = new JSONArray();

            for (Imp i : mImps) {
                jsonArray.put(i.getJsonObject());
            }

            toJSON(jsonObject, "imp", jsonArray);
        }

        toJSON(jsonObject, "id", !TextUtils.isEmpty(mId) ? mId : null);
        toJSON(jsonObject, "app", (mApp != null) ? mApp.getJsonObject() : null);
        toJSON(jsonObject, "device", (mDevice != null) ? mDevice.getJsonObject() : null);
        toJSON(jsonObject, "regs", (mRegs != null) ? mRegs.getJsonObject() : null);
        toJSON(jsonObject, "user", (mUser != null) ? mUser.getJsonObject() : null);
        toJSON(jsonObject, "source", mSource != null ? mSource.getJsonObject() : null);
        toJSON(jsonObject, "ext", mExt != null ? mExt.getJsonObject() : null);

        return jsonObject;
    }

    // Accessors to prevent NPE while maintaining null if object is not set

    // App
    public App getApp() {
        if (mApp == null) {
            mApp = new App();
        }

        return mApp;
    }

    public void setApp(App app) {
        mApp = app;
    }

    // Device
    public Device getDevice() {
        if (mDevice == null) {
            mDevice = new Device();
        }

        return mDevice;
    }

    public void setDevice(Device device) {
        mDevice = device;
    }

    // Imp
    public ArrayList<Imp> getImp() {
        return mImps;
    }

    public void setImp(ArrayList<Imp> imp) {
        mImps = imp;
    }

    // Regs
    public Regs getRegs() {
        if (mRegs == null) {
            mRegs = new Regs();
        }

        return mRegs;
    }

    public void setRegs(Regs regs) {
        mRegs = regs;
    }

    // User
    public User getUser() {
        if (mUser == null) {
            mUser = new User();
        }

        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public void setSource(Source source) {
        mSource = source;
    }

    public Source getSource() {
        if (mSource == null) {
            mSource = new Source();
        }

        return mSource;
    }

    public void setId(String id) {
        mId = id;
    }

    @VisibleForTesting
    public String getId() {
        return mId;
    }

    public Ext getExt() {
        if (mExt == null) {
            mExt = new Ext();
        }
        return mExt;
    }
}
