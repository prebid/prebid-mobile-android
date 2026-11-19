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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The static EIDs set through {@link TargetingParams#addExternalUserId} and
 * {@link TargetingParams#setExternalUserIds}, keyed by source.
 * <p>
 * It isn't a registered provider. Each change is applied under a lock, so a bid request never sees a
 * partly updated list. The IDs are kept in a {@link HashMap}, as before providers existed, so the
 * order of the static IDs in a bid request doesn't change.
 */
class StaticExtendedIdProvider {

    private final Map<String, ExternalUserId> userIds = new HashMap<>();

    /**
     * A snapshot of the static IDs.
     */
    @NonNull
    synchronized List<ExternalUserId> getExternalUserIds() {
        return new ArrayList<>(userIds.values());
    }

    @NonNull
    List<ExtendedId> getExtendedIds() {
        return new ArrayList<>(getExternalUserIds());
    }

    /**
     * Replaces all static IDs. {@code null} clears them, and {@code null} elements are skipped.
     * Of several IDs with the same source, the last one is kept.
     */
    synchronized void setUserIds(@Nullable List<ExternalUserId> newUserIds) {
        userIds.clear();
        if (newUserIds == null) {
            return;
        }
        for (ExternalUserId userId : newUserIds) {
            if (userId != null) {
                userIds.put(userId.getSource(), userId);
            }
        }
    }

    /**
     * Adds the ID, replacing the ID that has the same source.
     */
    synchronized void addUserId(@NonNull ExternalUserId userId) {
        userIds.put(userId.getSource(), userId);
    }

    /**
     * Removes the ID with the given source.
     */
    synchronized void removeUserId(@Nullable String source) {
        userIds.remove(source);
    }

    synchronized void clear() {
        userIds.clear();
    }
}
