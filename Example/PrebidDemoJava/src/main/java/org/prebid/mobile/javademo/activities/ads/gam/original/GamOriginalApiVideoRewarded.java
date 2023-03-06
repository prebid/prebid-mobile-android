package org.prebid.mobile.javademo.activities.ads.gam.original;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import org.prebid.mobile.RewardedVideoAdUnit;
import org.prebid.mobile.Signals;
import org.prebid.mobile.VideoBaseAdUnit;
import org.prebid.mobile.javademo.activities.BaseAdActivity;

import java.util.Collections;

public class GamOriginalApiVideoRewarded extends BaseAdActivity {

    private static final String AD_UNIT_ID = "/21808260008/prebid-demo-app-original-api-video-interstitial";
    private static final String CONFIG_ID = "imp-prebid-video-rewarded-320-480-original-api";

    private RewardedVideoAdUnit adUnit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createAd();
    }

    private void createAd() {
        adUnit = new RewardedVideoAdUnit(CONFIG_ID);

        VideoBaseAdUnit.Parameters parameters = new VideoBaseAdUnit.Parameters();
        parameters.setMimes(Collections.singletonList("video/mp4"));
        parameters.setProtocols(Collections.singletonList(Signals.Protocols.VAST_2_0));
        parameters.setPlaybackMethod(Collections.singletonList(Signals.PlaybackMethod.AutoPlaySoundOff));
        adUnit.setParameters(parameters);

        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();
        adUnit.fetchDemand(builder, resultCode -> {
            AdManagerAdRequest request = builder.build();
            RewardedAd.load(this, AD_UNIT_ID, request, createListener());
        });
    }

    private RewardedAdLoadCallback createListener() {
        return new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                rewardedAd.show(GamOriginalApiVideoRewarded.this, rewardItem -> {
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e("GamOriginalRewarded", loadAdError.getMessage());
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
