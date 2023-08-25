package org.prebid.mobile.configuration;

public class PBSConfig {
    int bannerTimeout;
    int preRenderTimeout;

    public PBSConfig(int bannerTimeout, int preRenderTimeout) {
        this.bannerTimeout = bannerTimeout;
        this.preRenderTimeout = preRenderTimeout;
    }

    public int getBannerTimeout() {
        return bannerTimeout;
    }

    public void setBannerTimeout(int bannerTimeout) {
        this.bannerTimeout = bannerTimeout;
    }

    public int getPreRenderTimeout() {
        return preRenderTimeout;
    }

    public void setPreRenderTimeout(int preRenderTimeout) {
        this.preRenderTimeout = preRenderTimeout;
    }
}
