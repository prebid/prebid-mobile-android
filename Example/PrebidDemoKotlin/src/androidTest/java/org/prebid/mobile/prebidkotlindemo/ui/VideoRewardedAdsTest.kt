package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertNotNull
import org.junit.Test
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

class VideoRewardedAdsTest : BaseAdsTest() {

    @Test
    fun videoRewardedAdsShouldBeDisplayed(){
        testAd(TestConstants.IN_APP, TestConstants.VIDEO_REWARDED)
        testAd(TestConstants.IN_APP_ADMOB, TestConstants.VIDEO_REWARDED)
        displayErrorMessages()
    }

    override fun checkAd(adServer: String) {
        val ad = By.res(packageName, "exo_subtitles")
        val endCard = By.text("Pbs_intestitial_320x480")
        val closeButton = By.res(packageName, "iv_close_interstitial")

        val findAd = device.wait(Until.findObject(ad), timeout)
        assertNotNull(findAd)

        val findEndCard = device.wait(Until.findObject(endCard), timeout * 3)
        assertNotNull(findEndCard)

        val findCloseButton = device.wait(Until.findObject(closeButton), timeout)
        assertNotNull(findCloseButton)
    }

    override fun teardownAd(adServer: String) {
        val closeButton = By.res(packageName, "iv_close_interstitial")
        device.wait(Until.findObject(closeButton), timeout).click()
        Thread.sleep(1000)
        device.pressBack()
    }

}