package com.mopub.mediation;

import android.util.Log;
import androidx.annotation.Nullable;
import com.mopub.mobileads.MoPubView;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.utils.helpers.VisibilityChecker;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class MoPubBannerMediationUtils extends MoPubBaseMediationUtils {

    private static final String TAG = "MoPubBannerMediation";
    private final WeakReference<MoPubView> adViewReference;

    public MoPubBannerMediationUtils(MoPubView adView) {
        this.adViewReference = new WeakReference<>(adView);
    }

    @Override
    public void setResponseToLocalExtras(@Nullable BidResponse response) {
        setResponseToLocalExtras(response, localExtras -> {
            MoPubView adView = adViewReference.get();
            if (isAdViewNull(adView)) {
                return;
            }
            adView.setLocalExtras(localExtras);
        });
    }

    @Override
    public void handleKeywordsUpdate(@Nullable HashMap<String, String> keywords) {
        handleKeywordsUpdate(keywords, new KeywordsManager() {
            @Override
            public void setKeywords(String keywords) {
                MoPubView adView = adViewReference.get();
                if (isAdViewNull(adView)) {
                    return;
                }
                adView.setKeywords(keywords);
            }

            @Override
            public String getKeywords() {
                MoPubView adView = adViewReference.get();
                if (isAdViewNull(adView)) {
                    return "";
                }
                return adView.getKeywords();
            }
        });
    }

    @Override
    public boolean canPerformRefresh() {
        MoPubView adView = adViewReference.get();
        if (isAdViewNull(adView)) {
            return false;
        }

        final VisibilityTrackerOption visibilityTrackerOption = new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION);
        final VisibilityChecker checker = new VisibilityChecker(visibilityTrackerOption);

        boolean isVisible = checker.isVisibleForRefresh(adView);
        if (isVisible) {
            Log.d(TAG, "Visibility checker result: " + true);
        } else {
            Log.e(TAG, "Can't perform refresh. Ad view is not visible.");
        }
        return isVisible;
    }

}
