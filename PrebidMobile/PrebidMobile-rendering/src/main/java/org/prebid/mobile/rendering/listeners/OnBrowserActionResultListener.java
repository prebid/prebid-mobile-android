package org.prebid.mobile.rendering.listeners;

public interface OnBrowserActionResultListener {
    void onSuccess(BrowserActionResult browserActionResult);

    enum BrowserActionResult {
        INTERNAL_BROWSER, EXTERNAL_BROWSER
    }
}
