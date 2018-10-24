package org.prebid.mobile;


import java.util.ArrayList;
import java.util.HashSet;

public class RequestParams {
    private String configId = "";
    private AdType adType = AdType.BANNER;
    private HashSet<AdSize> sizes = new HashSet<>();
    private ArrayList<String> keywords;

    RequestParams(String configId, AdType adType, HashSet<AdSize> sizes, ArrayList<String> keywords) {
        this.configId = configId;
        this.adType = adType;
        this.sizes = sizes; // for Interstitial this will be null, will use screen width & height in the request
        this.keywords = keywords;
    }

    public String getConfigId() {
        return this.configId;
    }

    public AdType getAdType() {
        return this.adType;
    }

    public HashSet<AdSize> getAdSizes() {
        return this.sizes;
    }

    public ArrayList<String> getKeywords() {
        return keywords;
    }

}
