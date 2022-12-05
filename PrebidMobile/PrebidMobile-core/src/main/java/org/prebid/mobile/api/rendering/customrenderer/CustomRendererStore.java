package org.prebid.mobile.api.rendering.customrenderer;

import java.util.HashMap;

public class CustomRendererStore {

    private static CustomRendererStore instance = new CustomRendererStore();

    public HashMap<String, AdRenderer> customRenderers = new HashMap<>();

    private CustomRendererStore() {
    }

    public static CustomRendererStore getInstance() {
        return instance;
    }
}
