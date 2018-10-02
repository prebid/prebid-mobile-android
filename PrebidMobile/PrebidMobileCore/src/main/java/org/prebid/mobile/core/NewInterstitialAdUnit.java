package org.prebid.mobile.core;


import android.support.annotation.NonNull;

public class NewInterstitialAdUnit extends NewAdUnit {
    NewInterstitialAdUnit(@NonNull String configId) {
        super(configId, AdType.INTERSTITIAL);
        this.adType = AdType.INTERSTITIAL;
    }
}
