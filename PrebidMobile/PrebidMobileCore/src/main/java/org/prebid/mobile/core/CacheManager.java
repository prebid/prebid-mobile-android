package org.prebid.mobile.core;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CacheManager {
    // This is the class that manages three cache instances, one for DFP, one for MoPub and one for SDK rendered ad
    private WebView dfpWebCache;
    private WebView mopubWebCache;
    private HashMap<String, String> sdkCache;
    private static CacheManager cache;
    private HashMap<String, Long> cachedIds = new HashMap<String, Long>();

    private CacheManager(Context context) {
        setupBidCleanUpRunnable();
        setupDFPCache(context);
        setupMoPubCache(context);
        setupSDKCache();
    }

    private void setupBidCleanUpRunnable() {
        final Handler handler = new Handler();
        final Runnable cleanCache = new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                ArrayList<String> toBeRemoved = new ArrayList<String>();
                for (String key : cachedIds.keySet()) {
                    if ((now - cachedIds.get(key)) > 270000) {
                        toBeRemoved.add(key);
                        String removeCache = "<html><script>localStorage.removeItem(\"" + key + "\");</script><html>";
                        if (dfpWebCache != null) {
                            dfpWebCache.loadDataWithBaseURL("https://pubads.g.doubleclick.net/", removeCache, "text/html", null, null);
                        }
                        if (mopubWebCache != null) {
                            mopubWebCache.loadDataWithBaseURL("http://ads.mopub.com", removeCache, "text/html", null, null);
                        }
                        if (sdkCache != null) {
                            sdkCache.remove(key);
                        }
                    }
                }
                for (String key : toBeRemoved) {
                    cachedIds.remove(key);
                }
                handler.postDelayed(this, 270000);
            }
        };
        handler.post(cleanCache);
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
        String cacheId = "local_" + UUID.randomUUID().toString();
        cachedIds.put(cacheId, System.currentTimeMillis());

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
