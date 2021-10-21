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

import androidx.annotation.Nullable;

public class BaseNativeAdElement {
    @Nullable
    private NativeAdLink mNativeAdLink;

    void setNativeAdLink(
        @Nullable
            NativeAdLink nativeAdLink) {
        mNativeAdLink = nativeAdLink;
    }

    @Nullable
    NativeAdLink getNativeAdLink() {
        return mNativeAdLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseNativeAdElement that = (BaseNativeAdElement) o;

        return mNativeAdLink != null
               ? mNativeAdLink.equals(that.mNativeAdLink)
               : that.mNativeAdLink == null;
    }

    @Override
    public int hashCode() {
        return mNativeAdLink != null ? mNativeAdLink.hashCode() : 0;
    }
}
