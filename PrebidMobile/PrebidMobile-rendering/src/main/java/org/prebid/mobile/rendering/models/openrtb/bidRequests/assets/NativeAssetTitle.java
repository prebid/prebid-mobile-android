package org.prebid.mobile.rendering.models.openrtb.bidRequests.assets;

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

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
