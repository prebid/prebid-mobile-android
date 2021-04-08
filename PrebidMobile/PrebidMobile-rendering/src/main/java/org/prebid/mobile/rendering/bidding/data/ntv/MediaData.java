package org.prebid.mobile.rendering.bidding.data.ntv;

import android.text.TextUtils;

import androidx.annotation.NonNull;

public class MediaData {
    @NonNull
    private final String mVastTag;

    public MediaData(
        @NonNull
            String vastTag) {
        mVastTag = vastTag;
    }

    public MediaData() {
        mVastTag = "";
    }

    /**
     * @return if present - vast tag, else - empty string.
     */
    @NonNull
    String getVastTag() {
        return mVastTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MediaData mediaData = (MediaData) o;

        return mVastTag.equals(mediaData.mVastTag);
    }

    @Override
    public int hashCode() {
        return mVastTag.hashCode();
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(mVastTag);
    }
}
