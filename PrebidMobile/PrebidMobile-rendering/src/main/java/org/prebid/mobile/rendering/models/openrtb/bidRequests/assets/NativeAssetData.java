/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.models.openrtb.bidRequests.assets;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

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
