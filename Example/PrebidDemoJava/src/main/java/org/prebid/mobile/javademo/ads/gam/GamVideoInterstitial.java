package org.prebid.mobile.javademo.ads.gam;

import android.app.Activity;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import org.prebid.mobile.Signals;
import org.prebid.mobile.VideoBaseAdUnit;
import org.prebid.mobile.VideoInterstitialAdUnit;

import java.util.Collections;

public class GamVideoInterstitial {

    private static VideoInterstitialAdUnit adUnit;

    public static void create(
        Activity activity,
        String adUnitId,
        String configId,
        int autoRefreshTime
    ) {
        VideoBaseAdUnit.Parameters parameters = new VideoBaseAdUnit.Parameters();
        parameters.setMimes(Collections.singletonList("video/mp4"));
        parameters.setProtocols(Collections.singletonList(Signals.Protocols.VAST_2_0));
        parameters.setPlaybackMethod(Collections.singletonList(Signals.PlaybackMethod.AutoPlaySoundOff));

        adUnit = new VideoInterstitialAdUnit(configId);
        adUnit.setParameters(parameters);
        adUnit.setAutoRefreshInterval(autoRefreshTime);

        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();

        adUnit.fetchDemand(builder, resultCode -> {
            AdManagerAdRequest request = builder.build();

            AdManagerInterstitialAd.load(activity, adUnitId, request, createListener(activity));
        });
    }

    public static void destroy() {
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
            adUnit = null;
        }
    }

    private static AdManagerInterstitialAdLoadCallback createListener(Activity activity) {
        return new AdManagerInterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AdManagerInterstitialAd adManagerInterstitialAd) {
                adManagerInterstitialAd.show(activity);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                showDialog(activity, loadAdError.getMessage());
            }
        };
    }

    private static void showDialog(
        Activity activity,
        String message
    ) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        builder.setTitle("Failed to load AdManager interstitial ad")
            .setMessage("Error: " + message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

}
