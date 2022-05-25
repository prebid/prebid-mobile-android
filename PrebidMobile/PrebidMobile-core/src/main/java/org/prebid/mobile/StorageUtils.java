/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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
import android.text.TextUtils;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for external user ids management.
 */
final class StorageUtils {

    private static final String TAG = StorageUtils.class.getSimpleName();

    static final String PB_ExternalUserIdsKey = "PB_ExternalUserIdsKey";

    private StorageUtils() {}

    static void storeExternalUserId(ExternalUserId externalUserId) {
        // Remove the existing ExternalUserId with same source.
        removeStoredExternalUserId(externalUserId.getSource());

        // Storing the ExternalUserId
        SharedPreferences pref = getSharedPreferences();
        if (pref == null) return;

        String externalUserIdString = pref.getString(PB_ExternalUserIdsKey, null);
        List<ExternalUserId> externalUidListFromJson;
        if (!TextUtils.isEmpty(externalUserIdString)) {
            externalUidListFromJson = ExternalUserId.getExternalUidListFromJson(externalUserIdString);
        } else {
            externalUidListFromJson = new ArrayList<>();
        }
        externalUidListFromJson.add(externalUserId);
        SharedPreferences.Editor editor = pref.edit();
        if (externalUserId != null) {
            editor.putString(StorageUtils.PB_ExternalUserIdsKey, externalUidListFromJson.toString());
        }
        editor.apply();
    }

    static List<ExternalUserId> fetchStoredExternalUserIds() {
        SharedPreferences pref = getSharedPreferences();
        if (pref == null) return null;

        String externalUserIds = pref.getString(StorageUtils.PB_ExternalUserIdsKey, null);
        if (externalUserIds != null) {
            return ExternalUserId.getExternalUidListFromJson(externalUserIds);
        }
        return null;
    }

    static ExternalUserId fetchStoredExternalUserId(String source) {
        SharedPreferences pref = getSharedPreferences();
        if (pref == null) return null;

        String externalUserIds = pref.getString(StorageUtils.PB_ExternalUserIdsKey, null);
        if (!TextUtils.isEmpty(externalUserIds)) {
            List<ExternalUserId> externalUidListFromJson = ExternalUserId.getExternalUidListFromJson(externalUserIds);
            for (ExternalUserId externalUserId : externalUidListFromJson) {
                if (externalUserId.getSource().equals(source)) {
                    return externalUserId;
                }
            }
        }
        return null;
    }

    static void removeStoredExternalUserId(String source) {
        SharedPreferences pref = getSharedPreferences();
        if (pref == null) return;

        String externalUserIds = pref.getString(StorageUtils.PB_ExternalUserIdsKey, null);
        if (!TextUtils.isEmpty(externalUserIds)) {
            List<ExternalUserId> externalUidListFromJson = ExternalUserId.getExternalUidListFromJson(externalUserIds);
            ExternalUserId toBeRemoved = null;
            for (ExternalUserId externalUserId : externalUidListFromJson) {
                if (externalUserId.getSource().equals(source)) {
                    toBeRemoved = externalUserId;
                    break;
                }
            }

            if (toBeRemoved != null) {
                externalUidListFromJson.remove(toBeRemoved);
                if (externalUidListFromJson.isEmpty()) {
                    clearStoredExternalUserIds();
                } else {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(StorageUtils.PB_ExternalUserIdsKey, externalUidListFromJson.toString());
                    editor.apply();
                }
            }
        }
    }

    static void clearStoredExternalUserIds() {
        if (fetchStoredExternalUserIds() != null) {
            SharedPreferences pref = getSharedPreferences();
            if (pref == null) return;

            SharedPreferences.Editor editor = pref.edit();
            editor.remove(StorageUtils.PB_ExternalUserIdsKey);
            editor.apply();
        }
    }


    @Nullable
    private static SharedPreferences getSharedPreferences() {
        Context context = PrebidMobile.getApplicationContext();

        if (context == null) {
            LogUtil.error(TAG, "You can't manage external user ids before calling PrebidMobile.initializeSdk().");
            return null;
        }

        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}
