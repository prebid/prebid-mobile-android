package org.prebid.mobile;

import org.json.JSONArray;
import org.json.JSONObject;

public class NativeDataAsset extends NativeAsset {
    public NativeDataAsset() {
        super(REQUEST_ASSET.DATA);
    }

    public enum DATA_TYPE {
        SPONSORED(1),
        DESC(2),
        RATING(3),
        LIKES(4),
        DOWNLOADS(5),
        PRICE(6),
        SALEPRICE(7),
        PHONE(8),
        ADDRESS(9),
        DESC2(10),
        DESPLAYURL(11),
        CTATEXT(12),
        CUSTOM(500);
        private int id;

        DATA_TYPE(final int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public void setID(int id) throws Exception {
            if (this.equals(CUSTOM) && id >= 500) {
                this.id = id;
            } else {
                throw new Exception("Invalid input, should only set value on CUSTOM, should only use 500 above.");
            }
        }
    }

    private DATA_TYPE dataType = null;

    public DATA_TYPE getDataType() {
        return dataType;
    }

    public void setDataType(DATA_TYPE dataType) {
        this.dataType = dataType;
    }

    private int len = -1;

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    private boolean required = false;

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    private Object dataExt = null;

    public Object getDataExt() {
        return dataExt;
    }

    public void setDataExt(Object dataExt) {
        if (dataExt instanceof JSONObject || dataExt instanceof JSONArray) {
            this.dataExt = dataExt;
        }
    }

    private Object assetExt = null;

    public Object getAssetExt() {
        return assetExt;
    }

    public void setAssetExt(Object assetExt) {
        if (assetExt instanceof JSONArray || assetExt instanceof JSONObject) {
            this.assetExt = assetExt;
        }
    }
}
