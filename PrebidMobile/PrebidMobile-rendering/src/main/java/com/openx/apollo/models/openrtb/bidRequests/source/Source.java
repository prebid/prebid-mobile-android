package com.openx.apollo.models.openrtb.bidRequests.source;

import android.text.TextUtils;

import com.openx.apollo.models.openrtb.bidRequests.BaseBid;
import com.openx.apollo.models.openrtb.bidRequests.Ext;

import org.json.JSONException;
import org.json.JSONObject;

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
