package com.applovin.mediation.adapters.prebid;

import android.os.Bundle;
import android.view.View;
import com.applovin.mediation.adapter.listeners.MaxNativeAdAdapterListener;
import com.applovin.mediation.nativeAds.MaxNativeAd;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.PrebidNativeAdEventListener;

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
        prebidNativeAd.registerViewList(maxView, views, new PrebidNativeAdEventListener() {
            @Override
            public void onAdClicked() {
                maxListener.onNativeAdClicked();
            }

            @Override
            public void onAdImpression() {
                maxListener.onNativeAdDisplayed(new Bundle());
            }

            @Override
            public void onAdExpired() {}
        });
    }

}
