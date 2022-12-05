package org.prebid.mobile.api.rendering.customrenderer;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;

public interface AdRenderer {

    View getBannerAdView(
            @NonNull Context context,
            DisplayViewListener listener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse response
    );

    // TODO when default render implements it, it is always null since BaseInterstitialAdUnit is in charge of its init
    InterstitialControllerInterface getInterstitialController(
            Context context,
            InterstitialControllerListener listener
    );
}
