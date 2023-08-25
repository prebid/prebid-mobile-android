package com.applovin.mediation.adapters.prebid;

import android.os.Bundle;
import android.view.View;

import com.applovin.mediation.adapter.listeners.MaxNativeAdAdapterListener;
import com.applovin.mediation.nativeAds.MaxNativeAd;
import com.applovin.mediation.nativeAds.MaxNativeAdView;

import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.PrebidNativeAdEventListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

public class PrebidMaxNativeAd extends MaxNativeAd {

    private final PrebidNativeAd prebidNativeAd;
    private final MaxNativeAdAdapterListener maxListener;

    public PrebidMaxNativeAd(
            Builder builder,
            PrebidNativeAd prebidNativeAd,
            MaxNativeAdAdapterListener maxListener
    ) {
        super(builder);
        this.prebidNativeAd = prebidNativeAd;
        this.maxListener = maxListener;
    }

    /**
     * Makes all views clickable.
     */
    @Override
    public void prepareViewForInteraction(MaxNativeAdView maxView) {
        super.prepareViewForInteraction(maxView);

        ArrayList<View> views = new ArrayList<>(7);
        views.addAll(Arrays.asList(
                maxView.getAdvertiserTextView(),
                maxView.getBodyTextView(),
                maxView.getTitleTextView(),
                maxView.getMainView(),
                maxView.getIconImageView(),
                maxView.getCallToActionButton(),
                maxView.getMediaContentViewGroup()
        ));
        prebidNativeAd.registerView(maxView, views, new SafeNativeListener(maxListener));
    }

    private static class SafeNativeListener implements PrebidNativeAdEventListener {

        private final WeakReference<MaxNativeAdAdapterListener> listenerReference;

        public SafeNativeListener(MaxNativeAdAdapterListener maxListener) {
            this.listenerReference = new WeakReference(maxListener);
        }

        @Override
        public void onAdClicked() {
            MaxNativeAdAdapterListener listener = listenerReference.get();
            if (listener != null) {
                listener.onNativeAdClicked();
            }
        }

        @Override
        public void onAdImpression() {
            MaxNativeAdAdapterListener listener = listenerReference.get();
            if (listener != null) {
                listener.onNativeAdDisplayed(new Bundle());
            }
        }

        @Override
        public void onAdExpired() {
        }

    }

}
