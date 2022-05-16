package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertNotNull
import org.junit.Test
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

class VideoRewardedAdsTest : BaseAdsTest() {
    @Test
    fun inAppVideoRewardedShouldBeDisplayed() {
        testAd(TestConstants.IN_APP, TestConstants.VIDEO_REWARDED)
    }

    /*@Test
    fun gamVideoRewardedShouldBeDisplayed(){
        testAd(TestConstants.GAM, TestConstants.DISPLAY_INTERSTITIAL)
    }*/

   /* @Test
    fun inAppGamVideoRewardedShouldBeDisplayed() {
        testAd(TestConstants.IN_APP_GAM, TestConstants.VIDEO_REWARDED)
    }*/

    @Test
    fun inAppAdMobVideoRewardedShouldBeDisplayed() {
        testAd(TestConstants.IN_APP_ADMOB, TestConstants.VIDEO_REWARDED)
    }

    override fun checkAd() {
        val ad = By.res(packageName, "exo_subtitles")
        val endCard = By.text("Pbs_intestitial_320x480")
        val closeButton = By.res(packageName, "iv_close_interstitial")

        val findAd = device.wait(Until.findObject(ad), timeout)
        val findEndCard = device.wait(Until.findObject(endCard), timeout * 4)
        val findCloseButton = device.wait(Until.findObject(closeButton), timeout)

        assertNotNull(findAd)
        assertNotNull(findEndCard)
        assertNotNull(findCloseButton)
    }
}