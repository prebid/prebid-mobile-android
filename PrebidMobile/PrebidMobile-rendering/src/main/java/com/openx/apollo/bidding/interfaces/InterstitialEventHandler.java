package com.openx.apollo.bidding.interfaces;

import androidx.annotation.Nullable;

import com.openx.apollo.bidding.data.bid.Bid;
import com.openx.apollo.bidding.listeners.InterstitialEventListener;

public interface InterstitialEventHandler {
    void setInterstitialEventListener(InterstitialEventListener interstitialEventListener);

    void requestAdWithBid(@Nullable Bid bid);

    void show();

    void trackImpression();

    void destroy();
}
