/*
 *    Copyright 2018-2026 Prebid.org, Inc.
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

package org.prebid.mobile.api.plugin;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;

/**
 * A kind of plugin the SDK accepts. Pass one to
 * {@link PrebidMobile#registerPlugin(Object, PluginCapability)}.
 * <p>
 * Each capability accepts objects of its plugin type {@code P}, e.g. {@link #RENDERER} accepts
 * {@link PrebidMobilePluginRenderer} objects. The compiler checks the type. Only the SDK defines
 * capabilities.
 *
 * @param <P> the plugin type the capability accepts.
 */
public final class PluginCapability<P> {

    /**
     * Plugin renderers: objects that implement {@link PrebidMobilePluginRenderer}.
     * <p>
     * Renderers are identified by name alone: a renderer whose name is already registered is
     * ignored, whatever its version. To replace a renderer, unregister it first.
     */
    public static final PluginCapability<PrebidMobilePluginRenderer> RENDERER = new PluginCapability<>(
            new PluginRegistry<>(
                    "renderer",
                    renderer -> new PrebidPluginInfo(renderer.getName(), ""),
                    PluginRegistry.UNLIMITED,
                    null,
                    null
            )
    );

    @NonNull
    private final PluginRegistry<P> registry;

    private PluginCapability(@NonNull PluginRegistry<P> registry) {
        this.registry = registry;
    }

    /**
     * The capability name, used in logs.
     */
    @NonNull
    public String getName() {
        return registry.getName();
    }

    /**
     * The registry of this capability. The SDK reads the capability's plugins from here.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @NonNull
    public PluginRegistry<P> getRegistry() {
        return registry;
    }

    @NonNull
    @Override
    public String toString() {
        return getName();
    }
}
