package com.openx.apollo.views;

import android.view.View;

import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AdDetails;

public abstract class AdViewManagerListener {
    /**
     * A successful load of an ad
     */
    public void adLoaded(AdDetails adDetails) { }

    /**
     * Attach creativeview to AdView
     *
     * @param creative which is ready for display
     */
    public void viewReadyForImmediateDisplay(View creative) { }

    /**
     * Callback for a failure in loading an ad
     *
     * @param error which occurred while loading
     */
    public void failedToLoad(AdException error) { }

    /**
     * When an ad has finished refreshing.
     */
    public void adCompleted() { }

    /**
     * Handle click of a creative
     */
    public void creativeClicked(String url) { }

    /**
     * Handle close of an interstitial ad
     */
    public void creativeInterstitialClosed() { }

    //mraidAdExpanded
    public void creativeExpanded() { }

    //mraidAdCollapsed
    public void creativeCollapsed() { }

    public void creativeMuted() { }

    public void creativeUnMuted() { }

    public void creativePaused() { }

    public void creativeResumed() { }

    public void videoCreativePlaybackFinished() {}
}
