/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

        public void setID(int id) {
            if (this.equals(CUSTOM) && id >= 500) {
                this.id = id;
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

    private Object dateExt = null;

    public Object getDateExt() {
        return dateExt;
    }

    public void setDateExt(Object dateExt) {
        if (dateExt instanceof JSONObject || dateExt instanceof JSONArray) {
            this.dateExt = dateExt;
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
