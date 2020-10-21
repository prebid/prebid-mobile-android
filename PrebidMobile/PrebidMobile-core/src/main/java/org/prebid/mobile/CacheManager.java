package org.prebid.mobile;

import android.os.Handler;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.UUID;

public class CacheManager {
    private static HashMap<String, String> savedValues = new HashMap<>();
    private static Handler handler = new Handler();

    public static String save(String content) {
        if (!TextUtils.isEmpty(content)) {
            final String cacheId = "Prebid_" + UUID.randomUUID().toString();
            savedValues.put(cacheId, content);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    savedValues.remove(cacheId);
                }
            }, 300000);
            return cacheId;
        } else {
            return null;
        }
    }

    public static boolean isValid(String cacheId) {
        return savedValues.keySet().contains(cacheId);
    }

    public static String get(String cacheId) {
        return savedValues.remove(cacheId);
    }
}