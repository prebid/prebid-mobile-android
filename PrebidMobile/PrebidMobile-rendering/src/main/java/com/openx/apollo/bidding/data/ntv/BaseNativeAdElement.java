package com.openx.apollo.bidding.data.ntv;

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
