package org.prebid.mobile.rendering.models.openrtb.bidRequests.assets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.BaseBid;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

public abstract class NativeAsset extends BaseBid {
    private boolean mRequired;
    private Ext mAssetExt;

    public void setRequired(boolean required) {
        mRequired = required;
    }

    public boolean isRequired() {
        return mRequired;
    }

    public Ext getAssetExt() {
        if (mAssetExt == null) {
            mAssetExt = new Ext();
        }
        return mAssetExt;
    }

    public abstract JSONObject getAssetJsonObject() throws JSONException;

    protected JSONObject getParentJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        toJSON(jsonObject, "required", mRequired ? 1 : 0);
        toJSON(jsonObject, "ext", mAssetExt != null ? mAssetExt.getJsonObject() : null);
        return jsonObject;
    }

    protected <T> boolean isArrayValid(T[] array) {
        return array != null && array.length > 0;
    }

    protected <T> JSONArray createJsonArray(T[] array) {
        JSONArray jsonArray = new JSONArray();

        for (T element : array) {
            jsonArray.put(element);
        }

        return jsonArray;
    }
}
