package org.prebid.mobile.rendering.bidding.data.ntv;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

public class NativeAdTitle extends BaseNativeAdElement {
    @NonNull
    private final String mText;
    @Nullable
    private final Integer mLen;
    @Nullable
    private final Ext mExt;

    NativeAdTitle(
        @NonNull
            String text,
        @Nullable
            Integer len,
        @Nullable
            Ext ext) {
        mText = text;
        mLen = len;
        mExt = ext;
    }

    /**
     * @return if present - text string, empty string otherwise
     */
    @NonNull
    public String getText() {
        return mText;
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

        NativeAdTitle that = (NativeAdTitle) o;

        if (!mText.equals(that.mText)) {
            return false;
        }
        if (mLen != null ? !mLen.equals(that.mLen) : that.mLen != null) {
            return false;
        }
        return mExt != null ? mExt.equals(that.mExt) : that.mExt == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mText.hashCode();
        result = 31 * result + (mLen != null ? mLen.hashCode() : 0);
        result = 31 * result + (mExt != null ? mExt.hashCode() : 0);
        return result;
    }
}
