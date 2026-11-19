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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    /** The most warnings kept per provider, since the sources in them come from vendor code. */
    private static final int MAX_WARNINGS_PER_PROVIDER = 100;

    /**
     * Holds the static IDs. It isn't one of the registered providers, so unregistering a provider
     * can never remove it.
     */
    final StaticExtendedIdProvider staticProvider = new StaticExtendedIdProvider();

    /**
     * The warnings logged about each provider, keyed by its registration. A provider's problems
     * repeat on every bid request, so each one is logged once per registration: registering the
     * provider again logs it again. Guarded by itself.
     */
    private final Map<Long, Set<String>> providerWarnings = new HashMap<>();

    /** Logs a warning about an ID. */
    private interface Warning {

        void log(@NonNull String message);
    }

    /**
     * Returns the static IDs, then {@code sharedId} if it isn't {@code null}, then the IDs of each
     * registered provider in registration order. Calls {@code getExtendedIds()} on every provider.
     * <p>
     * Every ID is returned as a copy of the JSON it is sent with, built once here. An ID is skipped,
     * with a warning, if that JSON can't be sent: its source is empty, {@code getSource()} or
     * {@code getJson()} throws, the JSON is {@code null} or empty, has a {@code "source"} other than
     * {@code getSource()}, or can't be encoded. A skipped ID doesn't take its source. Then a
     * provider's ID whose source is already in the list is skipped too, so of several providers the
     * first one registered with a valid ID wins. The static IDs and SharedID are never skipped as
     * duplicates, not even of each other.
     * <p>
     * A problem with the static IDs or SharedID is logged on every request, since it comes from the
     * app's own setup. A problem with a provider's ID is usually logged once per registration of the
     * provider: a request that read the providers before the provider was unregistered can still log
     * it afterwards.
     */
    @NonNull
    List<ExtendedId> getAllExtendedIds(@Nullable ExtendedId sharedId) {
        List<ExtendedId> extendedIds = new ArrayList<>();
        Set<String> sources = new HashSet<>();

        Warning everyTime = message -> LogUtil.warning(TAG, "[extendedIds] " + message);
        for (ExtendedId staticId : staticProvider.getExtendedIds()) {
            addOwnId(copyIfValid(staticId, "in the static IDs", everyTime), extendedIds, sources);
        }
        if (sharedId != null) {
            addOwnId(copyIfValid(sharedId, "in SharedID", everyTime), extendedIds, sources);
        }

        PluginRegistry<ExtendedIdProvider> registry = PluginCapability.EXTENDED_IDS.getRegistry();
        List<PluginRegistry.Entry<ExtendedIdProvider>> entries = registry.getActiveEntries();
        forgetUnregisteredProviders(entries);
        for (PluginRegistry.Entry<ExtendedIdProvider> entry : entries) {
            String origin = "provided by " + entry.getInfo();
            Warning once = message -> warnOnce(entry, message);
            for (ExtendedId providerId : getProviderIds(entry, once)) {
                ExtendedId extendedId = copyIfValid(providerId, origin, once);
                if (extendedId == null) {
                    continue;
                }

                String source = extendedId.getSource();
                if (!sources.add(source)) {
                    once.log(skippingFrom(source, origin) + "an ID from this source is already present.");
                    continue;
                }

                extendedIds.add(extendedId);
            }
        }

        return extendedIds;
    }

    /**
     * Adds a static ID or SharedID that passed the checks. It is never skipped as a duplicate.
     */
    private static void addOwnId(@Nullable ExtendedId extendedId, @NonNull List<ExtendedId> extendedIds, @NonNull Set<String> sources) {
        if (extendedId != null) {
            extendedIds.add(extendedId);
            sources.add(extendedId.getSource());
        }
    }

    /**
     * Returns the provider's IDs without {@code null} elements, or an empty list if
     * {@code getExtendedIds()} throws or returns {@code null}.
     */
    @NonNull
    private static List<ExtendedId> getProviderIds(@NonNull PluginRegistry.Entry<ExtendedIdProvider> entry, @NonNull Warning warning) {
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
            warning.log("Skipping the IDs of " + entry.getInfo() + ": its getExtendedIds() threw " + exception.getClass().getName() + ".");
            return new ArrayList<>();
        }
    }

    /**
     * Reads the source and the JSON of the ID once, and returns a copy of that JSON, or {@code null}
     * if it can't be sent. The JSON is not read again later, so what is checked here is what the bid
     * request gets. {@code origin} says where the ID comes from in the warning.
     */
    @Nullable
    private static ExtendedId copyIfValid(@NonNull ExtendedId id, @NonNull String origin, @NonNull Warning warning) {
        String source;
        try {
            source = id.getSource();
        } catch (Exception exception) {
            warning.log("Skipping an ID " + origin + ": its getSource() threw " + exception.getClass().getName() + ".");
            return null;
        }
        if (source == null || source.isEmpty()) {
            warning.log("Skipping an ID " + origin + ": its source is empty.");
            return null;
        }

        String skipping = skippingFrom(source, origin);
        JSONObject json;
        try {
            json = id.getJson();
        } catch (Exception exception) {
            warning.log(skipping + "its getJson() threw " + exception.getClass().getName() + ".");
            return null;
        }
        if (json == null || json.length() == 0) {
            warning.log(skipping + "its JSON is empty.");
            return null;
        }
        if (!source.equals(json.opt("source"))) {
            warning.log(skipping + "the \"source\" in its JSON is different.");
            return null;
        }
        try {
            // A single EID that can't be encoded, e.g. with a NaN number, would fail the whole request.
            return ExtendedId.wrap(json);
        } catch (Exception exception) {
            // The provider may also change the JSON on another thread while it is encoded.
            warning.log(skipping + "its JSON can't be encoded.");
            return null;
        }
    }

    /**
     * The start of a warning about the ID. The warnings name the source and where the ID comes from,
     * never an ID value: exceptions are logged by class only, since their message can contain it.
     */
    @NonNull
    private static String skippingFrom(@NonNull String source, @NonNull String origin) {
        return "Skipping the ID from source " + source + " " + origin + ": ";
    }

    /**
     * Logs a warning about the provider the first time it occurs in this registration of the
     * provider.
     */
    private void warnOnce(@NonNull PluginRegistry.Entry<ExtendedIdProvider> entry, @NonNull String message) {
        boolean isNew;
        synchronized (providerWarnings) {
            Set<String> logged = providerWarnings.get(entry.getSequence());
            if (logged == null) {
                logged = new HashSet<>();
                providerWarnings.put(entry.getSequence(), logged);
            }
            isNew = !logged.contains(message);
            if (isNew) {
                if (logged.size() >= MAX_WARNINGS_PER_PROVIDER) {
                    logged.clear();
                }
                logged.add(message);
            }
        }
        if (isNew) {
            LogUtil.warning(TAG, "[extendedIds] " + message);
        }
    }

    /**
     * Forgets the warnings of the providers that are no longer registered.
     */
    private void forgetUnregisteredProviders(@NonNull List<PluginRegistry.Entry<ExtendedIdProvider>> entries) {
        synchronized (providerWarnings) {
            if (providerWarnings.isEmpty()) {
                return;
            }
            Set<Long> registrations = new HashSet<>();
            for (PluginRegistry.Entry<ExtendedIdProvider> entry : entries) {
                registrations.add(entry.getSequence());
            }
            providerWarnings.keySet().retainAll(registrations);
        }
    }
}
