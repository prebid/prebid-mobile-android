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

public class NativeAdVideo extends BaseNativeAdElement {
    @NonNull
    private final MediaData mMediaData;

    NativeAdVideo(
        @NonNull
            MediaData mediaData) {
        mMediaData = mediaData;
    }

    NativeAdVideo() {
        mMediaData = new MediaData();
    }

    @NonNull
    public MediaData getMediaData() {
        return mMediaData;
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

        NativeAdVideo that = (NativeAdVideo) o;

        return mMediaData.equals(that.mMediaData);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mMediaData.hashCode();
        return result;
    }
}
