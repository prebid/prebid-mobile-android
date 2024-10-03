package org.prebid.mobile;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Native title object for requesting asset.
 */
public class NativeTitleAsset extends NativeAsset {

    private int len;
    private boolean required;
    private Object titleExt;
    private Object assetExt;

    public NativeTitleAsset() {
        super(REQUEST_ASSET.TITLE);
    }

    public void setLength(int len) {
        this.len = len;
    }

    public int getLen() {
        return len;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }

    public void setTitleExt(Object ext) {
        if (ext instanceof JSONArray || ext instanceof JSONObject) {
            this.titleExt = ext;
        }
    }

    public Object getTitleExt() {
        return titleExt;
    }

    public Object getAssetExt() {
        return assetExt;
    }

    public void setAssetExt(Object assetExt) {
        if (assetExt instanceof JSONArray || assetExt instanceof JSONObject) {
            this.assetExt = assetExt;
        }
    }

    @Override
    public JSONObject getJsonObject(int idCount) {
        JSONObject result = new JSONObject();

        try {
            if (PrebidMobile.shouldAssignNativeAssetID()) {
                result.putOpt("id", idCount);
            }

            result.putOpt("required", required ? 1 : 0);
            result.putOpt("ext", assetExt);

            JSONObject titleObject = new JSONObject();
            titleObject.putOpt("len", len);
            titleObject.putOpt("ext", titleExt);

            result.put("title", titleObject);
        } catch (Exception exception) {
            LogUtil.error("NativeTitleAsset", "Can't create json object: " + exception.getMessage());
        }

        return result;
    }

}
