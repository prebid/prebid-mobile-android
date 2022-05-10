package org.prebid.mobile.javademo.ads.gam;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import org.prebid.mobile.RewardedVideoAdUnit;
import org.prebid.mobile.Signals;
import org.prebid.mobile.VideoBaseAdUnit;

import java.util.Collections;

public class GamRewarded {

    private static final String TAG = GamRewarded.class.getSimpleName();

    private static RewardedVideoAdUnit adUnit;

    public static void create(
        Activity activity,
        String adUnitId,
        String configId
    ) {
        VideoBaseAdUnit.Parameters parameters = new VideoBaseAdUnit.Parameters();
        parameters.setMimes(Collections.singletonList("video/mp4"));
        parameters.setProtocols(Collections.singletonList(Signals.Protocols.VAST_2_0));
        parameters.setPlaybackMethod(Collections.singletonList(Signals.PlaybackMethod.AutoPlaySoundOff));

        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();
        adUnit = new RewardedVideoAdUnit(configId);
        adUnit.setParameters(parameters);
        adUnit.fetchDemand(builder, resultCode -> {
            AdManagerAdRequest request = builder.build();
            RewardedAd.load(activity, adUnitId, request, createListener(activity));
        });
    }

    public static void destroy() {
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
            adUnit = null;
        }
    }

    private static RewardedAdLoadCallback createListener(Activity activity) {
        return new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                rewardedAd.show(activity, rewardItem -> {
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e(TAG, loadAdError.getMessage());
            }
        };
    }

}
