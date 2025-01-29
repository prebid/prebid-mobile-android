/*
 *    Copyright 2020-2025 Prebid.org, Inc.
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

import java.util.UUID;

public class SharedId {
    private static ExternalUserId sessionId = null;

    static ExternalUserId getIdentifier() {
        Boolean persistentStorageAllowed = TargetingParams.getDeviceAccessConsent();

        // If sharedId was used previously in this session, then use that id
        if (sessionId != null) {
            if (persistentStorageAllowed != null && persistentStorageAllowed) {
                StorageUtils.storeSharedId(sessionId.getIdentifier());
            }
            return sessionId;
        }

        // Otherwise if an id is available in persistent storage, then use that id
        if (persistentStorageAllowed != null && persistentStorageAllowed) {
            String storedSharededId = StorageUtils.fetchSharedId();
            if (storedSharededId != null) {
                ExternalUserId eid = externalUserIdFrom(storedSharededId);
                sessionId = eid;
                return eid;
            }
        }

        // Otherwise generate a new id
        ExternalUserId eid = externalUserIdFrom(UUID.randomUUID().toString());
        sessionId = eid;
        if (persistentStorageAllowed != null && persistentStorageAllowed) {
            StorageUtils.storeSharedId(eid.getIdentifier());
        }
        return eid;
    }

    static void resetIdentifier() {
        sessionId = null;
        StorageUtils.storeSharedId(null);
    }

    private static ExternalUserId externalUserIdFrom(String identifier) {
        return new ExternalUserId("pubcid.org", identifier, 1, null);
    }
}