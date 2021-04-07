package com.mopub.nativeads;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openx.apollo.bidding.data.ntv.MediaData;
import com.openx.apollo.bidding.data.ntv.MediaView;
import com.openx.apollo.listeners.MediaViewListener;

import java.util.WeakHashMap;

import static android.view.View.VISIBLE;

public class ApolloNativeAdRenderer implements MoPubAdRenderer<ApolloNativeAdWrapper> {

    @NonNull
    private final ViewBinder mViewBinder;
    @NonNull
    private final WeakHashMap<View, StaticNativeViewHolder> mViewHolderMap;
    private Integer mMediaViewResId;
    private MediaViewListener mMediaViewListener;

    public ApolloNativeAdRenderer(
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
            ApolloNativeAdWrapper apolloAd) {
        StaticNativeViewHolder staticNativeViewHolder = mViewHolderMap.get(view);
        if (staticNativeViewHolder == null) {
            staticNativeViewHolder = StaticNativeViewHolder.fromViewBinder(view, mViewBinder);
            mViewHolderMap.put(view, staticNativeViewHolder);
        }

        update(staticNativeViewHolder, apolloAd);
        setViewVisibility(staticNativeViewHolder, VISIBLE);
    }

    @Override
    public boolean supports(
        @NonNull
            BaseNativeAd nativeAd) {
        return nativeAd instanceof ApolloNativeAdWrapper;
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
        final ApolloNativeAdWrapper apolloAd) {
        NativeRendererHelper.addTextView(staticNativeViewHolder.titleView,
                                         apolloAd.getTitle());
        NativeRendererHelper.addTextView(staticNativeViewHolder.textView, apolloAd.getText());
        NativeRendererHelper.addTextView(staticNativeViewHolder.callToActionView, apolloAd.getCallToAction());
        NativeImageHelper.loadImageView(apolloAd.getMainImageUrl(),
                                        staticNativeViewHolder.mainImageView);
        NativeImageHelper.loadImageView(apolloAd.getIconImageUrl(),
                                        staticNativeViewHolder.iconImageView);
        apolloAd.registerActionView(staticNativeViewHolder.callToActionView);
        loadMediaView(staticNativeViewHolder.mainView, apolloAd.getMediaData());
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
