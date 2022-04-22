package org.prebid.mobile.javademo.ads.gam;

import android.app.Activity;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import org.prebid.mobile.AdUnit;
import org.prebid.mobile.InterstitialAdUnit;

public class GamInterstitial {

    public static AdUnit adUnit;

    public static void create(
        Activity activity,
        String adUnitId,
        String configId,
        int autoRefreshTime
    ) {
        adUnit = new InterstitialAdUnit(configId);
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
            public void onAdLoaded(@NonNull AdManagerInterstitialAd interstitialManager) {
                interstitialManager.show(activity);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                createDialog(activity, loadAdError.getMessage());
            }
        };
    }

    private static void createDialog(
        Activity activity,
        String message
    ) {
        AlertDialog.Builder builder1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder1 = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder1 = new AlertDialog.Builder(activity);
        }
        builder1.setTitle("Failed to load AdManager interstitial ad")
            .setMessage("Error: " + message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

}
