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

import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PluginRegisterCustomRenderer {

    public static final String CUSTOM_RENDERER_KEY = "plugin_custom_renderer_key";

    private static final PluginRegisterCustomRenderer instance = new PluginRegisterCustomRenderer();

    public HashMap<AdUnitConfiguration, List<PrebidMobilePluginCustomRenderer>> prebidMobilePluginCustomRenderer = new HashMap<>();

    public List<String> getRTBListOfCustomRenderersFor(AdUnitConfiguration adUnitConfiguration) {
        List<PrebidMobilePluginCustomRenderer> customRenderers = prebidMobilePluginCustomRenderer.get(adUnitConfiguration);
        if (customRenderers != null && customRenderers.size() > 0) {
            List<String> rendererNames = new ArrayList<>();
            for (int i = 0; i < customRenderers.size(); i++) {
                String name = customRenderers.get(i).getName();
                rendererNames.add(name);
            }
            return rendererNames;
        } else {
            return  null;
        }
    }

    public PrebidMobilePluginCustomRenderer getPluginForPreferredRenderer(BidResponse bidResponse) {
        List<PrebidMobilePluginCustomRenderer> customRenderers = prebidMobilePluginCustomRenderer.get(bidResponse.getAdUnitConfiguration());
        String preferredRenderer = bidResponse.gePreferredCustomRenderer();
        if (customRenderers != null && customRenderers.size() > 0) {
            for (int i = 0; i < customRenderers.size(); i++) {
                PrebidMobilePluginCustomRenderer plugin = customRenderers.get(i);
                if (plugin.getName().equals(preferredRenderer)) {
                    return plugin;
                }
            }
        }
        return null;
    }

    private PluginRegisterCustomRenderer() {
    }

    public static PluginRegisterCustomRenderer getInstance() {
        return instance;
    }
}
