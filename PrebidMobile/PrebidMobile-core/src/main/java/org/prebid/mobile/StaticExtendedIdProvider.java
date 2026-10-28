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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Built-in provider that holds static EIDs set via the legacy
 * {@link TargetingParams#addExternalUserId} / {@link TargetingParams#setExternalUserIds} API,
 * or directly via the convenience methods on {@link ExtendedIdRegistry}.
 * <p>
 * Always registered in the registry. Its {@link #getExternalUserIds()} returns a
 * snapshot of the current static EID map on every bid request.
 */
class StaticExtendedIdProvider implements ExtendedIdProvider {

    private static final Info INFO = new Info("_static", "1.0");

    private final ConcurrentHashMap<String, ExternalUserId> eids = new ConcurrentHashMap<>();

    @NonNull
    @Override
    public Info getProviderInfo() {
        return INFO;
    }

    @Override
    public void onRegister() {}

    @Override
    public void onUnregister() {}

    @NonNull
    @Override
    public List<ExtendedId> getExtendedIds() {
        return Collections.unmodifiableList(getExternalUserIds());
    }

    @NonNull
    List<ExternalUserId> getExternalUserIds() {
        return new ArrayList<>(eids.values());
    }

    void setUserIds(@Nullable List<ExternalUserId> userIds) {
        eids.clear();
        if (userIds == null) return;
        for (ExternalUserId userId : userIds) {
            if (userId == null) continue;
            eids.put(userId.getSource(), userId);
        }
    }

    void addUserId(@NonNull ExternalUserId userId) {
        eids.put(userId.getSource(), userId);
    }

    void removeUserId(@NonNull String source) {
        eids.remove(source);
    }

    void clear() {
        eids.clear();
    }
}
