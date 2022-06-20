package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertNotNull
import org.junit.Test
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

class DisplayInterstitialAdsTest : BaseAdsTest() {
    @Test
    fun displayInterstitialAdsShouldBeDisplayed() {
        testAd(TestConstants.IN_APP, TestConstants.DISPLAY_INTERSTITIAL)
        testAd(TestConstants.GAM, TestConstants.DISPLAY_INTERSTITIAL)
        testAd(TestConstants.IN_APP_ADMOB, TestConstants.DISPLAY_INTERSTITIAL)
        testAd(TestConstants.IN_APP_GAM, TestConstants.DISPLAY_INTERSTITIAL)
    }

    @Test
    fun multiformatInterstitialAdsShouldBeDisplayed() {
        testAd(TestConstants.IN_APP, TestConstants.MULTIFORMAT_INTERSTITIAL)
    }

    override fun checkAd(adServer: String, adName: String) {
        val closeButton = By.res(packageName, "iv_close_interstitial")
        val gamCloseButton = By.desc("Interstitial close button")
        val ad = By.clazz("android.view.View")
        val videoAd = By.res(packageName, "exo_subtitles")

        val findAd = if (adName == TestConstants.MULTIFORMAT_INTERSTITIAL) {
            val findVideoAd = device.wait(Until.findObject(videoAd), timeout)
            findVideoAd ?: device.wait(Until.findObject(ad), timeout)
        } else {
            device.wait(Until.findObject(ad), timeout)
        }
        assertNotNull(findAd)

        val findCloseButton = if (adServer == TestConstants.GAM) {
            device.wait(Until.findObject(gamCloseButton), timeout)
        } else {
            device.wait(Until.findObject(closeButton), timeout)
        }
        assertNotNull(findCloseButton)

        findCloseButton.click()
        Thread.sleep(1000)
        device.pressBack()


    }

}