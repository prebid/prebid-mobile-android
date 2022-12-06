package org.prebid.mobile.api.rendering.customrenderer;

import java.util.HashMap;

public class CustomRendererStore {

    private static final CustomRendererStore instance = new CustomRendererStore();

    public HashMap<String, CustomBannerRenderer> customBannerRenderers = new HashMap<>();
    public HashMap<String, CustomInterstitialRenderer> customInterstitialRenderers = new HashMap<>();

    private CustomRendererStore() {
    }

    public static CustomRendererStore getInstance() {
        return instance;
    }
}
