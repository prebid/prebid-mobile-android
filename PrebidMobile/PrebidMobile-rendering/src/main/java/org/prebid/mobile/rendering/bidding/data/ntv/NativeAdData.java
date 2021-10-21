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

package org.prebid.mobile.rendering.bidding.data.ntv;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetData;

public class NativeAdData extends BaseNativeAdElement {
    @Nullable
    private final NativeAssetData.DataType mDataType;
    @NonNull
    private final String mValue;
    @Nullable
    private final Integer mLen;
    @Nullable
    private final Ext mExt;

    NativeAdData(
        @Nullable
            NativeAssetData.DataType dataType,
        @NonNull
            String value,
        @Nullable
            Integer len,
        @Nullable
            Ext ext) {
        mDataType = dataType;
        mValue = value;
        mLen = len;
        mExt = ext;
    }

    /**
     * @return Specified {@link NativeAssetData.DataType} or null.
     */
    @Nullable
    public NativeAssetData.DataType getDataType() {
        return mDataType;
    }

    /**
     * @return data value or empty string.
     */
    @NonNull
    public String getValue() {
        return mValue;
    }

    /**
     * @return if present - length, null otherwise
     */
    @Nullable
    public Integer getLen() {
        return mLen;
    }

    /**
     * @return if present - asset level ext, else - null.
     */
    @Nullable
    public Ext getExt() {
        return mExt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        NativeAdData data = (NativeAdData) o;

        if (mDataType != data.mDataType) {
            return false;
        }
        if (!mValue.equals(data.mValue)) {
            return false;
        }
        if (mLen != null ? !mLen.equals(data.mLen) : data.mLen != null) {
            return false;
        }
        return mExt != null ? mExt.equals(data.mExt) : data.mExt == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mDataType != null ? mDataType.hashCode() : 0);
        result = 31 * result + mValue.hashCode();
        result = 31 * result + (mLen != null ? mLen.hashCode() : 0);
        result = 31 * result + (mExt != null ? mExt.hashCode() : 0);
        return result;
    }
}
