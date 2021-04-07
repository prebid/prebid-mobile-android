package com.openx.apollo.models.openrtb.bidRequests.assets;

import androidx.annotation.Nullable;

import com.openx.apollo.models.openrtb.bidRequests.Ext;

import org.json.JSONException;
import org.json.JSONObject;

public class NativeAssetData extends NativeAsset {
    private DataType mType;
    private Integer mLen;
    private Ext mDataExt;

    public enum DataType {
        SPONSORED(1),
        DESC(2),
        RATING(3),
        LIKES(4),
        DOWNLOADS(5),
        PRICE(6),
        SALE_PRICE(7),
        PHONE(8),
        ADDRESS(9),
        DESC_2(10),
        DISPLAY_URL(11),
        CTA_TEXT(12),
        CUSTOM(500);

        private int mId;

        DataType(int typeId) {
            mId = typeId;
        }

        @Nullable
        public static NativeAssetData.DataType getType(Integer id) {
            if (id == null || id < 0) {
                return null;
            }

            NativeAssetData.DataType[] dataTypes = NativeAssetData.DataType.values();
            for (NativeAssetData.DataType dataType : dataTypes) {
                if (dataType.getId() == id) {
                    return dataType;
                }
            }

            NativeAssetData.DataType custom = NativeAssetData.DataType.CUSTOM;
            custom.setId(id);
            return custom;
        }

        public int getId() {
            return mId;
        }

        public void setId(int id) {
            if (this.equals(CUSTOM) && !inExistingValue(id)) {
                mId = id;
            }
        }

        private boolean inExistingValue(int id) {
            DataType[] possibleValues = this.getDeclaringClass().getEnumConstants();
            for (DataType value : possibleValues) {
                if (!value.equals(CUSTOM) && value.getId() == id) {
                    return true;
                }
            }
            return false;
        }
    }

    public DataType getType() {
        return mType;
    }

    public void setType(DataType type) {
        mType = type;
    }

    public Integer getLen() {
        return mLen;
    }

    public void setLen(Integer len) {
        mLen = len;
    }

    public Ext getDataExt() {
        if (mDataExt == null) {
            mDataExt = new Ext();
        }
        return mDataExt;
    }

    public JSONObject getAssetJsonObject() throws JSONException {
        JSONObject jsonObject = getParentJsonObject();
        JSONObject nativeAssetDataJson = new JSONObject();

        toJSON(nativeAssetDataJson, "type", mType != null ? mType.getId() : null);
        toJSON(nativeAssetDataJson, "len", mLen);
        toJSON(nativeAssetDataJson, "ext", mDataExt != null ? mDataExt.getJsonObject() : null);

        toJSON(jsonObject, "data", nativeAssetDataJson);
        return jsonObject;
    }
}
