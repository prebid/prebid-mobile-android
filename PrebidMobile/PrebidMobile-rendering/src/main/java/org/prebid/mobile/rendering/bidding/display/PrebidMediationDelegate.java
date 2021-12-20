package org.prebid.mobile.rendering.bidding.display;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.utils.broadcast.ScreenStateReceiver;
import org.prebid.mobile.rendering.utils.helpers.VisibilityChecker;

import java.util.HashMap;

/**
 * PrebidMediationDelegate is a delegate of custom mediation platform.
 */
public interface PrebidMediationDelegate {

    /**
     * Returns true if a given object is Banner class
     */
    public boolean isBannerView(@Nullable Object adView);

    /**
     * Returns true if a given object is Interstitial class
     */
    public boolean isInterstitialView(@Nullable Object adView);

    /**
     * Returns true if a given object is Native class
     */
    public boolean isNativeView(@Nullable Object adView);

    /**
     * Sets keywords into a given mediation ad object
     */
    public void handleKeywordsUpdate(@Nullable Object adView, @Nullable HashMap<String, String> keywords);

    /**
     * Sets response into a given mediation ad object
     */
    public void setResponseToLocalExtras(@Nullable Object adView, @Nullable BidResponse response);

    /**
     * Sets response id into a given mediation ad object
     */
    public void setResponseIdToLocalExtras(@Nullable Object adView, @Nullable BidResponse response);

    /**
     * Checks if a given mediation ad object supported and visible for auto refresh
     */
    public boolean canPerformRefresh(
            @Nullable Object adView,
            @NonNull VisibilityChecker visibilityChecker,
            @NonNull ScreenStateReceiver screenStateReceiver,
            boolean isAdFailed
    );

}
