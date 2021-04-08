package org.prebid.mobile.rendering.bidding.listeners;

import android.view.View;

import org.prebid.mobile.rendering.errors.AdException;

public interface BannerEventListener {
    void onOXBSdkWin();

    void onAdServerWin(View view);

    void onAdFailed(AdException exception);

    void onAdClicked();

    void onAdClosed();
}
