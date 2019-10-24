package org.prebid.mobile;

import org.json.JSONArray;
import org.json.JSONObject;

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
}
