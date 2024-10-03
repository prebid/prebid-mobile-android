package org.prebid.mobile.api.mediation;

import android.content.Context;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;

import org.prebid.mobile.AdSize;
import org.prebid.mobile.api.data.Position;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;

/**
 * Internal base interstitial ad unit.
 */
public abstract class MediationBaseFullScreenAdUnit extends MediationBaseAdUnit {

    private static final String TAG = MediationBaseFullScreenAdUnit.class.getSimpleName();

    protected MediationBaseFullScreenAdUnit(
        Context context,
        String configId,
        AdSize adSize,
        PrebidMediationDelegate mediationDelegate
    ) {
        super(context, configId, adSize, mediationDelegate);
    }

    /**
     * Sets max video duration. If the ad from server is bigger, it will be rejected.
     */
    public void setMaxVideoDuration(int seconds) {
        adUnitConfig.setMaxVideoDuration(seconds);
    }

    /**
     * Sets delay in seconds to show skip or close button.
     */
    public void setSkipDelay(int secondsDelay) {
        adUnitConfig.setSkipDelay(secondsDelay);
    }

    /**
     * Sets skip button percentage size in range from 0.05 to 1.
     * If value less than 0.05, size will be default.
     */
    public void setSkipButtonArea(@FloatRange(from = 0, to = 1.0) double buttonArea) {
        adUnitConfig.setSkipButtonArea(buttonArea);
    }

    /**
     * Sets skip button position on the screen. Suitable values TOP_LEFT and TOP_RIGHT.
     * Default value TOP_RIGHT.
     */
    public void setSkipButtonPosition(Position skipButtonPosition) {
        adUnitConfig.setSkipButtonPosition(skipButtonPosition);
    }

    /**
     * Sets close button percentage size in range from 0.05 to 1.
     * If value less than 0.05, size will be default.
     */
    public void setCloseButtonArea(@FloatRange(from = 0, to = 1.0) double closeButtonArea) {
        adUnitConfig.setCloseButtonArea(closeButtonArea);
    }

    /**
     * Sets close button position on the screen. Suitable values TOP_LEFT and TOP_RIGHT.
     * Default value TOP_RIGHT.
     */
    public void setCloseButtonPosition(@Nullable Position closeButtonPosition) {
        adUnitConfig.setCloseButtonPosition(closeButtonPosition);
    }

    /**
     * Sets desired is muted property.
     */
    public void setIsMuted(boolean isMuted) {
        adUnitConfig.setIsMuted(isMuted);
    }

    /**
     * Makes sound button visible.
     */
    public void setIsSoundButtonVisible(boolean isSoundButtonVisible) {
        adUnitConfig.setIsSoundButtonVisible(isSoundButtonVisible);
    }

}
