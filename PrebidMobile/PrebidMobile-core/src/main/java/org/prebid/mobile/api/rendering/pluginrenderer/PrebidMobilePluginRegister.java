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

package org.prebid.mobile.api.rendering.pluginrenderer;

import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrebidMobilePluginRegister {

    public static final String PREBID_MOBILE_RENDERER_NAME = "PrebidRenderer";
    private static final String TAG = PrebidMobilePluginRegister.class.getSimpleName();

    private static final PrebidMobilePluginRegister instance = new PrebidMobilePluginRegister();

    private final HashMap<String, PrebidMobilePluginRenderer> plugins = new HashMap<>();

    public void registerPlugin(PrebidMobilePluginRenderer prebidMobilePluginRenderers) {
        String rendererName = prebidMobilePluginRenderers.getName();
        if (plugins.containsKey(rendererName)) {
            LogUtil.debug(TAG, "New plugin renderer with name" + rendererName + "will replace the previous one with same name");
        }
        plugins.put(prebidMobilePluginRenderers.getName(), prebidMobilePluginRenderers);
    }

    public void unregisterPlugin(PrebidMobilePluginRenderer prebidMobilePluginRenderer) {
        plugins.remove(prebidMobilePluginRenderer.getName());
    }

    public Boolean containsPlugin(PrebidMobilePluginRenderer prebidMobilePluginRenderer) {
        return plugins.containsKey(prebidMobilePluginRenderer.getName());
    }

    @VisibleForTesting
    public Boolean containsPlugin(String prebidMobilePluginRendererName) {
        return plugins.containsKey(prebidMobilePluginRendererName);
    }

    public void registerEventListener(
            PluginEventListener pluginEventListener,
            String listenerKey
    ) {
        if (plugins.containsKey(pluginEventListener.getPluginRendererName())) {
            plugins.get(pluginEventListener.getPluginRendererName()).registerEventListener(pluginEventListener, listenerKey);
        } else {
            LogUtil.debug(TAG, "Skipping PluginEventListener with name" + pluginEventListener.getPluginRendererName() + ", such key does not exist");
        }
    }

    public void unregisterEventListener(String listenerKey) {
        for (Map.Entry<String, PrebidMobilePluginRenderer> entry : plugins.entrySet()) {
            PrebidMobilePluginRenderer renderer = entry.getValue();
            renderer.unregisterEventListener(listenerKey);
        }
    }

    // Returns the list of available renderers for the given ad unit for RT request
    public List<PrebidMobilePluginRenderer> getRTBListOfRenderersFor(AdUnitConfiguration adUnitConfiguration) {
        List<PrebidMobilePluginRenderer> compliantPlugins = new ArrayList<>();
        for (Map.Entry<String, PrebidMobilePluginRenderer> entry : plugins.entrySet()) {
            PrebidMobilePluginRenderer renderer = entry.getValue();
            if (renderer.isSupportRenderingFor(adUnitConfiguration)) {
                compliantPlugins.add(renderer);
            }
        }
        return compliantPlugins;
    }

    // Returns the registered renderer according to the preferred renderer data in the bid response
    // If no preferred renderer is found, it returns PrebidRenderer to perform default behavior
    public PrebidMobilePluginRenderer getPluginForPreferredRenderer(BidResponse bidResponse) {
        String preferredRendererName = bidResponse.getPreferredPluginRendererName();
        String preferredRendererVersion = bidResponse.getPreferredPluginRendererVersion();
        PrebidMobilePluginRenderer preferredPlugin = plugins.get(preferredRendererName);
        if (preferredPlugin != null
                && preferredPlugin.isSupportRenderingFor(bidResponse.getAdUnitConfiguration())
                && preferredPlugin.getVersion().equals(preferredRendererVersion)) {
            return preferredPlugin;
        } else {
            return plugins.get(PREBID_MOBILE_RENDERER_NAME);
        }
    }

    private PrebidMobilePluginRegister() {
    }

    public static PrebidMobilePluginRegister getInstance() {
        return instance;
    }
}
