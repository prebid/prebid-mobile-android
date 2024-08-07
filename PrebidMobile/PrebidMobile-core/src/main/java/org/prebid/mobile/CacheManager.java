/*
 *    Copyright 2020-2021 Prebid.org, Inc.
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

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.HashMap;
import java.util.UUID;

/**
 * Original API cache manager. Manages saving and receiving cached ad.
 * It is also responsible for native ad expiration.
 */
public class CacheManager {
    private static final int NATIVE_AD_EXPIRY_TIMEOUT = 300000;
    private static HashMap<String, String> savedValues = new HashMap<>();
    private static HashMap<String, Long> expiryIntervalMap = new HashMap<>();
    private static HashMap<String, CacheExpiryListener> cacheExpiryListenerMap = new HashMap<>();
    private static Handler handler = new Handler(Looper.getMainLooper());

    public static String save(String content) {
        if (!TextUtils.isEmpty(content)) {
            final String cacheId = "Prebid_" + UUID.randomUUID().toString();
            savedValues.put(cacheId, content);
            handler.postDelayed(() -> {
                if (cacheExpiryListenerMap.containsKey(cacheId)) {
                    cacheExpiryListenerMap.remove(cacheId).onCacheExpired();
                }
                savedValues.remove(cacheId);
            }, getExpiryInterval(cacheId));
            return cacheId;
        } else {
            return null;
        }
    }

    private static long getExpiryInterval(String cacheId) {
        return expiryIntervalMap.containsKey(cacheId) ? expiryIntervalMap.get(cacheId) : NATIVE_AD_EXPIRY_TIMEOUT;
    }

    public static boolean isValid(String cacheId) {
        return savedValues.keySet().contains(cacheId);
    }

    @VisibleForTesting
    public static void clear() {
        savedValues.clear();
        cacheExpiryListenerMap.clear();
        expiryIntervalMap.clear();
    }

    /**
     * Return cached ad content by cache id.
     */
    @Nullable
    protected static String get(String cacheId) {
        return savedValues.remove(cacheId);
    }

    protected static void registerCacheExpiryListener(String cacheId, CacheExpiryListener expiryListener) {
        cacheExpiryListenerMap.put(cacheId, expiryListener);
    }

    public static void setExpiry(String cacheId, long exp) {
        expiryIntervalMap.put(cacheId, exp * 1000); //converting seconds into ms
    }

    interface CacheExpiryListener {
        void onCacheExpired();
    }
}
