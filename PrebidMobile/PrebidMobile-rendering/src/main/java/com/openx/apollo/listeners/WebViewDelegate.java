package com.openx.apollo.listeners;

import com.openx.apollo.errors.AdException;

public interface WebViewDelegate {

    void webViewReadyToDisplay();

    void webViewFailedToLoad(AdException error);

    void webViewShouldOpenExternalLink(String url);

    void webViewShouldOpenMRAIDLink(String url);
}
