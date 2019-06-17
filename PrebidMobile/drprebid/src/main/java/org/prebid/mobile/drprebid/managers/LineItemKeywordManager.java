package org.prebid.mobile.drprebid.managers;

import android.text.TextUtils;

import org.prebid.mobile.drprebid.model.AdFormat;
import org.prebid.mobile.drprebid.model.AdSize;
import org.prebid.mobile.drprebid.model.PrebidServer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LineItemKeywordManager {
    public static final String KEYWORD_PRICE = "hb_pb";
    public static final String KEYWORD_CACHE_ID = "hb_cache_id";
    public static final String KEYWORD_SIZE_KEY = "hb_size";
    public static final String KEYWORD_REQUEST_ID = "hb_dr_prebid";
    public static final String KEYWORD_DIVIDER = ":";
    public static final String KEYWORD_COMMA = ",";

    public static final String FAKE_CACHE_ID = "FakeCacheId_ShouldNotAffectTest";

    public static final String CACHE_ENDPOINT_APPNEXUS = "https://prebid.adnxs.com/pbc/v1/cache";
    public static final String CACHE_ENDPOINT_RUBICON = "https://prebid-server.rubiconproject.com/cache";
    public static final String CACHE_ENDPOINT_CUSTOM = "";

    private Map<String, String> mAppNexusCacheIds;
    private Map<String, String> mRubiconCacheIds;
    private Map<String, String> mCustomServerCacheIds;

    private static LineItemKeywordManager sInstance;

    private LineItemKeywordManager() {

    }

    public static LineItemKeywordManager getInstance() {
        if (sInstance == null) {
            sInstance = new LineItemKeywordManager();
        }
        return sInstance;
    }

    public void refreshCacheIds() {
        mAppNexusCacheIds = new HashMap<>();
        mRubiconCacheIds = new HashMap<>();
        mCustomServerCacheIds = new HashMap<>();
    }

    public String getStringKeywords(float bidPrice, AdSize adSize, AdFormat adFormat, PrebidServer prebidServer) {
        StringBuilder stringBuilder = new StringBuilder();

        if (prebidServer == PrebidServer.RUBICON) {
            if (mRubiconCacheIds != null) {
                if (adFormat == AdFormat.INTERSTITIAL) {
                    String cacheId = mRubiconCacheIds.get("320x480");
                    if (!TextUtils.isEmpty(cacheId)) {
                        stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(cacheId);
                    }
                } else {
                    String cacheId = mRubiconCacheIds.get(String.format(Locale.ENGLISH, "%dx%d", adSize.getWidth(), adSize.getHeight()));
                    if (!TextUtils.isEmpty(cacheId)) {
                        stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(cacheId);
                    }
                }
            } else {
                stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(FAKE_CACHE_ID);
            }
        } else if (prebidServer == PrebidServer.APPNEXUS) {
            if (mAppNexusCacheIds != null) {
                if (adFormat == AdFormat.INTERSTITIAL) {
                    String cacheId = mAppNexusCacheIds.get("320x480");
                    if (!TextUtils.isEmpty(cacheId)) {
                        stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(cacheId);
                    }
                } else {
                    String cacheId = mAppNexusCacheIds.get(String.format(Locale.ENGLISH, "%dx%d", adSize.getWidth(), adSize.getHeight()));
                    if (!TextUtils.isEmpty(cacheId)) {
                        stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(cacheId);
                    }
                }
            } else {
                stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(FAKE_CACHE_ID);
            }
        } else {
            if (mCustomServerCacheIds != null) {
                if (adFormat == AdFormat.INTERSTITIAL) {
                    String cacheId = mCustomServerCacheIds.get("320x480");
                    if (!TextUtils.isEmpty(cacheId)) {
                        stringBuilder.append(KEYWORD_CACHE_ID).append(KEYWORD_DIVIDER).append(cacheId);
                    }
                } else {
                    String cacheId = mCustomServerCacheIds.get(String.format(Locale.ENGLISH, "%dx%d", adSize.getWidth(), adSize.getHeight()));
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
}
