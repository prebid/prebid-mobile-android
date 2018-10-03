package org.prebid.mobile.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class CacheManager {
    // This is the class that manages three cache instances, one for DFP, one for MoPub and one for SDK rendered ad
    private WebView dfpWebCache;
    private HashMap<String, String> sdkCache;
    private static CacheManager cache;

    private CacheManager(Context context) {
        Handler handler = new Handler(Looper.getMainLooper());
        setupBidCleanUpRunnable(handler);
        setupWebCache(context, handler);
        setupSDKCache();
    }

    public static void init(Context context) {
        if (cache == null) {
            cache = new CacheManager(context);
        }
    }

    public static CacheManager getCacheManager() {
        return cache;
    }

    private void setupBidCleanUpRunnable(final Handler handler) {
        handler.post(new BidCleanUpRunnable(this, handler));
    }

    private void removeCache(long now) {
        String removeWebCache = "<html><script>var currentTime = " + String.valueOf(now) + ";" +
                "\nvar toBeDeleted = [];\n" +
                "\n" +
                "for(i = 0; i< localStorage.length; i ++) {\n" +
                "\tif (localStorage.key(i).startsWith('Prebid_')) {\n" +
                "\t\tcreatedTime = localStorage.key(i).split('_')[2];\n" +
                "\t\tif (( currentTime - createdTime) > 270000){\n" +
                "\t\t\ttoBeDeleted.push(localStorage.key(i));\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}\n" +
                "\n" +
                "for ( i = 0; i< toBeDeleted.length; i ++) {\n" +
                "\tlocalStorage.removeItem(toBeDeleted[i]);\n" +
                "}</script></html>";
        if (dfpWebCache != null) {
            dfpWebCache.loadDataWithBaseURL("https://pubads.g.doubleclick.net", removeWebCache, "text/html", null, null);
        }
        if (sdkCache != null) {
            ArrayList<String> toBeDeleted = new ArrayList<String>();
            for (String key : sdkCache.keySet()) {
                long createdTime = Long.valueOf(key.split("_")[2]);
                if ((now - createdTime) > 270000) {
                    toBeDeleted.add(key);
                }
            }
            for (String key : toBeDeleted) {
                sdkCache.remove(key);
            }
        }
    }

    public String saveCache(String bid, String format) {
        if (TextUtils.isEmpty(bid)) {
            return null;
        }

        String cacheId = "Prebid_" + StringUtils.randomLowercaseAlphabetic(8) + "_" + String.valueOf(System.currentTimeMillis());

        if ("html".equals(format)) {
            saveCacheForWeb(cacheId, bid);
        } else if ("demand_sdk".equals(format)) {
            saveCacheForSDK(cacheId, bid);
        }
        return cacheId;
    }


    private void setupWebCache(final Context context, final Handler handler) {
        handler.postAtFrontOfQueue(new WebCacheInitializer(this, context));
    }

    private void saveCacheForWeb(final String cacheId, final String bid) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postAtFrontOfQueue(new SaveCacheForWebRunnable(this, cacheId, bid));
    }

    private void setupSDKCache() {
        if (sdkCache == null) {
            sdkCache = new HashMap<String, String>();
        }
    }

    private void saveCacheForSDK(String cacheId, String bid) {
        if (sdkCache != null) {
            sdkCache.put(cacheId, bid);
        }
    }

    private String getCacheForSDK(String cacheId) {
        if (sdkCache != null) {
            return sdkCache.remove(cacheId);
        }
        return null;
    }

    private static class BidCleanUpRunnable implements Runnable {
        private final WeakReference<CacheManager> cacheManagerWeakRef;
        private final Handler handler;

        BidCleanUpRunnable(final CacheManager cacheManager, final Handler handler) {
            cacheManagerWeakRef = new WeakReference<>(cacheManager);
            this.handler = handler;
        }

        @Override
        public void run() {
            CacheManager cacheManager = cacheManagerWeakRef.get();
            if (cacheManager == null) {
                return;
            }
            cacheManager.removeCache(System.currentTimeMillis());
            handler.postDelayed(this, 270000);
        }
    }

    private static class WebCacheInitializer implements Runnable {
        private final WeakReference<CacheManager> cacheManagerWeakRef;
        private final WeakReference<Context> contextWeakRef;

        WebCacheInitializer(final CacheManager cacheManager,final  Context context) {
            cacheManagerWeakRef = new WeakReference<>(cacheManager);
            contextWeakRef = new WeakReference<>(context);
        }

        @Override
        public void run() {
            CacheManager cacheManager = cacheManagerWeakRef.get();
            if (cacheManager == null) {
                return;
            }

            Context context = contextWeakRef.get();
            if (context == null) {
                return;
            }

            try {
                cacheManager.dfpWebCache = new WebView(context);
                WebSettings webSettings = cacheManager.dfpWebCache.getSettings();
                if (webSettings != null) {
                    webSettings.setDomStorageEnabled(true);
                    webSettings.setJavaScriptEnabled(true);
                }
            } catch (Throwable t) {
                // possible AndroidRuntime thrown at android.webkit.WebViewFactory.getFactoryClass
                // stemming from WebView's constructor, manifests itself in Android 5.0 and 5.1.
            }
        }
    }

    private static class SaveCacheForWebRunnable implements Runnable {
        private final WeakReference<CacheManager> cacheManagerWeakRef;
        private final String cacheId;
        private final String bid;

        SaveCacheForWebRunnable(CacheManager cacheManager, final String cacheId, final String bid) {
            cacheManagerWeakRef = new WeakReference<>(cacheManager);
            this.cacheId = cacheId;
            this.bid = bid;
        }

        @Override
        public void run() {
            CacheManager cacheManager = cacheManagerWeakRef.get();
            if (cacheManager == null) {
                return;
            }
            if (cacheManager.dfpWebCache == null) {
                return;
            }
            String escapedBid = StringUtils.escapeEcmaScript(bid);
            String result = "<html><script> localStorage.setItem('" + cacheId + "', '" + escapedBid + "');</script></html>";
            cacheManager.dfpWebCache.loadDataWithBaseURL("https://pubads.g.doubleclick.net", result, "text/html", null, null);
        }
    }
}
