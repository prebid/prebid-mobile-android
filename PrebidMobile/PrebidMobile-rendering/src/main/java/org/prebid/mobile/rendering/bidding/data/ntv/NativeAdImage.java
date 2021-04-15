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
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetImage;

public class NativeAdImage extends BaseNativeAdElement {
    @Nullable
    private final NativeAssetImage.ImageType mImageType;
    @NonNull
    private final String mUrl;
    @Nullable
    private final Integer mW;
    @Nullable
    private final Integer mH;
    @Nullable
    private final Ext mExt;

    NativeAdImage(
        @Nullable
                NativeAssetImage.ImageType imageType,
        @NonNull
            String url,
        @Nullable
            Integer w,
        @Nullable
            Integer h,
        @Nullable
            Ext ext) {
        mImageType = imageType;
        mUrl = url;
        mW = w;
        mH = h;
        mExt = ext;
    }

    /**
     * @return Specified {@link NativeAssetImage.ImageType} or null.
     */
    @Nullable
    public NativeAssetImage.ImageType getImageType() {
        return mImageType;
    }

    /**
     * @return {@link #mUrl} or empty string.
     */
    @NonNull
    public String getUrl() {
        return mUrl;
    }

    /**
     * @return if present - {@link #mW}, null otherwise.
     */
    @Nullable
    public Integer getW() {
        return mW;
    }

    /**
     * @return if present - {@link #mH}, null otherwise.
     */
    @Nullable
    public Integer getH() {
        return mH;
    }

    /**
     * @return if present - link level ext, else - null.
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

        NativeAdImage that = (NativeAdImage) o;

        if (mImageType != that.mImageType) {
            return false;
        }
        if (!mUrl.equals(that.mUrl)) {
            return false;
        }
        if (mW != null ? !mW.equals(that.mW) : that.mW != null) {
            return false;
        }
        if (mH != null ? !mH.equals(that.mH) : that.mH != null) {
            return false;
        }
        return mExt != null ? mExt.equals(that.mExt) : that.mExt == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mImageType != null ? mImageType.hashCode() : 0);
        result = 31 * result + mUrl.hashCode();
        result = 31 * result + (mW != null ? mW.hashCode() : 0);
        result = 31 * result + (mH != null ? mH.hashCode() : 0);
        result = 31 * result + (mExt != null ? mExt.hashCode() : 0);
        return result;
    }
}
