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

import org.prebid.mobile.api.rendering.PrebidRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.ArrayList;
import java.util.List;

public class PluginRegisterCustomRenderer {

    public static final String CUSTOM_RENDERER_KEY = "plugin_custom_renderer_key";

    private static final PluginRegisterCustomRenderer instance = new PluginRegisterCustomRenderer();

    private final List<PrebidMobilePluginCustomRenderer> plugins = new ArrayList();

    private final PrebidRenderer defaultPrebidRenderer = new PrebidRenderer();

    public void registerPlugin(PrebidMobilePluginCustomRenderer prebidMobilePluginCustomRenderers) {
        plugins.add(prebidMobilePluginCustomRenderers);
    }

    public void unregisterPlugin(PrebidMobilePluginCustomRenderer prebidMobilePluginCustomRenderer) {
        plugins.remove(prebidMobilePluginCustomRenderer);
    }

    // Returns the list of available renderers for the given ad unit for RT request
    public List<String> getRTBListOfCustomRenderersFor(AdUnitConfiguration adUnitConfiguration) {
        List<String> compliantPlugins = new ArrayList<>();
        if (plugins.size() > 0) {
            for (int i = 0; i < plugins.size(); i++) {
                if (plugins.get(i).isSupportRenderingFor(adUnitConfiguration)) {
                    compliantPlugins.add(plugins.get(i).getName());
                }
            }
        }
        return compliantPlugins;
    }

    // Returns the registered renderer according to the preferred renderer name in the bid response
    // If no preferred renderer is found, it returns PrebidRenderer to perform default behavior
    public PrebidMobilePluginCustomRenderer getPluginForPreferredRenderer(BidResponse bidResponse) {
        String preferredRendererName = bidResponse.gePreferredCustomRendererName();
        PrebidMobilePluginCustomRenderer preferredPlugin = null;
        if (plugins.size() > 0) {
            for (int i = 0; i < plugins.size(); i++) {
                if (plugins.get(i).getName().equals(preferredRendererName)) {
                    preferredPlugin = plugins.get(i);
                }
            }
        }
        if (preferredPlugin != null) {
            return preferredPlugin;
        } else {
            return defaultPrebidRenderer; // Return default
        }
    }

    private PluginRegisterCustomRenderer() {
    }

    public static PluginRegisterCustomRenderer getInstance() {
        return instance;
    }
}
