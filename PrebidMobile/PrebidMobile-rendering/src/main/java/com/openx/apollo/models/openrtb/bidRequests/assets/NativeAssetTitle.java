package com.openx.apollo.models.openrtb.bidRequests.assets;

import com.openx.apollo.models.openrtb.bidRequests.Ext;

import org.json.JSONException;
import org.json.JSONObject;

public class NativeAssetTitle extends NativeAsset {
    private Integer mLen;
    private Ext mTitleExt;

    public Integer getLen() {
        return mLen;
    }

    public void setLen(Integer len) {
        mLen = len;
    }

    public Ext getTitleExt() {
        if (mTitleExt == null) {
            mTitleExt = new Ext();
        }

        return mTitleExt;
    }

    @Override
    public JSONObject getAssetJsonObject() throws JSONException {
        JSONObject jsonObject = getParentJsonObject();
        JSONObject titleAssetJson = new JSONObject();

        toJSON(titleAssetJson, "len", mLen);
        toJSON(titleAssetJson, "ext", mTitleExt != null ? mTitleExt.getJsonObject() : null);
        toJSON(jsonObject, "title", titleAssetJson);

        return jsonObject;
    }
}
