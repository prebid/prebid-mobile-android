package org.prebid.mobile.drprebid.managers;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.drprebid.model.AdFormat;
import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.PrebidServer;
import org.prebid.mobile.drprebid.model.PrebidServerSettings;
import org.prebid.mobile.drprebid.util.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LineItemKeywordManager {
    public static final String TAG = LineItemKeywordManager.class.getSimpleName();

    public static final String KEYWORD_PRICE = "hb_pb";
    public static final String KEYWORD_CACHE_ID = "hb_cache_id";
    public static final String KEYWORD_SIZE_KEY = "hb_size";
    public static final String KEYWORD_REQUEST_ID = "hb_dr_prebid";
    public static final String KEYWORD_DIVIDER = ":";
    public static final String KEYWORD_COMMA = ",";

    public static final String FAKE_CACHE_ID = "FakeCacheId_ShouldNotAffectTest";

    public static final String CACHE_ENDPOINT_APPNEXUS = "https://prebid.adnxs.com/pbc/v1/cache";
    public static final String CACHE_ENDPOINT_RUBICON = "https://prebid-server.rubiconproject.com/cache";

    public static final String CREATIVE_300x250 = "{\"id\":\"7438652069000399098\",\"impid\":\"Home\",\"price\":0.5,\"adm\":\"<script type=\\\"text/javascript\\\">document.write('<a href=\\\"http://prebid.org\\\" target=\\\"_blank\\\"><img width=\\\"300\\\" height=\\\"250\\\" style=\\\"border-style: none\\\" src=\\\"https://vcdn.adnxs.com/p/creative-image/27/c0/52/67/27c05267-5a6d-4874-834e-18e218493c32.png\\\"/></a>');</script>\",\"adid\":\"29681110\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=29681110\",\"cid\":\"958\",\"crid\":\"29681110\",\"w\":300,\"h\":250}";
    public static final String CREATIVE_300x600 = "{\"id\":\"7438652069000399098\",\"impid\":\"Home\",\"price\":0.5,\"adm\":\"<script type=\\\"text/javascript\\\">document.write('<a href=\\\"http://prebid.org\\\" target=\\\"_blank\\\"><img width=\\\"300\\\" height=\\\"600\\\" style=\\\"border-style: none\\\" src=\\\"https://vcdn.adnxs.com/p/creative-image/27/c0/52/67/27c05267-5a6d-4874-834e-18e218493c32.png\\\"/></a>');</script>\",\"adid\":\"29681110\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=29681110\",\"cid\":\"958\",\"crid\":\"29681110\",\"w\":300,\"h\":600}";
    public static final String CREATIVE_320x50 = "{\"id\":\"7438652069000399098\",\"impid\":\"Home\",\"price\":0.5,\"adm\":\"<script type=\\\"text/javascript\\\">document.write('<a href=\\\"http://prebid.org\\\" target=\\\"_blank\\\"><img width=\\\"320\\\" height=\\\"50\\\" style=\\\"border-style: none\\\" src=\\\"https://vcdn.adnxs.com/p/creative-image/27/c0/52/67/27c05267-5a6d-4874-834e-18e218493c32.png\\\"/></a>');</script>\",\"adid\":\"29681110\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=29681110\",\"cid\":\"958\",\"crid\":\"29681110\",\"w\":320,\"h\":50}";
    public static final String CREATIVE_320x100 = "{\"id\":\"7438652069000399098\",\"impid\":\"Home\",\"price\":0.5,\"adm\":\"<script type=\\\"text/javascript\\\">document.write('<a href=\\\"http://prebid.org\\\" target=\\\"_blank\\\"><img width=\\\"320\\\" height=\\\"100\\\" style=\\\"border-style: none\\\" src=\\\"https://vcdn.adnxs.com/p/creative-image/27/c0/52/67/27c05267-5a6d-4874-834e-18e218493c32.png\\\"/></a>');</script>\",\"adid\":\"29681110\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=29681110\",\"cid\":\"958\",\"crid\":\"29681110\",\"w\":320,\"h\":100}";
    public static final String CREATIVE_320x480 = "{\"id\":\"7438652069000399098\",\"impid\":\"Home\",\"price\":0.5,\"adm\":\"<script type=\\\"text/javascript\\\">document.write('<a href=\\\"http://prebid.org\\\" target=\\\"_blank\\\"><img width=\\\"320\\\" height=\\\"480\\\" style=\\\"border-style: none\\\" src=\\\"https://vcdn.adnxs.com/p/creative-image/27/c0/52/67/27c05267-5a6d-4874-834e-18e218493c32.png\\\"/></a>');</script>\",\"adid\":\"29681110\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=29681110\",\"cid\":\"958\",\"crid\":\"29681110\",\"w\":320,\"h\":480}";
    public static final String CREATIVE_728x90 = "{\"id\":\"7438652069000399098\",\"impid\":\"Home\",\"price\":0.5,\"adm\":\"<script type=\\\"text/javascript\\\">document.write('<a href=\\\"http://prebid.org\\\" target=\\\"_blank\\\"><img width=\\\"7280\\\" height=\\\"90\\\" style=\\\"border-style: none\\\" src=\\\"https://vcdn.adnxs.com/p/creative-image/27/c0/52/67/27c05267-5a6d-4874-834e-18e218493c32.png\\\"/></a>');</script>\",\"adid\":\"29681110\",\"adomain\":[\"appnexus.com\"],\"iurl\":\"https://nym1-ib.adnxs.com/cr?id=29681110\",\"cid\":\"958\",\"crid\":\"29681110\",\"w\":728,\"h\":90}";

    private Map<String, String> appNexusCacheIds;
    private Map<String, String> rubiconCacheIds;
    private Map<String, String> customServerCacheIds;

    private static LineItemKeywordManager instance;

    private LineItemKeywordManager() {

    }

    public static LineItemKeywordManager getInstance() {
        if (instance == null) {
            instance = new LineItemKeywordManager();
        }
        return instance;
    }

    public void refreshCacheIds(Context context) {
        appNexusCacheIds = new HashMap<>();
        rubiconCacheIds = new HashMap<>();
        customServerCacheIds = new HashMap<>();

        PrebidServerSettings serverSettings = SettingsManager.getInstance(context).getPrebidServerSettings();
        String customServerCacheUrl = null;
        if (TextUtils.isEmpty(serverSettings.getCustomPrebidServerUrl())) {
            Uri.Builder uriBuilder = Uri.parse(serverSettings.getCustomPrebidServerUrl()).buildUpon();
            uriBuilder.appendPath("cache");

            customServerCacheUrl = uriBuilder.build().toString();
        }

        try {
            JSONObject creative300x250 = new JSONObject(CREATIVE_300x250);
            JSONObject content300x250 = new JSONObject();
            content300x250.put("type", "json");
            content300x250.put("value", creative300x250);

            JSONObject creative300x600 = new JSONObject(CREATIVE_300x600);
            JSONObject content300x600 = new JSONObject();
            content300x600.put("type", "json");
            content300x600.put("value", creative300x600);

            JSONObject creative320x50 = new JSONObject(CREATIVE_320x50);
            JSONObject content320x50 = new JSONObject();
            content320x50.put("type", "json");
            content320x50.put("value", creative320x50);

            JSONObject creative320x100 = new JSONObject(CREATIVE_320x100);
            JSONObject content320x100 = new JSONObject();
            content320x100.put("type", "json");
            content320x100.put("value", creative320x100);

            JSONObject creative320x480 = new JSONObject(CREATIVE_320x480);
            JSONObject content320x480 = new JSONObject();
            content320x480.put("type", "json");
            content320x480.put("value", creative320x480);

            JSONObject creative728x90 = new JSONObject(CREATIVE_728x90);
            JSONObject content728x90 = new JSONObject();
            content728x90.put("type", "json");
            content728x90.put("value", creative728x90);

            JSONArray contentArray = new JSONArray();
            contentArray.put(content300x250);
            contentArray.put(content300x600);
            contentArray.put(content320x50);
            contentArray.put(content320x100);
            contentArray.put(content320x480);
            contentArray.put(content728x90);

            JSONObject postObject = new JSONObject();
            postObject.put("puts", contentArray);

            String postBody = postObject.toString();

            MediaType jsonMediaType = MediaType.parse("application/json");
            OkHttpClient client = new OkHttpClient.Builder().build();
            RequestBody body = RequestBody.create(jsonMediaType, postBody);

            Request appNexusRequest = new Request.Builder()
                    .url(CACHE_ENDPOINT_APPNEXUS)
                    .post(body)
                    .build();

            Request rubiconRequest = new Request.Builder()
                    .url(CACHE_ENDPOINT_RUBICON)
                    .post(body)
                    .build();

            client.newCall(appNexusRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.body() != null) {
                        InputStream inputStream = response.body().byteStream();
                        String responseText = IOUtil.getStringFromStream(inputStream);
                        inputStream.close();

                        try {
                            JSONObject responseJson = new JSONObject(responseText);
                            JSONArray uuids = responseJson.getJSONArray("responses");

                            appNexusCacheIds.put("300x250", uuids.getJSONObject(0).getString("uuid"));
                            appNexusCacheIds.put("300x600", uuids.getJSONObject(1).getString("uuid"));
                            appNexusCacheIds.put("320x50", uuids.getJSONObject(2).getString("uuid"));
                            appNexusCacheIds.put("320x100", uuids.getJSONObject(3).getString("uuid"));
                            appNexusCacheIds.put("320x480", uuids.getJSONObject(4).getString("uuid"));
                            appNexusCacheIds.put("728x90", uuids.getJSONObject(5).getString("uuid"));
                        } catch (JSONException exception) {
                            Log.e(TAG, exception.getMessage());
                        }
                    }
                }
            });

            client.newCall(rubiconRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.body() != null) {
                        InputStream inputStream = response.body().byteStream();
                        String responseText = IOUtil.getStringFromStream(inputStream);
                        inputStream.close();

                        try {
                            JSONObject responseJson = new JSONObject(responseText);
                            JSONArray uuids = responseJson.getJSONArray("responses");

                            rubiconCacheIds.put("300x250", uuids.getJSONObject(0).getString("uuid"));
                            rubiconCacheIds.put("300x600", uuids.getJSONObject(1).getString("uuid"));
                            rubiconCacheIds.put("320x50", uuids.getJSONObject(2).getString("uuid"));
                            rubiconCacheIds.put("320x100", uuids.getJSONObject(3).getString("uuid"));
                            rubiconCacheIds.put("320x480", uuids.getJSONObject(4).getString("uuid"));
                            rubiconCacheIds.put("728x90", uuids.getJSONObject(5).getString("uuid"));
                        } catch (JSONException exception) {
                            Log.e(TAG, exception.getMessage());
                        }
                    }
                }
            });

            if (!TextUtils.isEmpty(customServerCacheUrl)) {
                Request customRequest = new Request.Builder()
                        .url(customServerCacheUrl)
                        .post(body)
                        .build();

                client.newCall(customRequest).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.body() != null) {
                            InputStream inputStream = response.body().byteStream();
                            String responseText = IOUtil.getStringFromStream(inputStream);
                            inputStream.close();

                            try {
                                JSONObject responseJson = new JSONObject(responseText);
                                JSONArray uuids = responseJson.getJSONArray("responses");

                                customServerCacheIds.put("300x250", uuids.getJSONObject(0).getString("uuid"));
                                customServerCacheIds.put("300x600", uuids.getJSONObject(1).getString("uuid"));
                                customServerCacheIds.put("320x50", uuids.getJSONObject(2).getString("uuid"));
                                customServerCacheIds.put("320x100", uuids.getJSONObject(3).getString("uuid"));
                                customServerCacheIds.put("320x480", uuids.getJSONObject(4).getString("uuid"));
                                customServerCacheIds.put("728x90", uuids.getJSONObject(5).getString("uuid"));
                            } catch (JSONException exception) {
                                Log.e(TAG, exception.getMessage());
                            }
                        }
                    }
                });
            }
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage());
        }
    }

    public String getStringKeywords(float bidPrice, AdSize adSize, AdFormat adFormat, PrebidServer prebidServer) {
        StringBuilder stringBuilder = new StringBuilder();

        if (prebidServer == PrebidServer.RUBICON) {
            if (rubiconCacheIds != null) {
                if (adFormat == AdFormat.INTERSTITIAL) {
                    String cacheId = rubiconCacheIds.get("320x480");
                    if (!TextUtils.isEmpty(cacheId)) {
                        stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(cacheId);
                    }
                } else {
                    String cacheId = rubiconCacheIds.get(String.format(
                            Locale.ENGLISH,
                            "%dx%d",
                            adSize.getWidth(),
                            adSize.getHeight()
                    ));
                    if (!TextUtils.isEmpty(cacheId)) {
                        stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(cacheId);
                    }
                }
            } else {
                stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(FAKE_CACHE_ID);
            }
        } else if (prebidServer == PrebidServer.APPNEXUS) {
            if (appNexusCacheIds != null) {
                if (adFormat == AdFormat.INTERSTITIAL) {
                    String cacheId = appNexusCacheIds.get("320x480");
                    if (!TextUtils.isEmpty(cacheId)) {
                        stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(cacheId);
                    }
                } else {
                    String cacheId = appNexusCacheIds.get(String.format(
                            Locale.ENGLISH,
                            "%dx%d",
                            adSize.getWidth(),
                            adSize.getHeight()
                    ));
                    if (!TextUtils.isEmpty(cacheId)) {
                        stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(cacheId);
                    }
                }
            } else {
                stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(FAKE_CACHE_ID);
            }
        } else {
            if (customServerCacheIds != null) {
                if (adFormat == AdFormat.INTERSTITIAL) {
                    String cacheId = customServerCacheIds.get("320x480");
                    if (!TextUtils.isEmpty(cacheId)) {
                        stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(cacheId);
                    }
                } else {
                    String cacheId = customServerCacheIds.get(String.format(
                            Locale.ENGLISH,
                            "%dx%d",
                            adSize.getWidth(),
                            adSize.getHeight()
                    ));
                    if (!TextUtils.isEmpty(cacheId)) {
                        stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(cacheId);
                    }
                }
            } else {
                stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(FAKE_CACHE_ID);
            }
        }

        stringBuilder.append(KEYWORD_COMMA);
        stringBuilder.append(KEYWORD_PRICE).append(KEYWORD_DIVIDER).append(String.format(Locale.ENGLISH, "%.02f", bidPrice));

        stringBuilder.append(KEYWORD_COMMA);
        if (adFormat == AdFormat.INTERSTITIAL) {
            stringBuilder.append(KEYWORD_SIZE_KEY).append(KEYWORD_DIVIDER).append("320x480");
        } else {
            stringBuilder.append(KEYWORD_SIZE_KEY).append(KEYWORD_DIVIDER).append(String.format(Locale.ENGLISH, "%dx%d", adSize.getWidth(), adSize.getHeight()));
        }

        return stringBuilder.toString();
    }

    public Map<String, String> getMapKeywords(float bidPrice, AdSize adSize, AdFormat adFormat, PrebidServer prebidServer) {
        Map<String, String> keywords = new HashMap<>();

        if (prebidServer == PrebidServer.RUBICON) {
            if (rubiconCacheIds != null) {
                if (adFormat == AdFormat.INTERSTITIAL) {
                    String cacheId = rubiconCacheIds.get("320x480");
                    if (!TextUtils.isEmpty(cacheId)) {
                        keywords.put(KEYWORD_CACHE_ID, cacheId);
                    }
                } else {
                    String cacheId = rubiconCacheIds.get(String.format(
                            Locale.ENGLISH,
                            "%dx%d",
                            adSize.getWidth(),
                            adSize.getHeight()
                    ));
                    if (!TextUtils.isEmpty(cacheId)) {
                        keywords.put(KEYWORD_CACHE_ID, cacheId);
                    }
                }
            } else {
                keywords.put(KEYWORD_CACHE_ID, FAKE_CACHE_ID);
            }
        } else if (prebidServer == PrebidServer.APPNEXUS) {
            if (appNexusCacheIds != null) {
                if (adFormat == AdFormat.INTERSTITIAL) {
                    String cacheId = appNexusCacheIds.get("320x480");
                    if (!TextUtils.isEmpty(cacheId)) {
                        keywords.put(KEYWORD_CACHE_ID, cacheId);
                    }
                } else {
                    String cacheId = appNexusCacheIds.get(String.format(
                            Locale.ENGLISH,
                            "%dx%d",
                            adSize.getWidth(),
                            adSize.getHeight()
                    ));
                    if (!TextUtils.isEmpty(cacheId)) {
                        keywords.put(KEYWORD_CACHE_ID, cacheId);
                    }
                }
            } else {
                keywords.put(KEYWORD_CACHE_ID, FAKE_CACHE_ID);
            }
        } else {
            if (customServerCacheIds != null) {
                if (adFormat == AdFormat.INTERSTITIAL) {
                    String cacheId = customServerCacheIds.get("320x480");
                    if (!TextUtils.isEmpty(cacheId)) {
                        keywords.put(KEYWORD_CACHE_ID, cacheId);
                    }
                } else {
                    String cacheId = customServerCacheIds.get(String.format(
                            Locale.ENGLISH,
                            "%dx%d",
                            adSize.getWidth(),
                            adSize.getHeight()
                    ));
                    if (!TextUtils.isEmpty(cacheId)) {
                        keywords.put(KEYWORD_CACHE_ID, cacheId);
                    }
                }
            } else {
                keywords.put(KEYWORD_CACHE_ID, FAKE_CACHE_ID);
            }
        }

        keywords.put(KEYWORD_PRICE, String.format(Locale.ENGLISH, "%.02f", bidPrice));

        if (adFormat == AdFormat.INTERSTITIAL) {
            keywords.put(KEYWORD_SIZE_KEY, "320x480");
        } else {
            keywords.put(KEYWORD_SIZE_KEY, String.format(Locale.ENGLISH, "%dx%d", adSize.getWidth(), adSize.getHeight()));
        }

        return keywords;
    }
}
