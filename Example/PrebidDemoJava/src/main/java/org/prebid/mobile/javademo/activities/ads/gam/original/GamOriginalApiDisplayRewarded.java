package org.prebid.mobile.javademo.activities.ads.gam.original;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import org.prebid.mobile.RewardedDisplayAdUnit;
import org.prebid.mobile.javademo.activities.BaseAdActivity;

public class GamOriginalApiDisplayRewarded extends BaseAdActivity {

    private static final String AD_UNIT_ID = "/21808260008/prebid-demo-app-original-api-display-interstitial";
    private static final String CONFIG_ID = "prebid-demo-banner-rewarded-time";

    private RewardedDisplayAdUnit adUnit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createAd();
    }

    private void createAd() {
        adUnit = new RewardedDisplayAdUnit(CONFIG_ID);

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
                rewardedAd.show(GamOriginalApiDisplayRewarded.this, rewardItem ->
                        Log.d("GamOriginalRewarded", "User earned reward.")
                );
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
            adUnit.destroy();
        }
    }
}
