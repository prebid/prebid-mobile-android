package com.openx.apollo.bidding.listeners;

import android.view.View;

import com.openx.apollo.errors.AdException;

public interface BannerEventListener {
    void onOXBSdkWin();

    void onAdServerWin(View view);

    void onAdFailed(AdException exception);

    void onAdClicked();

    void onAdClosed();
}
