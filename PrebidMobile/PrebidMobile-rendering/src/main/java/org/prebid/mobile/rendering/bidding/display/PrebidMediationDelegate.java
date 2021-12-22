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
     * Sets keywords into a given mediation ad object
     */
    public void handleKeywordsUpdate(@Nullable HashMap<String, String> keywords);

    /**
     * Sets response into a given mediation ad object
     */
    public void setResponseToLocalExtras(@Nullable BidResponse response);

    /**
     * Returns ad object of current mediation delegate.
     */
    public Object getAdObject();

}
