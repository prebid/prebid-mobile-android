package com.mopub.nativeads;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.PrebidNativeAdEventListener;

import java.util.ArrayList;

public class PrebidNativeAdWrapper extends BaseNativeAd {

    private final PrebidNativeAd nativeAd;
    private PrebidNativeAdEventListener prebidListener;

    public PrebidNativeAdWrapper(PrebidNativeAd nativeAd) {
        this.nativeAd = nativeAd;
    }

    @Override
    public void prepare(@NonNull View view) {
        if (view instanceof ViewGroup && ((ViewGroup) view).getChildCount() > 0) {
            ViewGroup viewGroup = (ViewGroup) view;
            int size = viewGroup.getChildCount();
            ArrayList<View> children = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                View child = viewGroup.getChildAt(i);
                children.add(child);
            }
            nativeAd.registerViewList(view, children, prebidListener);
            return;
        }
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
