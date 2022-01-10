package com.mopub.mediation;

import android.util.Log;
import android.view.View;
import com.mopub.mobileads.MoPubView;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.utils.helpers.VisibilityChecker;

public class MoPubBannerMediationUtils extends MoPubBaseMediationUtils {

    private static final String TAG = "MoPubBannerMediation";

    public MoPubBannerMediationUtils(MoPubView adView) {
        super(adView);
    }

    @Override
    public boolean canPerformRefresh() {
        boolean isVisible = true;
        if (adObject instanceof View) {
            final VisibilityTrackerOption visibilityTrackerOption = new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION);
            final VisibilityChecker checker = new VisibilityChecker(visibilityTrackerOption);
            isVisible = checker.isVisibleForRefresh((View) adObject);
            if (isVisible) {
                Log.d(TAG, "Visibility checker result: " + true);
            } else {
                Log.e(TAG, "Can't perform refresh. Ad view is not visible.");
            }
        }
        return isVisible;
    }

}
