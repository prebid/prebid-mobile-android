package org.prebid.mobile.rendering.bidding.display;

import android.view.View;
import androidx.annotation.Nullable;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.HashMap;

/**
 * PrebidMediationDelegate is a delegate of custom mediation platform.
 */
public interface PrebidMediationDelegate {

    /**
     * Sets keywords into a given mediation ad object
     */
    public void handleKeywordsUpdate(@Nullable HashMap<String, String> keywords);

    /**
     * Sets response into a given mediation ad object
     */
    public void setResponseToLocalExtras(@Nullable BidResponse response);

    /**
     * Checks if banner view is visible, and it is possible to make refresh.
     */
    public boolean canPerformRefresh();

    /**
     * Returns the mediation SDK's ad view this delegate was created with, or null when the
     * delegate is not bound to a view or the view has already been released.
     * <p>
     * A banner ad unit reads the Prebid creative's playback state off this view, so a delegate
     * that returns null opts out of the "do not refresh mid video playback" gate.
     */
    @Nullable
    default View getAdView() {
        return null;
    }

}
