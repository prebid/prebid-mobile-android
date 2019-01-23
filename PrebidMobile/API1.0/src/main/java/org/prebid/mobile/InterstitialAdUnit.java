package org.prebid.mobile;


import android.support.annotation.NonNull;

public class InterstitialAdUnit extends AdUnit {
    public InterstitialAdUnit(@NonNull String configId) {
        super(configId, AdType.INTERSTITIAL);
    }
}
