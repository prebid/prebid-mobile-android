package org.prebid.mobile.core;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.WebView;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class CacheManager {
    // This is the class that manages three cache instances, one for DFP, one for MoPub and one for SDK rendered ad
    private WebView dfpWebCache;
    private HashMap<String, String> sdkCache;
    private static CacheManager cache;

    private CacheManager(Context context) {
        setupBidCleanUpRunnable();
        setupWebCache(context);
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

    private void setupBidCleanUpRunnable() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable cleanCache = new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                removeCache(now);
                handler.postDelayed(this, 270000);
            }
        };
        handler.post(cleanCache);
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

        String cacheId = "Prebid_" + RandomStringUtils.randomAlphabetic(8) + "_" + String.valueOf(System.currentTimeMillis());

        if ("html".equals(format)) {
            saveCacheForWeb(cacheId, bid);
        } else if ("demand_sdk".equals(format)) {
            saveCacheForSDK(cacheId, bid);
        }
        return cacheId;
    }

    public void getCache(String cacheId, CacheListener listener) {
        JSONObject jsonObject = new JSONObject();
        if (!TextUtils.isEmpty(cacheId) && listener != null) {
            if (cacheId.startsWith("Prebid_")) {
                // only get cache for demand_sdk type bid, since html bid will be retrieved by pbm.js
                try {
                    String cachedBid = getCacheForSDK(cacheId);
                    if (!TextUtils.isEmpty(cachedBid)) {
                        jsonObject = new JSONObject(cachedBid);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listener.onResponded(jsonObject);
            } else {
                CacheService cs = new CacheService(listener, cacheId);
                cs.execute();
            }
        }
    }


    private void setupWebCache(final Context context) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                dfpWebCache = new WebView(context);
                dfpWebCache.getSettings().setDomStorageEnabled(true);
                dfpWebCache.getSettings().setJavaScriptEnabled(true);
            }
        });
    }

    private void saveCacheForWeb(final String cacheId, final String bid) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                String escapedBid = StringEscapeUtils.escapeEcmaScript(bid);
                String result = "<html><script> localStorage.setItem('" + cacheId + "', '" + escapedBid + "');</script></html>";
                if (dfpWebCache != null) {
                    dfpWebCache.loadDataWithBaseURL("https://pubads.g.doubleclick.net", result, "text/html", null, null);
                }
            }
        });
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

    static class CacheService extends AsyncTask<Object, Object, JSONObject> {

        private String cacheId;
        private CacheListener listener;

        CacheService(CacheListener listener, String cacheID) {
            this.listener = listener;
            this.cacheId = cacheID;
        }

        @Override
        protected JSONObject doInBackground(Object... objects) {
            try {
                StringBuilder sb = new StringBuilder("http://prebid.adnxs.com/pbc/v1/cache?uuid=");
                sb.append(this.cacheId);
                URL url = new URL(sb.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // Start the connection
                conn.connect();

                // Read request response
                int httpResult = conn.getResponseCode();

                if (httpResult == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    reader.close();
                    is.close();
                    String result = builder.toString();
                    LogUtil.i(String.format("For cache id %s, Prebid cache returned response %s", cacheId, result));
                    JSONObject response = new JSONObject(result);
                    return response;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (listener != null) {
                listener.onResponded(jsonObject);
            }
        }


    }

    public interface CacheListener {
        void onResponded(JSONObject jsonObject);
    }

}
