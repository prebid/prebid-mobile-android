package org.prebid.mobile.rendering.views.webview;

public class ActionUrl {

    public String url;
    Runnable action;

    public ActionUrl(String url, Runnable action) {
        this.url = url;
        this.action = action;
    }

}
