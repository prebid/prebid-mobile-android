package com.openx.apollo.listeners;

public interface OnBrowserActionResultListener {
    void onSuccess(BrowserActionResult browserActionResult);

    enum BrowserActionResult {
        INTERNAL_BROWSER, EXTERNAL_BROWSER
    }
}
