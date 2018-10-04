package org.prebid.mobile;


import android.support.annotation.NonNull;

public class InterstitialAdUnit extends AdUnit {
    InterstitialAdUnit(@NonNull String configId) {
        super(configId, AdType.INTERSTITIAL);
    }
}
