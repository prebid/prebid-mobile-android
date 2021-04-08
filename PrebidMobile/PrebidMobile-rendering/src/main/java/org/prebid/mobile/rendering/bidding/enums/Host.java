package org.prebid.mobile.rendering.bidding.enums;

public enum Host {
    /**
     * URL <a href=https://prebid.adnxs.com/pbs/v1/openrtb2/auction>https://prebid.adnxs.com/pbs/v1/openrtb2/auction</a>
     */
    APPNEXUS("https://prebid.adnxs.com/pbs/v1/openrtb2/auction"),

    /**
     * URL <a href=https://prebid-server.rubiconproject.com/openrtb2/auction>https://prebid-server.rubiconproject.com/openrtb2/auction</a>
     */
    RUBICON("https://prebid-server.rubiconproject.com/openrtb2/auction"),

    CUSTOM("");

    private String mUrl;

    Host(String url) {
        mUrl = url;
    }

    public String getHostUrl() {
        return mUrl;
    }


    public void setHostUrl(String url) {
        if (this.equals(CUSTOM) && url != null) {
            mUrl = url;
        }
    }
}
