package com.mopub.nativeads;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.bidding.data.ntv.MediaData;
import org.prebid.mobile.rendering.bidding.data.ntv.MediaView;
import org.prebid.mobile.rendering.listeners.MediaViewListener;

import java.util.WeakHashMap;

import static android.view.View.VISIBLE;

public class PrebidNativeAdRenderer implements MoPubAdRenderer<PrebidNativeAdWrapper> {

    @NonNull
    private final ViewBinder mViewBinder;
    @NonNull
    private final WeakHashMap<View, StaticNativeViewHolder> mViewHolderMap;
    private Integer mMediaViewResId;
    private MediaViewListener mMediaViewListener;

    public PrebidNativeAdRenderer(
        @NonNull
        final ViewBinder viewBinder) {
        mViewBinder = viewBinder;
        mViewHolderMap = new WeakHashMap<>();
    }

    @NonNull
    @Override
    public View createAdView(
        @NonNull
            Context context,
        @Nullable
            ViewGroup parent) {
        return LayoutInflater
            .from(context)
            .inflate(mViewBinder.layoutId, parent, false);
    }

    @Override
    public void renderAdView(
        @NonNull
            View view,
        @NonNull
                PrebidNativeAdWrapper ad) {
        StaticNativeViewHolder staticNativeViewHolder = mViewHolderMap.get(view);
        if (staticNativeViewHolder == null) {
            staticNativeViewHolder = StaticNativeViewHolder.fromViewBinder(view, mViewBinder);
            mViewHolderMap.put(view, staticNativeViewHolder);
        }

        update(staticNativeViewHolder, ad);
        setViewVisibility(staticNativeViewHolder, VISIBLE);
    }

    @Override
    public boolean supports(
        @NonNull
            BaseNativeAd nativeAd) {
        return nativeAd instanceof PrebidNativeAdWrapper;
    }

    public void setMediaViewResId(int resId) {
        mMediaViewResId = resId;
    }

    public void setMediaViewListener(MediaViewListener mediaViewListener) {
        mMediaViewListener = mediaViewListener;
    }

    private void update(
        @NonNull
        final StaticNativeViewHolder staticNativeViewHolder,
        @NonNull
        final PrebidNativeAdWrapper adWrapper) {
        NativeRendererHelper.addTextView(staticNativeViewHolder.titleView,
                                         adWrapper.getTitle());
        NativeRendererHelper.addTextView(staticNativeViewHolder.textView, adWrapper.getText());
        NativeRendererHelper.addTextView(staticNativeViewHolder.callToActionView, adWrapper.getCallToAction());
        NativeImageHelper.loadImageView(adWrapper.getMainImageUrl(),
                                        staticNativeViewHolder.mainImageView);
        NativeImageHelper.loadImageView(adWrapper.getIconImageUrl(),
                                        staticNativeViewHolder.iconImageView);
        adWrapper.registerActionView(staticNativeViewHolder.callToActionView);
        loadMediaView(staticNativeViewHolder.mainView, adWrapper.getMediaData());
    }

    private void setViewVisibility(
        @NonNull
        final StaticNativeViewHolder staticNativeViewHolder,
        final int visibility) {
        if (staticNativeViewHolder.mainView != null) {
            staticNativeViewHolder.mainView.setVisibility(visibility);
        }
    }

    private void loadMediaView(View nativeView, MediaData mediaData) {
        if (mediaData.isEmpty() || mMediaViewResId == null) {
            return;
        }
        View targetView = nativeView.findViewById(mMediaViewResId);
        if (targetView instanceof MediaView) {
            MediaView mediaView = (MediaView) targetView;
            mediaView.setMediaViewListener(mMediaViewListener);
            mediaView.loadMedia(mediaData);
        }
    }
}
