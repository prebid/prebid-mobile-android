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

import org.json.JSONObject;
import org.prebid.mobile.api.eid.ExtendedId;
import org.prebid.mobile.api.eid.ExtendedIdProvider;
import org.prebid.mobile.api.plugin.PluginCapability;
import org.prebid.mobile.api.plugin.PluginRegistry;

import java.util.ArrayList;
import java.util.Collections;
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
    /** Bounds the memory of the logged warnings, since sources come from vendor code. */
    private static final int MAX_LOGGED_WARNINGS = 100;

    /**
     * Holds the static IDs. It isn't one of the registered providers, so unregistering a provider
     * can never remove it.
     */
    final StaticExtendedIdProvider staticProvider = new StaticExtendedIdProvider();

    /**
     * The warnings already logged. This runs for every bid request, so a provider that keeps
     * returning the same rejected ID would otherwise log the same warning each time.
     */
    private final Set<String> loggedWarnings = Collections.synchronizedSet(new HashSet<>());

    /**
     * Returns the static IDs, then {@code sharedId} if it isn't {@code null}, then the IDs of each
     * registered provider in registration order. Calls {@code getExtendedIds()} on every provider.
     * <p>
     * A provider's ID is returned as a copy of the JSON it is sent with, built once here. It is
     * skipped, with a warning, if that JSON can't be sent ({@code null}, empty, throws, can't be
     * encoded, or has a {@code "source"} other than {@code getSource()}), or if its source is already
     * in the list. The static IDs and SharedID are never skipped. A skipped ID doesn't take its
     * source, so of several providers the first one registered with a valid ID wins. Each warning is
     * logged once per provider, source and reason.
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
            for (ExtendedId providerId : getProviderIds(entry)) {
                ExtendedId extendedId = copyIfValid(entry, providerId);
                if (extendedId == null) {
                    continue;
                }

                String source = extendedId.getSource();
                if (!sources.add(source)) {
                    warnOnce("Skipping the ID from source " + source + " provided by " + entry.getInfo() + ": an ID from this source is already present.");
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
    private List<ExtendedId> getProviderIds(@NonNull PluginRegistry.Entry<ExtendedIdProvider> entry) {
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
            warnOnce("Skipping the IDs of " + entry.getInfo() + ": its getExtendedIds() threw " + exception.getClass().getName());
            return new ArrayList<>();
        }
    }

    /**
     * Builds the JSON of a provider's ID once and returns a copy of it, or {@code null} if it can't be
     * sent. The JSON is not read again later, so what is checked here is what the bid request gets.
     */
    @Nullable
    private ExtendedId copyIfValid(@NonNull PluginRegistry.Entry<ExtendedIdProvider> entry, @NonNull ExtendedId id) {
        String source;
        try {
            source = id.getSource();
        } catch (Exception exception) {
            warnOnce("Skipping an ID provided by " + entry.getInfo() + ": its getSource() threw " + exception.getClass().getName());
            return null;
        }
        if (source == null || source.isEmpty()) {
            warnOnce("Skipping an ID provided by " + entry.getInfo() + ": its source is empty.");
            return null;
        }

        String skipping = "Skipping the ID from source " + source + " provided by " + entry.getInfo() + ": ";
        JSONObject json;
        try {
            json = id.getJson();
        } catch (Exception exception) {
            warnOnce(skipping + "its getJson() threw " + exception.getClass().getName());
            return null;
        }
        if (json == null || json.length() == 0) {
            warnOnce(skipping + "its JSON is empty.");
            return null;
        }
        if (!source.equals(json.opt("source"))) {
            warnOnce(skipping + "the \"source\" in its JSON is different.");
            return null;
        }
        try {
            return ExtendedId.wrap(json);
        } catch (Exception exception) {
            // The provider may also change the JSON on another thread while it is encoded.
            warnOnce(skipping + "its JSON can't be encoded.");
            return null;
        }
    }

    /**
     * Logs a warning the first time it occurs. The messages name the provider and the source, never
     * an ID value: exceptions are logged by class only, since their message can contain the value.
     */
    private void warnOnce(@NonNull String message) {
        if (loggedWarnings.size() >= MAX_LOGGED_WARNINGS) {
            loggedWarnings.clear();
        }
        if (loggedWarnings.add(message)) {
            LogUtil.warning(TAG, "[extendedIds] " + message);
        }
    }
}
