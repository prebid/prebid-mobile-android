package org.prebid.mobile.core;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebView;

import java.util.HashMap;
import java.util.UUID;

public class CacheManager {
    // This is the class that manages three cache instances, one for DFP, one for MoPub and one for SDK rendered ad
    private WebView dfpWebCache;
    private WebView mopubWebCache;
    private HashMap<String, String> sdkCache;
    private static CacheManager cache;

    private CacheManager(Context context) {
        setupDFPCache(context);
        setupMoPubCache(context);
        setupSDKCache();
    }

    static void init(Context context) {
        if (cache == null) {
            cache = new CacheManager(context);
        }
    }

    public static CacheManager getCacheManager() {
        return cache;
    }

    public String saveCache(String bid, String format) {
        if (TextUtils.isEmpty(bid)) {
            return null;
        }
        String cacheId = UUID.randomUUID().toString();
        if ("html".equals(format)) {
            saveCacheForDFP(cacheId, bid);
            saveCacheForMoPub(cacheId, bid);
        } else if ("demand_sdk".equals(format)) {
            saveCacheForSDK(cacheId, bid);
        }
        return cacheId;
    }

    private void setupDFPCache(Context context) {
        dfpWebCache = new WebView(context);
        dfpWebCache.getSettings().setDomStorageEnabled(true);
    }

    private void saveCacheForDFP(String cacheId, String bid) {
        String result = "<html><script> localStorage.setItem(\"" + cacheId + "\", \"" + bid + "\");</script></html>";
        dfpWebCache.loadDataWithBaseURL("https://pubads.g.doubleclick.net/", result, "text/html", null, null);
    }


    private void setupMoPubCache(Context context) {
        mopubWebCache = new WebView(context);
        mopubWebCache.getSettings().setDomStorageEnabled(true);

    }

    private void saveCacheForMoPub(String cacheId, String bid) {
        String result = "<html><script> localStorage.setItem(\"" + cacheId + "\", \"" + bid + "\");</script></html>";
        mopubWebCache.loadDataWithBaseURL("http://ads.mopub.com", result, "text/html", null, null);

    }


    private void setupSDKCache() {
        if (sdkCache == null) {
            sdkCache = new HashMap<String, String>();
        }
    }

    private void saveCacheForSDK(String cacheId, String bid) {
        sdkCache.put(cacheId, bid);
    }

    private String getCacheForSDK(String cacheId) {
        return sdkCache.remove(cacheId);
    }
}
