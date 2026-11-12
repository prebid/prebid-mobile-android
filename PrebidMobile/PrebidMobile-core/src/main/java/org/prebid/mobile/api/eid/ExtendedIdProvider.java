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
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>{@link #onRegister()} is called once after the provider is registered, before its IDs are
 *       included in bid requests.</li>
 *   <li>{@link #getExtendedIds()} is called on every bid request.</li>
 *   <li>{@link #onUnregister()} is called after the provider is unregistered.</li>
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
     */
    @NonNull
    Info getProviderInfo();

    /**
     * Returns the provider's current EIDs. May be empty.
     * <p>
     * Called on every bid request, so it must return immediately: no network calls, disk I/O, or
     * other blocking work. May be called on any thread, including the main thread.
     */
    @NonNull
    List<ExtendedId> getExtendedIds();

    /**
     * Called once after the provider is registered, before its IDs are included in bid requests.
     * If it throws, the provider is not registered.
     */
    default void onRegister() {
    }

    /**
     * Called when the provider is unregistered. Release resources here.
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
    class Info {

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
