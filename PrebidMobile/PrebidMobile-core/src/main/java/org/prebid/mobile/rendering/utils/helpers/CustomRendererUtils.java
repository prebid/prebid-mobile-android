/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
