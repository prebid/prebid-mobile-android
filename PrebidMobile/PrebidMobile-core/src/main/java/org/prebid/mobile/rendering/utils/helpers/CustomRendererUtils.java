package org.prebid.mobile.rendering.utils.helpers;

import androidx.annotation.Nullable;

import org.prebid.mobile.api.rendering.customrenderer.CustomBannerRenderer;
import org.prebid.mobile.api.rendering.customrenderer.CustomInterstitialRenderer;
import org.prebid.mobile.api.rendering.customrenderer.CustomRendererStore;

import java.util.List;

public class CustomRendererUtils {

    @Nullable
    public static CustomBannerRenderer getBannerRendererBySingleton(List<String> renderers) {
        for (String rendererKey : renderers) {
            CustomBannerRenderer renderer = CustomRendererStore.getInstance().customBannerRenderers.get(rendererKey);
            if (renderer != null) {
                return renderer;
            }
        }
        return null;
    }

    @Nullable
    public static CustomInterstitialRenderer getInterstitialRendererBySingleton(List<String> renderers) {
        for (String rendererKey : renderers) {
            CustomInterstitialRenderer renderer = CustomRendererStore.getInstance().customInterstitialRenderers.get(rendererKey);
            if (renderer != null) {
                return renderer;
            }
        }
        return null;
    }
}
