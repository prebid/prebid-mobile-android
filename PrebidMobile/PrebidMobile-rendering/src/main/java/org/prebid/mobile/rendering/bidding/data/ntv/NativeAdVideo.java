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
