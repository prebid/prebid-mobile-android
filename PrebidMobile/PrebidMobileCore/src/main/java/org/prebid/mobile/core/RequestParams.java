package org.prebid.mobile.core;


import java.util.ArrayList;

public class RequestParams {
    private String configId = "";
    private AdType adType = AdType.BANNER;
    private ArrayList<AdSize> sizes = new ArrayList<>();

    RequestParams(String configId, AdType adType, ArrayList<AdSize> sizes) {
        this.configId = configId;
        this.adType = adType;
        if (this.adType.equals(AdType.INTERSTITIAL)) {
            this.sizes = new ArrayList<>();
        } else {
            this.sizes = sizes;
        }
    }

    public String getConfigId() {
        return this.configId;
    }

    public AdType getAdType() {
        return this.adType;
    }

    public ArrayList<AdSize> getAdSizes() {
        return this.sizes;
    }

}
