package org.prebid.mobile.prebidkotlindemo.testcases

enum class IntegrationKind(
    val adServer: String
) {

    GAM_RENDERING("GAM"),
    MAX_RENDERING("MAX"),
    AD_MOB_RENDERING("AdMob"),
    NO_AD_SERVER_RENDERING("In-App"),
    GAM_ORIGINAL("GAM (Original API)");

}