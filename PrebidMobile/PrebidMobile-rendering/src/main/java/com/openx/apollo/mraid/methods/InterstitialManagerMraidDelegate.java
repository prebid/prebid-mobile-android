package com.openx.apollo.mraid.methods;

import android.view.View;

import com.openx.apollo.models.internal.MraidEvent;
import com.openx.apollo.views.webview.WebViewBase;

public interface InterstitialManagerMraidDelegate {
    boolean collapseMraid();

    void closeThroughJs(WebViewBase viewToClose);

    void displayOpenXWebViewForMRAID(final WebViewBase adBaseView,
                                     final boolean isNewlyLoaded,
                                     MraidEvent mraidEvent);

    void displayViewInInterstitial(final View adBaseView,
                                   boolean addOldViewToBackStack,
                                   final MraidEvent expandUrl,
                                   final MraidController.DisplayCompletionListener displayCompletionListener);

    void destroyMraidExpand();
}
