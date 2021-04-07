package com.openx.apollo.interstitial;

import android.view.ViewGroup;

public interface InterstitialManagerDisplayDelegate {

    /**
     * Close of an interstitial ad
     */
    void interstitialAdClosed();

    void interstitialDialogShown(ViewGroup rootViewGroup);
}