package org.prebid.mobile.admob;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.ads.AdView;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;
import org.prebid.mobile.rendering.utils.broadcast.ScreenStateReceiver;
import org.prebid.mobile.rendering.utils.helpers.VisibilityChecker;

import java.util.HashMap;

public class PrebidAdMobMediationDelegate implements PrebidMediationDelegate {

    @Override
    public boolean isBannerView(@Nullable Object adView) {
        return true;
    }

    @Override
    public boolean isInterstitialView(@Nullable Object adView) {
        return false;
    }

    @Override
    public boolean isNativeView(@Nullable Object adView) {
        return false;
    }

    @Override
    public void handleKeywordsUpdate(@Nullable Object adView, @Nullable HashMap<String, String> keywords) {

    }

    @Override
    public void setResponseToLocalExtras(@Nullable Object adView, @Nullable BidResponse response) {

    }

    @Override
    public void setResponseIdToLocalExtras(@Nullable Object adView, @Nullable BidResponse response) {
        Log.e("PrebidAds", "setResponseIdToLocalExtras()");
        if (response != null) {
            if (adView instanceof PrebidAdMobRequest) {
                ((PrebidAdMobRequest) adView).setResponseId(response.getId());
                Log.e("PrebidAds", "setResponseId");
            }
        }
    }

    @Override
    public boolean canPerformRefresh(@Nullable Object adView, @NonNull VisibilityChecker visibilityChecker, @NonNull ScreenStateReceiver screenStateReceiver, boolean isAdFailed) {
        return adView instanceof PrebidAdMobRequest;
    }
}
