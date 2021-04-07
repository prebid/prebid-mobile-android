package com.openx.apollo.views.webview;

public class MraidEventsManager {

    public interface MraidListener {
        void openExternalLink(String url);

        void openMraidExternalLink(String url);

        void onAdWebViewWindowFocusChanged(boolean hasFocus);

        void loadMraidExpandProperties();
    }
}
