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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.plugin.PluginCapability;
import org.prebid.mobile.api.plugin.PluginRegistry;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Plugin registerer. The renderers are stored in the registry of {@link PluginCapability#RENDERER},
 * which {@link PrebidMobile#registerPlugin(Object, PluginCapability)} writes to.
 */
public class PrebidMobilePluginRegister {

    public static final String PREBID_MOBILE_RENDERER_NAME = "PrebidRenderer";
    private static final String TAG = PrebidMobilePluginRegister.class.getSimpleName();

    private static final PrebidMobilePluginRegister instance = new PrebidMobilePluginRegister();

    /**
     * Registers the renderer. A renderer whose name is already registered is ignored; to replace it,
     * unregister it first.
     *
     * @deprecated This method is deprecated and will be removed in 4.0. Use
     * {@link PrebidMobile#registerPlugin(Object, PluginCapability)} with {@link PluginCapability#RENDERER} instead.
     */
    @Deprecated
    public void registerPlugin(PrebidMobilePluginRenderer prebidMobilePluginRenderers) {
        getRegistry().add(prebidMobilePluginRenderers);
    }

    /**
     * Unregisters the renderer with the same name as this renderer.
     *
     * @deprecated This method is deprecated and will be removed in 4.0. Use
     * {@link PrebidMobile#unregisterPlugin(Object, PluginCapability)} with {@link PluginCapability#RENDERER} instead.
     */
    @Deprecated
    public void unregisterPlugin(PrebidMobilePluginRenderer prebidMobilePluginRenderer) {
        getRegistry().remove(prebidMobilePluginRenderer);
    }

    /**
     * Returns whether a renderer with the same name as this renderer is registered.
     *
     * @deprecated This method is deprecated and will be removed in 4.0. Use
     * {@link PrebidMobile#containsPlugin(Object, PluginCapability)} with {@link PluginCapability#RENDERER} instead.
     */
    @Deprecated
    public Boolean containsPlugin(PrebidMobilePluginRenderer prebidMobilePluginRenderer) {
        return getRegistry().contains(prebidMobilePluginRenderer);
    }

    @VisibleForTesting
    public Boolean containsPlugin(String prebidMobilePluginRendererName) {
        return getPluginRenderer(prebidMobilePluginRendererName) != null;
    }

    @VisibleForTesting
    public void unregisterAllPlugins() {
        getRegistry().removeAll();
    }

    public PrebidMobilePluginRenderer getDefaultPluginRenderer() {
        return getPluginRenderer(PREBID_MOBILE_RENDERER_NAME);
    }

    public void registerEventListener(
            PluginEventListener pluginEventListener,
            String listenerKey
    ) {
        String rendererName = pluginEventListener.getPluginRendererName();
        PrebidMobilePluginRenderer renderer = getPluginRenderer(rendererName);
        if (renderer != null) {
            renderer.registerEventListener(pluginEventListener, listenerKey);
        } else {
            LogUtil.debug(TAG, "Skipping PluginEventListener with name" + rendererName + ", such key does not exist");
        }
    }

    public void unregisterEventListener(String listenerKey) {
        for (PrebidMobilePluginRenderer renderer : getRegistry().getActive()) {
            renderer.unregisterEventListener(listenerKey);
        }
    }

    // Returns the list of available renderers for the given ad unit for RT request, in registration order
    public List<PrebidMobilePluginRenderer> getRTBListOfRenderersFor(AdUnitConfiguration adUnitConfiguration) {
        List<PrebidMobilePluginRenderer> compliantPlugins = new ArrayList<>();
        for (PrebidMobilePluginRenderer renderer : getRegistry().getActive()) {
            if (renderer.isSupportRenderingFor(adUnitConfiguration)) {
                compliantPlugins.add(renderer);
            }
        }
        return compliantPlugins;
    }

    // Returns the registered renderer according to the preferred renderer data in the bid response
    // If no preferred renderer is found, it returns PrebidRenderer to perform default behavior
    public PrebidMobilePluginRenderer getPluginForPreferredRenderer(BidResponse bidResponse) {
        return getPluginForPreferredRenderer(bidResponse, bidResponse.getAdUnitConfiguration());
    }

    // Returns the registered renderer according to the preferred renderer data in the bid response
    // If no preferred renderer is found, it returns PrebidRenderer to perform default behavior
    public PrebidMobilePluginRenderer getPluginForPreferredRenderer(
            BidResponse bidResponse,
            AdUnitConfiguration adUnitConfiguration
    ) {
        String preferredRendererName = bidResponse.getPreferredPluginRendererName();
        String preferredRendererVersion = bidResponse.getPreferredPluginRendererVersion();
        List<PluginRegistry.Entry<PrebidMobilePluginRenderer>> entries = getRegistry().getActiveEntries();
        PrebidMobilePluginRenderer preferredPlugin = findByName(entries, preferredRendererName);
        if (preferredPlugin != null
                && preferredPlugin.isSupportRenderingFor(adUnitConfiguration)
                && preferredPlugin.getVersion().equals(preferredRendererVersion)) {
            return preferredPlugin;
        } else {
            return findByName(entries, PREBID_MOBILE_RENDERER_NAME);
        }
    }

    @Nullable
    private PrebidMobilePluginRenderer getPluginRenderer(@Nullable String name) {
        return findByName(getRegistry().getActiveEntries(), name);
    }

    /**
     * Finds a renderer by the name it had when it was registered.
     */
    @Nullable
    private static PrebidMobilePluginRenderer findByName(
            @NonNull List<PluginRegistry.Entry<PrebidMobilePluginRenderer>> entries,
            @Nullable String name
    ) {
        for (PluginRegistry.Entry<PrebidMobilePluginRenderer> entry : entries) {
            if (Objects.equals(entry.getInfo().getName(), name)) {
                return entry.getPlugin();
            }
        }
        return null;
    }

    @NonNull
    private static PluginRegistry<PrebidMobilePluginRenderer> getRegistry() {
        return PluginCapability.RENDERER.getRegistry();
    }

    private PrebidMobilePluginRegister() {
    }

    public static PrebidMobilePluginRegister getInstance() {
        return instance;
    }
}
