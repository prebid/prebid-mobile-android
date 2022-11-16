package org.prebid.mobile.prebidkotlindemo.testcases

enum class IntegrationKind(
    val adServer: String
) {

    GAM_RENDERING("GAM (Rendering API)"),
    GAM_ORIGINAL("GAM (Original API)"),
    MAX("MAX"),
    ADMOB("AdMob"),
    NO_AD_SERVER("In-App (No Ad Server)");

}