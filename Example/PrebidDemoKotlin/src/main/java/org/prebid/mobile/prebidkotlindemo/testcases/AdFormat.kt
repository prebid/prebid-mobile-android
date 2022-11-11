package org.prebid.mobile.prebidkotlindemo.testcases

enum class AdFormat(
    val description: String
) {

    BANNER_DISPLAY("Display Banner"),
    BANNER_VIDEO("Video Banner"),
    INTERSTITIAL_DISPLAY("Display Interstitial"),
    INTERSTITIAL_VIDEO("Video Interstitial"),
    REWARDED("Rewarded Video"),
    IN_STREAM_VIDEO("In-stream Video"),
    NATIVE("Native"),

}