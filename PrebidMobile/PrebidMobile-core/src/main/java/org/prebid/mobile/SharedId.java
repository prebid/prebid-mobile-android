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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.sdk.PrebidContextHolder;

import java.util.List;
import java.util.UUID;

public class SharedId {
    static final String TAG = SharedId.class.getSimpleName();

    static final String PB_SharedIdKey = "PB_SharedIdKey";
    private static ExternalUserId sessionId = null;

    static ExternalUserId getIdentifier() {
        Boolean persistentStorageAllowed = TargetingParams.getDeviceAccessConsent();

        // If sharedId was used previously in this session, then use that id
        if (sessionId != null) {
            if (persistentStorageAllowed != null && persistentStorageAllowed) {
                if (!sessionId.getUniqueIds().isEmpty()) {
                    ExternalUserId.UniqueId uniqueId = sessionId.getUniqueIds().get(0);
                    storeSharedId(uniqueId.getId());
                }
            }
            return sessionId;
        }

        // Otherwise if an id is available in persistent storage, then use that id
        if (persistentStorageAllowed != null && persistentStorageAllowed) {
            String storedSharededId = fetchSharedId();
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
            if (!sessionId.getUniqueIds().isEmpty()) {
                ExternalUserId.UniqueId uniqueId = sessionId.getUniqueIds().get(0);
                storeSharedId(uniqueId.getId());
            }
        }
        return eid;
    }

    static void resetIdentifier() {
        sessionId = null;
        storeSharedId(null);
    }

    static String fetchSharedId() {
        SharedPreferences pref = getSharedPreferences();
        if (pref == null) return null;

        return pref.getString(PB_SharedIdKey, null);
    }

    static void storeSharedId(String sharedId) {
        // Storing the SharedId
        SharedPreferences pref = getSharedPreferences();
        if (pref == null) return;

        SharedPreferences.Editor editor = pref.edit();
        if (sharedId != null) {
            editor.putString(PB_SharedIdKey, sharedId);
        } else {
            editor.remove(PB_SharedIdKey);
        }
        editor.apply();
    }

    private static ExternalUserId externalUserIdFrom(String identifier) {
        ExternalUserId.UniqueId uniqueId = new ExternalUserId.UniqueId(identifier, 1);
        return new ExternalUserId("pubcid.org", List.of(uniqueId));
    }

    @Nullable
    private static SharedPreferences getSharedPreferences() {
        Context context = PrebidContextHolder.getContext();

        if (context == null) {
            LogUtil.error(TAG, "You can't manage external user ids before calling PrebidMobile.initializeSdk().");
            return null;
        }

        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}