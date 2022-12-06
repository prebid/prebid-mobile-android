package org.prebid.mobile.api.rendering;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import org.prebid.mobile.api.rendering.customrenderer.AdRenderer;
import org.prebid.mobile.api.rendering.customrenderer.InterstitialControllerInterface;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;

public class DefaultRenderer implements AdRenderer {

    @Override
    public View getBannerAdView(@NonNull Context context,
                                DisplayViewListener listener,
                                @NonNull AdUnitConfiguration adUnitConfiguration,
                                @NonNull BidResponse response) {
        return new DefaultDisplayView(context, listener, adUnitConfiguration, response);
    }

    // todo but this is never used, it should be changed
    @Override
    public InterstitialControllerInterface getInterstitialController(Context context, InterstitialControllerListener listener) {
        return null;
    }
}
