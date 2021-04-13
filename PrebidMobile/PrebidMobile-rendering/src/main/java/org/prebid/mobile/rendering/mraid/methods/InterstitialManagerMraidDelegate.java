package org.prebid.mobile.rendering.mraid.methods;

import android.view.View;

import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.views.webview.WebViewBase;

public interface InterstitialManagerMraidDelegate {
    boolean collapseMraid();

    void closeThroughJs(WebViewBase viewToClose);

    void displayPrebidWebViewForMraid(final WebViewBase adBaseView,
                                      final boolean isNewlyLoaded,
                                      MraidEvent mraidEvent);

    void displayViewInInterstitial(final View adBaseView,
                                   boolean addOldViewToBackStack,
                                   final MraidEvent expandUrl,
                                   final MraidController.DisplayCompletionListener displayCompletionListener);

    void destroyMraidExpand();
}
