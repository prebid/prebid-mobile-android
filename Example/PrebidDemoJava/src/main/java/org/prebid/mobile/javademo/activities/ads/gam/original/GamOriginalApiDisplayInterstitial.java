package org.prebid.mobile.javademo.activities.ads.gam.original;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.InterstitialAdUnit;
import org.prebid.mobile.javademo.activities.BaseAdActivity;

public class GamOriginalApiDisplayInterstitial extends BaseAdActivity {

    private static final String AD_UNIT_ID = "/21808260008/prebid-demo-app-original-api-display-interstitial";
    private static final String CONFIG_ID = "imp-prebid-display-interstitial-320-480";

    private AdUnit adUnit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createAd();
    }

    private void createAd() {
        adUnit = new InterstitialAdUnit(CONFIG_ID);
        adUnit.setAutoRefreshInterval(getRefreshTimeSeconds());

        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();
        adUnit.fetchDemand(builder, resultCode -> {
            AdManagerAdRequest request = builder.build();
            AdManagerInterstitialAd.load(this, AD_UNIT_ID, request, createListener());
        });
    }

    private AdManagerInterstitialAdLoadCallback createListener() {
        return new AdManagerInterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AdManagerInterstitialAd interstitialManager) {
                interstitialManager.show(GamOriginalApiDisplayInterstitial.this);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e("GamInterstitial", loadAdError.getMessage());
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (adUnit != null) {
            adUnit.stopAutoRefresh();
        }
    }
}
