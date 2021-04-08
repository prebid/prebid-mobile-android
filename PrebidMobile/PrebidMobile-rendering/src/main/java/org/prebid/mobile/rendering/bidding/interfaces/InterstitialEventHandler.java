package org.prebid.mobile.rendering.bidding.interfaces;

import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.listeners.InterstitialEventListener;

public interface InterstitialEventHandler {
    void setInterstitialEventListener(InterstitialEventListener interstitialEventListener);

    void requestAdWithBid(@Nullable Bid bid);

    void show();

    void trackImpression();

    void destroy();
}
