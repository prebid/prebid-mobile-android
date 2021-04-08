package org.prebid.mobile.rendering.listeners;

import android.view.ViewGroup;

import org.prebid.mobile.rendering.models.AbstractCreative;

public interface CreativeViewListener {
    /**
     * Is called when creative finish its lifecycle.
     *
     * @param creative
     */
    void creativeDidComplete(AbstractCreative creative);

    /**
     * Is called when ad's content is rendered in the displayed view.
     * @param creative
     * @param url
     */
    void creativeWasClicked(AbstractCreative creative, String url);

    /**
     * Is called when user closes the creatives clickthrough.
     * @param creative
     */
    void creativeInterstitialDidClose(AbstractCreative creative);

    void creativeDidExpand(AbstractCreative creative);  // MRAID banner only

    void creativeDidCollapse(AbstractCreative creative);    // MRAID banner only

    void creativeInterstitialDialogShown(ViewGroup rootViewGroup);

    void creativeMuted(AbstractCreative creative);

    void creativeUnMuted(AbstractCreative creative);

    void creativePaused(AbstractCreative creative);

    void creativeResumed(AbstractCreative creative);
}
