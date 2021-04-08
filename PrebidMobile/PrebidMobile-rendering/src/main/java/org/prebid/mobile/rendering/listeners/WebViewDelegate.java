package org.prebid.mobile.rendering.listeners;

import org.prebid.mobile.rendering.errors.AdException;

public interface WebViewDelegate {

    void webViewReadyToDisplay();

    void webViewFailedToLoad(AdException error);

    void webViewShouldOpenExternalLink(String url);

    void webViewShouldOpenMRAIDLink(String url);
}
