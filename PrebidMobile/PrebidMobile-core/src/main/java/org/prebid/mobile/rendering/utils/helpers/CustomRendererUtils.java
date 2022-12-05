package org.prebid.mobile.rendering.utils.helpers;

import androidx.annotation.Nullable;

import org.prebid.mobile.api.rendering.customrenderer.AdRenderer;
import org.prebid.mobile.api.rendering.customrenderer.CustomRendererStore;

import java.lang.reflect.Constructor;
import java.util.List;

public class CustomRendererUtils {

    @Nullable
    public static AdRenderer retrieveCustomRendererBySingleton(List<String> rendererPackageNames) {
        for (String packageRenderer : rendererPackageNames) {
            AdRenderer renderer = CustomRendererStore.getInstance().customRenderers.get(packageRenderer);
            if (renderer != null) {
                return renderer;
            }
        }
        return null;
    }

    @Nullable
    public static AdRenderer retrieveCustomRendererByReflexivity(List<String> rendererPackageNames) {
        for (String packageRenderer : rendererPackageNames) {
            try {
                Class<?> c = Class.forName(packageRenderer);
                Constructor<?> constructor = c.getConstructor();
                Object testObject = constructor.newInstance();
                return (AdRenderer) testObject;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
