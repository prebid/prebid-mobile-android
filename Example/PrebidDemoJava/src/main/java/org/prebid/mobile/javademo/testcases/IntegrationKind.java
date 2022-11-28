package org.prebid.mobile.javademo.testcases;

public enum IntegrationKind {

    GAM_ORIGINAL("GAM (Original API)"),
    GAM_RENDERING("GAM (Rendering API)"),
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
