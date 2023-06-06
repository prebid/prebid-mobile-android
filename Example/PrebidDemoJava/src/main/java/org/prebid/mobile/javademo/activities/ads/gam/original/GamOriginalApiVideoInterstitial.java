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
import org.prebid.mobile.Signals;
import org.prebid.mobile.VideoParameters;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.javademo.activities.BaseAdActivity;
import org.prebid.mobile.javademo.utils.Settings;

import java.util.Collections;
import java.util.EnumSet;

public class GamOriginalApiVideoInterstitial extends BaseAdActivity {

    private static final String AD_UNIT_ID = "/21808260008/prebid-demo-app-original-api-video-interstitial";
    private static final String CONFIG_ID = "prebid-ita-video-interstitial-320-480-original-api";

    private InterstitialAdUnit adUnit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createAd();
    }

    private void createAd() {
        adUnit = new InterstitialAdUnit(CONFIG_ID, EnumSet.of(AdUnitFormat.VIDEO));
        adUnit.setAutoRefreshInterval(Settings.get().getRefreshTimeSeconds());

        VideoParameters parameters = new VideoParameters(Collections.singletonList("video/mp4"));
        parameters.setProtocols(Collections.singletonList(Signals.Protocols.VAST_2_0));
        parameters.setPlaybackMethod(Collections.singletonList(Signals.PlaybackMethod.AutoPlaySoundOff));
        adUnit.setVideoParameters(parameters);

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
                interstitialManager.show(GamOriginalApiVideoInterstitial.this);
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
