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

package org.prebid.mobile.api.eid;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * A source of Extended IDs (EIDs) for OpenRTB bid requests. Register it with
 * {@link org.prebid.mobile.PrebidMobile#registerPlugin(Object, org.prebid.mobile.api.plugin.PluginCapability)}
 * as {@link org.prebid.mobile.api.plugin.PluginCapability#EXTENDED_IDS}.
 * <p>
 * The SDK calls {@link #getExtendedIds()} on every bid request, so the IDs it sends always reflect
 * the provider's current state (consent, session changes, refreshed tokens). IDs may be resolved
 * synchronously or asynchronously, but {@link #getExtendedIds()} itself must not block, and it may
 * be called on any thread.
 * <p>
 * Provider IDs are sent after the static IDs set through {@link org.prebid.mobile.TargetingParams}
 * and SharedID, in the order the providers were registered. Registration order is the only
 * ordering guarantee. An ID whose source is already present (a static ID, SharedID, or an ID from a
 * provider registered earlier) is skipped, and the SDK logs a warning.
 * <p>
 * The SDK reads each ID's {@link ExtendedId#getJson()} once per bid request and sends that JSON. An
 * ID is also skipped, with a warning, if its source is empty, its JSON is {@code null} or empty,
 * can't be built or encoded, or has a {@code "source"} other than {@link ExtendedId#getSource()}. A
 * skipped ID doesn't take its source, so another provider's ID from the same source can still be
 * sent. Each warning about a provider's IDs is usually logged once per registration of the provider.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>{@link #onRegister()} runs synchronously inside {@code registerPlugin}, once per
 *       registration. The provider's IDs are used only after it returns.</li>
 *   <li>{@link #getExtendedIds()} is called for every bid request, on any thread, possibly
 *       concurrently.</li>
 *   <li>{@link #onUnregister()} runs inside {@code unregisterPlugin}. Requests already being built
 *       may still call {@link #getExtendedIds()} during or after it, so keep it safe to call (e.g.
 *       return an empty list).</li>
 * </ol>
 *
 * <h3>Usage example</h3>
 * <pre>{@code
 * public class MyIdProvider implements ExtendedIdProvider {
 *
 *     private static final Info INFO = new Info("my-source.com", "1.0");
 *
 *     public Info getProviderInfo() {
 *         return INFO;
 *     }
 *
 *     public List<ExtendedId> getExtendedIds() {
 *         String id = resolvedId;
 *         if (id == null) {
 *             return Collections.emptyList();
 *         }
 *         return Collections.singletonList(new ExternalUserId("my-source.com",
 *                 Collections.singletonList(new ExternalUserId.UniqueId(id, 1))));
 *     }
 * }
 *
 * PrebidMobile.registerPlugin(new MyIdProvider(), PluginCapability.EXTENDED_IDS);
 * }</pre>
 */
public interface ExtendedIdProvider {

    /**
     * Returns the provider's name and version. The SDK uses it to detect duplicate registrations
     * and in logs. Must return the same value for the lifetime of the provider.
     * <p>
     * Must not return {@code null} or throw: it is called by {@code registerPlugin},
     * {@code unregisterPlugin} and {@code containsPlugin}, which don't catch errors from it.
     */
    @NonNull
    Info getProviderInfo();

    /**
     * Returns the provider's current EIDs. May be empty.
     * <p>
     * Called for every bid request, so it must return immediately: no network calls, disk I/O, or
     * other blocking work. May be called on any thread, including the main thread, and on several
     * threads at once.
     */
    @NonNull
    List<ExtendedId> getExtendedIds();

    /**
     * Called synchronously inside {@code registerPlugin}, once per registration. The provider's IDs
     * are used only after it returns. If it throws, the provider is not registered.
     */
    default void onRegister() {
    }

    /**
     * Called when the provider is unregistered. Requests in progress may still call
     * {@link #getExtendedIds()} afterwards, so keep it safe to call.
     */
    default void onUnregister() {
    }

    /**
     * Identity of an {@link ExtendedIdProvider}: its {@code name} and {@code version}.
     * <p>
     * Two providers with equal info (both {@code name} and {@code version} match) are treated as
     * the same provider: registering the second one is ignored, and unregistering either of them
     * removes the registered one. Choose a {@code name} specific to your integration (e.g. your
     * source domain) so it doesn't collide with other vendors.
     */
    final class Info {

        @NonNull
        private final String name;
        @NonNull
        private final String version;

        public Info(@NonNull String name, @NonNull String version) {
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
            if (this == o) return true;
            if (!(o instanceof Info)) return false;
            Info info = (Info) o;
            return name.equals(info.name) && version.equals(info.version);
        }

        @Override
        public int hashCode() {
            return 31 * name.hashCode() + version.hashCode();
        }

        @NonNull
        @Override
        public String toString() {
            return name + "/" + version;
        }
    }
}
