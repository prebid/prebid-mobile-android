package org.prebid.mobile;


import java.util.ArrayList;
import java.util.HashMap;

class RequestParams {
    private String configId = "";
    private AdType adType = AdType.BANNER;
    private ArrayList<AdSize> sizes = new ArrayList<>();
    private boolean localCache = false;
    private HashMap<String, String> keywords;

    RequestParams(String configId, AdType adType, ArrayList<AdSize> sizes, boolean localCache) {
        this.configId = configId;
        this.adType = adType;
        this.sizes = sizes; // for Interstitial this will be null, will use screen width & height in the request
        this.localCache = localCache;
        this.keywords = new HashMap<>();
    }

    String getConfigId() {
        return this.configId;
    }

    AdType getAdType() {
        return this.adType;
    }

    ArrayList<AdSize> getAdSizes() {
        return this.sizes;
    }

    boolean useLocalCache() {
        return localCache;
    }

    HashMap<String, String> getKeywords() {
        return keywords;
    }

    void addKeyword(String key, String value) {
        keywords.put(key, value);
    }


}
