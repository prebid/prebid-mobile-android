package org.prebid.mobile.javademo.testcases;

public enum IntegrationKind {

    GAM_RENDERING("GAM (Rendering API)"),
    GAM_ORIGINAL("GAM (Original API)"),
    NO_AD_SERVER("In-App (No Ad Server)"),
    ADMOB("AdMob"),
    MAX("AppLovin MAX");

    private final String adServer;

    IntegrationKind(String adServer) {
        this.adServer = adServer;
    }

    public String getAdServer() {
        return adServer;
    }
}
