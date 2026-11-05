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

import java.util.Objects;

/**
 * Identity of a plugin: its name and version.
 * <p>
 * Within one {@link PluginCapability}, two plugins with equal info are the same plugin: registering
 * the second one is ignored, and unregistering either of them removes the registered one. Renderers
 * are identified by name alone, as they always have been.
 */
public final class PrebidPluginInfo {

    @NonNull
    private final String name;
    @NonNull
    private final String version;

    /**
     * @param name    the plugin name. Choose one specific to your integration (e.g. your domain),
     *                so it doesn't collide with other vendors.
     * @param version the plugin version.
     */
    public PrebidPluginInfo(@NonNull String name, @NonNull String version) {
        this.name = name;
        this.version = version;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PrebidPluginInfo)) {
            return false;
        }
        PrebidPluginInfo other = (PrebidPluginInfo) o;
        return Objects.equals(name, other.name) && Objects.equals(version, other.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }

    /**
     * Returns {@code name/version}, or the name alone when the version is empty.
     */
    @NonNull
    @Override
    public String toString() {
        return version == null || version.isEmpty() ? String.valueOf(name) : name + "/" + version;
    }
}
