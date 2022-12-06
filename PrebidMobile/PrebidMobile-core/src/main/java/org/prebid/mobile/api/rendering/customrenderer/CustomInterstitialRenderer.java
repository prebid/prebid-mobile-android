package org.prebid.mobile.api.rendering.customrenderer;

import android.content.Context;

import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;

public interface CustomInterstitialRenderer extends BaseCustomRenderer {

    InterstitialControllerInterface getInterstitialController(
            Context context,
            InterstitialControllerListener listener
    );
}
