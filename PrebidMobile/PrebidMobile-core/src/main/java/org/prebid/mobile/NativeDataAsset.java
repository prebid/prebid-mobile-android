package org.prebid.mobile;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Native data object for requesting asset.
 */
public class NativeDataAsset extends NativeAsset {
    public NativeDataAsset() {
        super(REQUEST_ASSET.DATA);
    }

    /**
     * Data type.
     */
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

        public void setID(int id) {
            if (this.equals(CUSTOM) && !inExistingValue(id)) {
                this.id = id;
            }
        }
        private boolean inExistingValue(int id) {
            DATA_TYPE[] possibleValues = this.getDeclaringClass().getEnumConstants();
            for (DATA_TYPE value : possibleValues) {
                if (!value.equals(DATA_TYPE.CUSTOM) && value.getID() == id) {
                    return true;
                }
            }
            return false;
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

    @Override
    public JSONObject getJsonObject(int idCount) {
        JSONObject result = new JSONObject();

        try {
            if (PrebidMobile.shouldAssignNativeAssetID()) {
                result.putOpt("id", idCount);
            }

            result.putOpt("required", required ? 1 : 0);
            result.putOpt("ext", assetExt);

            JSONObject dataObject = new JSONObject();
            dataObject.putOpt("type", dataType != null ? dataType.getID() : null);
            dataObject.putOpt("len", len);
            dataObject.putOpt("ext", dataExt);

            result.put("data", dataObject);
        } catch (Exception exception) {
            LogUtil.error("NativeTitleAsset", "Can't create json object: " + exception.getMessage());
        }

        return result;
    }
}
