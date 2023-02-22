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

package org.prebid.mobile.api.rendering.customrenderer;

import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginRegisterCustomRenderer {

    public static final String TAG = "PluginRegisterCustomRenderer";
    public static final String CUSTOM_RENDERER_KEY = "plugin_custom_renderer_key";
    public static final String PREBID_MOBILE_RENDERER_NAME = "PrebidRenderer";

    private static final PluginRegisterCustomRenderer instance = new PluginRegisterCustomRenderer();

    @VisibleForTesting
    public final HashMap<String, PrebidMobilePluginCustomRenderer> plugins = new HashMap<>();

    public void registerPlugin(PrebidMobilePluginCustomRenderer prebidMobilePluginCustomRenderers) {
        String rendererName = prebidMobilePluginCustomRenderers.getName();
        if (plugins.containsKey(rendererName)) {
            LogUtil.debug(TAG, "New custom renderer with name" + rendererName + "will replace the previous one with same name");
        }
        plugins.put(prebidMobilePluginCustomRenderers.getName(), prebidMobilePluginCustomRenderers);
    }

    public void unregisterPlugin(PrebidMobilePluginCustomRenderer prebidMobilePluginCustomRenderer) {
        plugins.remove(prebidMobilePluginCustomRenderer.getName());
    }

    // Returns the list of available renderers for the given ad unit for RT request
    public List<String> getRTBListOfCustomRenderersFor(AdUnitConfiguration adUnitConfiguration) {
        List<String> compliantPlugins = new ArrayList<>();
        for (Map.Entry<String, PrebidMobilePluginCustomRenderer> entry : plugins.entrySet()) {
            PrebidMobilePluginCustomRenderer renderer = entry.getValue();
            if (renderer.isSupportRenderingFor(adUnitConfiguration)) {
                compliantPlugins.add(renderer.getName());
            }
        }
        return compliantPlugins;
    }

    // Returns the registered renderer according to the preferred renderer name in the bid response
    // If no preferred renderer is found, it returns PrebidRenderer to perform default behavior
    public PrebidMobilePluginCustomRenderer getPluginForPreferredRenderer(BidResponse bidResponse) {
        String preferredRendererName = bidResponse.gePreferredCustomRendererName();
        PrebidMobilePluginCustomRenderer preferredPlugin = plugins.get(preferredRendererName);
        if (preferredPlugin != null && preferredPlugin.isSupportRenderingFor(bidResponse.getAdUnitConfiguration())) {
            return preferredPlugin;
        } else {
            return plugins.get(PREBID_MOBILE_RENDERER_NAME);
        }
    }

    private PluginRegisterCustomRenderer() {
    }

    public static PluginRegisterCustomRenderer getInstance() {
        return instance;
    }
}
