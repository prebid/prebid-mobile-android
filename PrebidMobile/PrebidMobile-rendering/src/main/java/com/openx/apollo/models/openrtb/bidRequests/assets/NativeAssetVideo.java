package com.openx.apollo.models.openrtb.bidRequests.assets;

import com.openx.apollo.models.openrtb.bidRequests.Ext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NativeAssetVideo extends NativeAsset {
    private String[] mMimes;
    private Integer mMinDuration;
    private Integer mMaxDuration;
    private Integer[] mProtocols;
    private Ext mVideoExt;

    public String[] getMimes() {
        return mMimes;
    }

    public void setMimes(String[] mimes) {
        mMimes = mimes;
    }

    public Integer getMinDuration() {
        return mMinDuration;
    }

    public void setMinDuration(Integer minDuration) {
        mMinDuration = minDuration;
    }

    public Integer getMaxDuration() {
        return mMaxDuration;
    }

    public void setMaxDuration(Integer maxDuration) {
        mMaxDuration = maxDuration;
    }

    public Integer[] getProtocols() {
        return mProtocols;
    }

    public void setProtocols(Integer[] protocols) {
        mProtocols = protocols;
    }

    public Ext getVideoExt() {
        if (mVideoExt == null) {
            mVideoExt = new Ext();
        }
        return mVideoExt;
    }

    @Override
    public JSONObject getAssetJsonObject() throws JSONException {
        JSONObject jsonObject = getParentJsonObject();
        JSONObject videoAssetJson = new JSONObject();

        toJSON(videoAssetJson, "minduration", mMinDuration);
        toJSON(videoAssetJson, "maxduration", mMaxDuration);
        toJSON(videoAssetJson, "ext", mVideoExt != null ? mVideoExt.getJsonObject() : null);

        if (isArrayValid(mMimes)) {
            JSONArray mimesJsonArray = createJsonArray(mMimes);

            toJSON(videoAssetJson, "mimes", mimesJsonArray);
        }

        if (isArrayValid(mProtocols)) {
            JSONArray protocolsJsonArray = createJsonArray(mProtocols);

            toJSON(videoAssetJson, "protocols", protocolsJsonArray);
        }

        toJSON(jsonObject, "video", videoAssetJson);

        return jsonObject;
    }
}
