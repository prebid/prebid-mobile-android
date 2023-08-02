package org.prebid.mobile.javademo.activities.ads.gam.original;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;

import org.prebid.mobile.InterstitialAdUnit;
import org.prebid.mobile.VideoParameters;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.javademo.activities.BaseAdActivity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;

public class GamOriginalApiMultiformatInterstitial extends BaseAdActivity {

    private static final String AD_UNIT_ID = "/21808260008/prebid-demo-intestitial-multiformat";
    private static final String CONFIG_ID_BANNER = "prebid-ita-display-interstitial-320-480";
    private static final String CONFIG_ID_VIDEO = "prebid-ita-video-interstitial-320-480-original-api";

    private InterstitialAdUnit adUnit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createAd();
    }

    private void createAd() {
        String configId;
        if (new Random().nextBoolean()) {
            configId = CONFIG_ID_BANNER;
        } else {
            configId = CONFIG_ID_VIDEO;
        }
        adUnit = new InterstitialAdUnit(configId, EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO));
        adUnit.setVideoParameters(new VideoParameters(Collections.singletonList("video/mp4")));
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
                interstitialManager.show(GamOriginalApiMultiformatInterstitial.this);
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
