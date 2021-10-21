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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

public class NativeAssetImage extends NativeAsset {

    private ImageType mType;
    private Integer mW;
    private Integer mWMin;
    private Integer mH;
    private Integer mHMin;
    private String[] mMimes;

    private Ext mImageExt;

    public enum ImageType {
        ICON(1),
        MAIN(3),
        CUSTOM(500);

        private int mId;

        ImageType(int typeId) {
            mId = typeId;
        }

        @Nullable
        public static ImageType getType(Integer id) {
            if (id == null || id < 0) {
                return null;
            }

            ImageType[] imageTypes = ImageType.values();
            for (ImageType imageType : imageTypes) {
                if (imageType.getId() == id) {
                    return imageType;
                }
            }

            ImageType custom = ImageType.CUSTOM;
            custom.setId(id);
            return custom;
        }

        public void setId(int id) {
            if (this.equals(CUSTOM) && !inExistingValue(id)) {
                mId = id;
            }
        }

        public int getId() {
            return mId;
        }

        private boolean inExistingValue(int id) {
            ImageType[] possibleValues = this.getDeclaringClass().getEnumConstants();
            for (ImageType value : possibleValues) {
                if (!value.equals(ImageType.CUSTOM) && value.getId() == id) {
                    return true;
                }
            }
            return false;
        }
    }

    public void setType(ImageType imageType) {
        mType = imageType;
    }

    public ImageType getType() {
        return mType;
    }

    public Integer getW() {
        return mW;
    }

    public void setW(Integer w) {
        mW = w;
    }

    public Integer getWMin() {
        return mWMin;
    }

    public void setWMin(Integer WMin) {
        mWMin = WMin;
    }

    public Integer getH() {
        return mH;
    }

    public void setH(Integer h) {
        mH = h;
    }

    public Integer getHMin() {
        return mHMin;
    }

    public void setHMin(Integer HMin) {
        mHMin = HMin;
    }

    public String[] getMimes() {
        return mMimes;
    }

    public void setMimes(String[] mimes) {
        mMimes = mimes;
    }

    public Ext getImageExt() {
        if (mImageExt == null) {
            mImageExt = new Ext();
        }
        return mImageExt;
    }

    @Override
    public JSONObject getAssetJsonObject() throws JSONException {
        JSONObject jsonObject = getParentJsonObject();
        JSONObject imageAssetJson = new JSONObject();

        toJSON(imageAssetJson, "type", mType != null ? mType.getId() : null);
        toJSON(imageAssetJson, "w", mW);
        toJSON(imageAssetJson, "wmin", mWMin);
        toJSON(imageAssetJson, "h", mH);
        toJSON(imageAssetJson, "hmin", mHMin);
        toJSON(imageAssetJson, "ext", mImageExt != null ? mImageExt.getJsonObject() : null);

        if (isArrayValid(mMimes)) {
            JSONArray mimesJsonArray = createJsonArray(mMimes);

            toJSON(imageAssetJson, "mimes", mimesJsonArray);
        }

        toJSON(jsonObject, "img", imageAssetJson);

        return jsonObject;
    }
}
