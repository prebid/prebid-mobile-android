package com.openx.apollo.models.openrtb;

import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;

import com.openx.apollo.models.openrtb.bidRequests.App;
import com.openx.apollo.models.openrtb.bidRequests.BaseBid;
import com.openx.apollo.models.openrtb.bidRequests.Device;
import com.openx.apollo.models.openrtb.bidRequests.Ext;
import com.openx.apollo.models.openrtb.bidRequests.Imp;
import com.openx.apollo.models.openrtb.bidRequests.Regs;
import com.openx.apollo.models.openrtb.bidRequests.User;
import com.openx.apollo.models.openrtb.bidRequests.source.Source;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
