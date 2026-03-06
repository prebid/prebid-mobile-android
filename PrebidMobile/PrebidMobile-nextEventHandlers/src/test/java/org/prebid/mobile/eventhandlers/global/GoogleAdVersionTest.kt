package org.prebid.mobile.eventhandlers.global

import com.google.android.libraries.ads.mobile.sdk.MobileAds
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.prebid.mobile.PrebidMobile
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class GoogleAdVersionTest {
    @Test
    fun checkIfLastVersionUsed() {
        val currentVersion = MobileAds.getVersion().toString()
        Assert.assertEquals(
            "Google Next-Gen SDK was updated to " + currentVersion + "! " +
                "Please test Prebid SDK with the new version, resolve compilation problems, rewrite deprecated code. " +
                "After testing you must update PrebidMobile.TESTED_GOOGLE_SDK_VERSION " +
                "and also update version in publishing XML files (scripts/Maven/*.xml.)",
            currentVersion,
            PrebidMobile.TESTED_GOOGLE_NEXT_GEN_SDK_VERSION
        )
    }
}
