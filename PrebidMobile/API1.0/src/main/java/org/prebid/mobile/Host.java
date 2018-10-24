package org.prebid.mobile;

public enum Host {
    APPNEXUS("https://prebid.adnxs.com/pbs/v1/openrtb2/auction"),
    RUBICON("https://prebid-server.rubiconproject.com/openrtb2/auction"),
    CUSTOM("");

    private String url;

    Host(String url) {
        this.url = url;
    }

    public String getHostUrl() {
        return this.url;
    }


    public void setHostUrl(String url) {
        if (this.equals(CUSTOM)) {
            this.url = url;
        }
    }
}
