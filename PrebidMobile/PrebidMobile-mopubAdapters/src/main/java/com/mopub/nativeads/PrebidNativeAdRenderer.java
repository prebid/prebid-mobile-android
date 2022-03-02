package com.mopub.nativeads;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.PrebidNativeAdEventListener;

import java.util.WeakHashMap;

public class PrebidNativeAdRenderer implements MoPubAdRenderer<PrebidNativeAdWrapper> {

    private final ViewBinder viewBinder;
    private final WeakHashMap<View, StaticNativeViewHolder> viewHolderMap;
    private final PrebidNativeAdEventListener listener;

    public PrebidNativeAdRenderer(
            @NonNull ViewBinder viewBinder,
            @Nullable PrebidNativeAdEventListener listener
    ) {
        this.viewBinder = viewBinder;
        this.viewHolderMap = new WeakHashMap<>();
        this.listener = listener;
    }

    @Override
    public boolean supports(@NonNull BaseNativeAd baseNativeAd) {
        if (baseNativeAd instanceof PrebidNativeAdWrapper) {
            PrebidNativeAdWrapper adWrapper = (PrebidNativeAdWrapper) baseNativeAd;
            adWrapper.setListener(listener);
        }
        return baseNativeAd instanceof PrebidNativeAdWrapper;
    }

    @NonNull
    @Override
    public View createAdView(
            @NonNull Context context,
            @Nullable ViewGroup parent
    ) {
        int layoutId = viewBinder.layoutId;
        return LayoutInflater
                .from(context)
                .inflate(layoutId, parent, false);
    }

    @Override
    public void renderAdView(
            @NonNull View view,
            @NonNull PrebidNativeAdWrapper adWrapper
    ) {
        StaticNativeViewHolder viewHolder = viewHolderMap.get(view);
        if (viewHolder == null) {
            viewHolder = StaticNativeViewHolder.fromViewBinder(view, viewBinder);
            viewHolderMap.put(view, viewHolder);
        }

        update(viewHolder, adWrapper);
        makeViewVisible(viewHolder);
    }


    private void update(
            @NonNull final StaticNativeViewHolder viewHolder,
            @NonNull final PrebidNativeAdWrapper adWrapper
    ) {
        NativeRendererHelper.addTextView(viewHolder.titleView, adWrapper.getTitle());
        NativeRendererHelper.addTextView(viewHolder.textView, adWrapper.getText());
        NativeRendererHelper.addTextView(viewHolder.callToActionView, adWrapper.getCallToAction());
        NativeImageHelper.loadImageView(adWrapper.getMainImageUrl(), viewHolder.mainImageView);
        NativeImageHelper.loadImageView(adWrapper.getIconImageUrl(), viewHolder.iconImageView);
        adWrapper.registerActionView(viewHolder.callToActionView);
    }

    private void makeViewVisible(
            @NonNull final StaticNativeViewHolder viewHolder
    ) {
        if (viewHolder.mainView != null) {
            viewHolder.mainView.setVisibility(View.VISIBLE);
        }
    }
}
