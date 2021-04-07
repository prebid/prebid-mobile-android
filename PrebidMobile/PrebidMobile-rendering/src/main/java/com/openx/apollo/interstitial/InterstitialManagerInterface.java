package com.openx.apollo.interstitial;

import android.view.View;
import android.view.ViewGroup;

public interface InterstitialManagerInterface {
    void interstitialAdClosed();

    void interstitialClosed(View view);

    void interstitialDialogShown(ViewGroup rootViewGroup);
}