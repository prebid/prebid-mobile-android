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
 * Provider of Extended IDs (EIDs) for OpenRTB auction requests.
 * <p>
 * The SDK calls {@link #getExtendedIds()} on every bid request to collect the provider's current
 * EIDs. Because resolution happens on-demand, returned values always reflect the latest runtime
 * state (e.g. consent flags, session changes).
 * <p>
 * IDs may be resolved synchronously or asynchronously, but {@link #getExtendedIds()} itself must
 * never block: it is called on the auction path and must return immediately with the IDs currently
 * available.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>{@link #onRegister()}: called once, immediately after the provider is
 *       added to the registry. Use it for one-time setup if needed.</li>
 *   <li>{@link #getExtendedIds()}: called on every bid request to collect current EIDs.</li>
 *   <li>{@link #onUnregister()}: called when the provider is removed from the
 *       registry. Use it to release resources.</li>
 * </ol>
 *
 * <h3>Usage example (synchronous provider)</h3>
 * <pre>{@code
 * public class MyIdProvider implements ExtendedIdProvider {
 *
 *     public Info getProviderInfo() {
 *         return new Info("myProvider", "1.0");
 *     }
 *
 *     public List<ExtendedId> getExtendedIds() {
 *         String id = resolveIdFromLocalState();
 *         return List.of(new ExternalUserId("my-source.com",
 *                 List.of(new ExternalUserId.UniqueId(id, 1))));
 *     }
 * }
 * }</pre>
 *
 * @see org.prebid.mobile.PrebidMobile#registerExtendedIdProvider
 */
public interface ExtendedIdProvider {

    /**
     * Returns metadata describing this provider (name and version).
     * Used by the registry for deduplication, logging, and debugging.
     * Must return the same value for the lifetime of this provider instance.
     *
     * @return provider metadata
     */
    @NonNull
    Info getProviderInfo();

    /**
     * Returns the provider's current EIDs. The registry calls this on the bid-request thread on
     * every auction, so it must be non-blocking and return immediately. Do not perform network
     * calls, disk I/O, or any other blocking or long-running work here.
     *
     * @return current list of extended IDs; may be empty but never null
     */
    @NonNull
    List<ExtendedId> getExtendedIds();

    /**
     * Called once, immediately after this provider is added to the registry
     * via {@link org.prebid.mobile.PrebidMobile#registerExtendedIdProvider}.
     * Use it for one-time initialization if needed.
     */
    void onRegister();

    /**
     * Called when this provider is removed from the registry via
     * {@link org.prebid.mobile.PrebidMobile#unregisterExtendedIdProvider}
     * or when the registry is cleared. Release resources here.
     */
    void onUnregister();

    /**
     * Identity of an {@link ExtendedIdProvider}: its {@code name} and {@code version}.
     * <p>
     * The registry treats two providers as the same when their {@code Info} is equal (both
     * {@code name} and {@code version} match): registering a second provider with an equal
     * {@code Info} is ignored, and {@link org.prebid.mobile.PrebidMobile#unregisterExtendedIdProvider}
     * removes whichever instance is registered under that {@code Info}. Choose a {@code name}
     * specific to your integration (e.g. your source domain) so it does not collide with other vendors.
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