package com.mopub.nativeads;

import android.view.View;
import androidx.annotation.NonNull;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.PrebidNativeAdEventListener;

public class PrebidNativeAdWrapper extends BaseNativeAd {

    private PrebidNativeAd nativeAd;
    private PrebidNativeAdEventListener prebidListener;

    public PrebidNativeAdWrapper(PrebidNativeAd nativeAd) {
        this.nativeAd = nativeAd;
    }

    @Override
    public void prepare(@NonNull View view) {
        nativeAd.registerView(view, prebidListener);
    }

    @Override
    public void clear(@NonNull View view) {

    }

    @Override
    public void destroy() {

    }

    public void registerActionView(View view) {
        nativeAd.registerView(view, prebidListener);
    }

    public String getTitle() {
        return nativeAd.getTitle();
    }

    public String getText() {
        return nativeAd.getDescription();
    }

    public String getMainImageUrl() {
        return nativeAd.getImageUrl();
    }

    public String getIconImageUrl() {
        return nativeAd.getIconUrl();
    }

    public String getCallToAction() {
        return nativeAd.getCallToAction();
    }

    public void setListener(PrebidNativeAdEventListener listener) {
        prebidListener = listener;
    }

}
