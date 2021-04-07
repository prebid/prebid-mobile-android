package com.openx.apollo.bidding.interfaces;

import androidx.annotation.Nullable;

import com.openx.apollo.bidding.data.bid.Bid;
import com.openx.apollo.bidding.listeners.InterstitialEventListener;

public class StandaloneInterstitialEventHandler implements InterstitialEventHandler {
    private InterstitialEventListener mInterstitialEventListener;

    @Override
    public void setInterstitialEventListener(InterstitialEventListener interstitialEventListener) {
        mInterstitialEventListener = interstitialEventListener;
    }

    @Override
    public void requestAdWithBid(
        @Nullable
            Bid bid) {
        mInterstitialEventListener.onOXBSdkWin();
    }

    @Override
    public void show() {

    }

    @Override
    public void trackImpression() {

    }

    @Override
    public void destroy() {

    }
}
