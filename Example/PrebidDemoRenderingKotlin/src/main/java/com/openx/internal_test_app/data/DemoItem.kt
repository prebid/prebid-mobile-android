package com.openx.internal_test_app.data

import android.os.Bundle

data class DemoItem(val label: String, val action: Int = -1, val tag: List<Tag> = listOf(), val bundle: Bundle? = null)

enum class Tag(val tagName: String) {
    REMOTE("Remote"),
    MOCK("Mock"),

    BANNER("Banner"),
    MRAID("MRAID"),
    VIDEO("Video"),
    INTERSTITIAL("Interstitial"),
    NATIVE("Native"),

    APOLLO("Apollo"),
    GAM("GAM"),
    MOPUB("MoPub"),

    ALL("All")
}