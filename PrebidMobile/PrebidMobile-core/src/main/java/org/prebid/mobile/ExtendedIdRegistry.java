/*
 *    Copyright 2020-2026 Prebid.org, Inc.
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

package org.prebid.mobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.api.eid.ExtendedId;
import org.prebid.mobile.api.eid.ExtendedIdProvider;
import org.prebid.mobile.api.plugin.PluginCapability;
import org.prebid.mobile.api.plugin.PluginRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The EID sources of {@link TargetingParams}: its static IDs, SharedID, and the providers registered
 * for {@link PluginCapability#EXTENDED_IDS}.
 * <p>
 * {@link #getAllExtendedIds(ExtendedId)} is called for each bid request. The providers live in the
 * capability's {@link PluginRegistry}, which keeps them in registration order and makes a provider
 * active only once its {@code onRegister()} returns.
 */
class ExtendedIdRegistry {

    private static final String TAG = "ExtendedIdRegistry";

    /**
     * Holds the static IDs. It isn't one of the registered providers, so unregistering a provider
     * can never remove it.
     */
    final StaticExtendedIdProvider staticProvider = new StaticExtendedIdProvider();

    /**
     * Returns the static IDs, then {@code sharedId} if it isn't {@code null}, then the IDs of each
     * registered provider in registration order. Calls {@code getExtendedIds()} on every provider.
     * <p>
     * A provider's ID whose source is already in the list is skipped, so the static IDs and SharedID
     * are always sent, and of several providers the first one registered wins.
     */
    @NonNull
    List<ExtendedId> getAllExtendedIds(@Nullable ExtendedId sharedId) {
        List<ExtendedId> extendedIds = new ArrayList<>(staticProvider.getExtendedIds());
        if (sharedId != null) {
            extendedIds.add(sharedId);
        }

        Set<String> sources = new HashSet<>();
        for (ExtendedId extendedId : extendedIds) {
            sources.add(extendedId.getSource());
        }

        PluginRegistry<ExtendedIdProvider> registry = PluginCapability.EXTENDED_IDS.getRegistry();
        for (PluginRegistry.Entry<ExtendedIdProvider> entry : registry.getActiveEntries()) {
            for (ExtendedId extendedId : getProviderIds(entry)) {
                String source;
                try {
                    source = extendedId.getSource();
                } catch (Exception exception) {
                    LogUtil.warning(TAG, "[extendedIds] Skipping an ID provided by " + entry.getInfo() + ": its getSource() threw " + exception);
                    continue;
                }

                if (!sources.add(source)) {
                    LogUtil.warning(TAG, "[extendedIds] Skipping the ID from source " + source + " provided by " + entry.getInfo() + ": an ID from this source is already present.");
                    continue;
                }

                extendedIds.add(extendedId);
            }
        }

        return extendedIds;
    }

    /**
     * Returns the provider's IDs without {@code null} elements, or an empty list if
     * {@code getExtendedIds()} throws or returns {@code null}.
     */
    @NonNull
    private static List<ExtendedId> getProviderIds(@NonNull PluginRegistry.Entry<ExtendedIdProvider> entry) {
        List<ExtendedId> result = new ArrayList<>();
        try {
            List<ExtendedId> ids = entry.getPlugin().getExtendedIds();
            if (ids == null) {
                return result;
            }
            // Copied inside the try: the provider may change its own list while it is read.
            for (ExtendedId id : ids) {
                if (id != null) {
                    result.add(id);
                }
            }
            return result;
        } catch (Exception exception) {
            // Also catches checked exceptions, which Kotlin code can throw without declaring them.
            LogUtil.warning(TAG, "[extendedIds] Skipping the IDs of " + entry.getInfo() + ": its getExtendedIds() threw " + exception);
            return new ArrayList<>();
        }
    }
}
