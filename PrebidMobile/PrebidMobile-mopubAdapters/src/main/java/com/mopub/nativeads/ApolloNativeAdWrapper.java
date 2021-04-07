package com.mopub.nativeads;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openx.apollo.bidding.data.ntv.MediaData;
import com.openx.apollo.bidding.data.ntv.NativeAd;
import com.openx.apollo.bidding.data.ntv.NativeAdElementType;
import com.openx.apollo.bidding.listeners.NativeAdListener;
import com.openx.apollo.models.ntv.NativeEventTracker;

public class ApolloNativeAdWrapper extends BaseNativeAd {

    private final NativeAd mNativeAd;
    private final NativeAdListener mNativeAdListener = new NativeAdListener() {

        @Override
        public void onAdClicked(NativeAd nativeAd) {
            notifyAdClicked();
        }

        @Override
        public void onAdEvent(NativeAd nativeAd, NativeEventTracker.EventType eventType) {
            if (eventType == NativeEventTracker.EventType.IMPRESSION) {
                notifyAdImpressed();
            }
        }
    };

    @Nullable
    private String mMainImageUrl;
    @Nullable
    private String mIconImageUrl;
    @Nullable
    private String mCallToAction;
    @Nullable
    private String mTitle;
    @Nullable
    private String mText;

    ApolloNativeAdWrapper(NativeAd nativeAd) {
        mNativeAd = nativeAd;
        mNativeAd.setNativeAdListener(mNativeAdListener);
        mText = mNativeAd.getText();
        mTitle = mNativeAd.getTitle();
        mCallToAction = mNativeAd.getCallToAction();
        mMainImageUrl = mNativeAd.getImageUrl();
        mIconImageUrl = mNativeAd.getIconUrl();
    }

    @Override
    public void prepare(
        @NonNull
            View view) {
        mNativeAd.registerView(view);
    }

    @Override
    public void clear(
        @NonNull
            View view) {

    }

    @Override
    public void destroy() {
        mNativeAd.destroy();
    }

    void registerActionView(View view) {
        if (view == null) {
            return;
        }
        mNativeAd.registerClickView(view, NativeAdElementType.ACTION_VIEW);
    }

    void registerView(View view) {
        if (view == null) {
            return;
        }
        mNativeAd.registerView(view);
    }

    // Getters

    /**
     * Returns the String corresponding to the ad's title.
     */
    @Nullable
    final public String getTitle() {
        return mTitle;
    }

    /**
     * Returns the String corresponding to the ad's body text.
     */
    @Nullable
    final public String getText() {
        return mText;
    }

    /**
     * Returns the String url corresponding to the ad's main image.
     */
    @Nullable
    final public String getMainImageUrl() {
        return mMainImageUrl;
    }

    /**
     * Returns the String url corresponding to the ad's icon image.
     */
    @Nullable
    final public String getIconImageUrl() {
        return mIconImageUrl;
    }

    /**
     * Returns the Call To Action String (i.e. "Download" or "Learn More") associated with this ad.
     */
    @Nullable
    final public String getCallToAction() {
        return mCallToAction;
    }

    @Nullable
    final public MediaData getMediaData() {
        return mNativeAd.getNativeVideoAd().getMediaData();
    }
}
