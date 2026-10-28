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

import org.prebid.mobile.api.eid.ExtendedId;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.api.eid.ExtendedIdProvider;
import org.prebid.mobile.api.eid.ExtendedIdProvider.Info;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central, thread-safe registry for external user IDs (EIDs).
 * <p>
 * All EID sources are {@link ExtendedIdProvider} instances. The registry
 * calls {@link ExtendedIdProvider#getExtendedIds()} on every registered provider
 * at each bid request, so values are always fresh.
 * <p>
 * For backward compatibility, the registry also exposes convenience methods
 * ({@link #addStaticExternalUserId}, {@link #setStaticExternalUserIds}, etc.) that manage
 * a built-in {@link StaticExtendedIdProvider} internally.
 * <p>
 * {@link #getAllExtendedIds()} collects EIDs from all providers and is called by
 * the bid request builder to populate {@code user.ext.eids} in the OpenRTB payload.
 *
 * @see ExtendedIdProvider
 */
class ExtendedIdRegistry {

    private static final String TAG = "ExtendedIdRegistry";

    private static final ExtendedIdRegistry INSTANCE = new ExtendedIdRegistry();

    private final ConcurrentHashMap<Info, ExtendedIdProvider> providers = new ConcurrentHashMap<>();
    private final StaticExtendedIdProvider staticProvider = new StaticExtendedIdProvider();

    private ExtendedIdRegistry() {
        providers.put(staticProvider.getProviderInfo(), staticProvider);
    }

    /**
     * Returns the singleton registry instance.
     */
    @NonNull
    static ExtendedIdRegistry getInstance() {
        return INSTANCE;
    }

    // --- Static EIDs (convenience API delegating to built-in StaticExtendedIdProvider) ---

    /**
     * Replaces all static EIDs with the provided list.
     * Existing static EIDs are discarded. Pass {@code null} to clear.
     * <p>
     * Typed as {@link ExternalUserId} to stay compatible
     * with the deprecated {@link TargetingParams} API.
     */
    void setStaticExternalUserIds(@Nullable List<ExternalUserId> userIds) {
        staticProvider.setUserIds(userIds);
    }

    /**
     * Adds or replaces a single static external user ID, keyed by its source.
     */
    void addStaticExternalUserId(@NonNull ExternalUserId userId) {
        staticProvider.addUserId(userId);
    }

    /**
     * Removes the static EID with the given source identifier.
     * No-op if no EID with that source exists.
     */
    void removeStaticExternalUserId(@NonNull String source) {
        staticProvider.removeUserId(source);
    }

    /**
     * Removes all static EIDs. Does not affect other providers.
     */
    void clearStaticExternalUserIds() {
        staticProvider.clear();
    }

    /**
     * Returns only the static EIDs. Does not include EIDs from other providers.
     * Use {@link #getAllExtendedIds()} to get the full list.
     * <p>
     * Typed as {@link ExternalUserId} to stay compatible
     * with the deprecated {@link TargetingParams} API.
     */
    @NonNull
    List<ExternalUserId> getStaticExternalUserIds() {
        return staticProvider.getExternalUserIds();
    }

    // --- Providers ---

    /**
     * Returns all EIDs from all registered providers (including the built-in
     * static provider). Each provider's {@link ExtendedIdProvider#getExtendedIds()}
     * is called on-demand. This is the list sent in each auction request.
     */
    @NonNull
    List<ExtendedId> getAllExtendedIds() {
        List<ExtendedId> result = new ArrayList<>();
        for (ExtendedIdProvider provider : providers.values()) {
            try {
                List<ExtendedId> ids = provider.getExtendedIds();
                if (ids == null) {
                    continue;
                }
                for (ExtendedId id : ids) {
                    if (id != null) {
                        result.add(id);
                    }
                }
            } catch (Exception e) {
                LogUtil.warning(TAG, "Provider " + provider.getProviderInfo()
                        + " failed to supply EIDs, skipping: " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * Registers a provider and calls its {@link ExtendedIdProvider#onRegister()}.
     * Duplicate registrations (same {@link Info}) are silently ignored.
     * The provider's EIDs will be included in all subsequent auction requests.
     * Reached through {@link org.prebid.mobile.PrebidMobile#registerExtendedIdProvider}.
     */
    void addProvider(@NonNull ExtendedIdProvider provider) {
        Info info = provider.getProviderInfo();
        if (providers.putIfAbsent(info, provider) != null) {
            LogUtil.warning(TAG, "Provider " + info + " already registered, ignoring duplicate");
            return;
        }
        LogUtil.debug(TAG, "Provider " + info + " registered");
        provider.onRegister();
    }

    /**
     * Removes a previously registered provider and calls its
     * {@link ExtendedIdProvider#onUnregister()}.
     * Its EIDs will no longer be included in auction requests.
     * Reached through {@link org.prebid.mobile.PrebidMobile#unregisterExtendedIdProvider}.
     */
    void removeProvider(@NonNull ExtendedIdProvider provider) {
        Info info = provider.getProviderInfo();
        ExtendedIdProvider removed = providers.remove(info);
        if (removed == null) {
            return;
        }
        removed.onUnregister();
        LogUtil.debug(TAG, "Provider " + info + " removed");
    }

    /**
     * Removes all registered providers (calling {@link ExtendedIdProvider#onUnregister()}
     * on each) and clears all static EIDs. The built-in static provider is
     * re-registered automatically.
     * Typically used during SDK teardown or testing.
     */
    void clearProviders() {
        for (ExtendedIdProvider provider : providers.values()) {
            provider.onUnregister();
        }
        providers.clear();
        staticProvider.clear();
        providers.put(staticProvider.getProviderInfo(), staticProvider);
        LogUtil.debug(TAG, "All providers cleared");
    }

    /**
     * Returns {@code true} if the given provider is currently registered.
     * Reached through {@link org.prebid.mobile.PrebidMobile#containsExtendedIdProvider}.
     */
    boolean hasProvider(@NonNull ExtendedIdProvider provider) {
        return providers.containsKey(provider.getProviderInfo());
    }

}
