package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.Assert
import junit.framework.TestCase
import org.junit.Test
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

class VideoAdsTest : BaseAdsTest() {
    @Test
    fun videoInterstitialWithEndCardAdShouldBeDisplayed() {
        testAd(TestConstants.IN_APP, TestConstants.VIDEO_INTERSTITIAL_WITH_END_CARD)
    }

    @Test
    fun videoRewardedAdsShouldBeDisplayed() {
       /* testAd(TestConstants.GAM, TestConstants.VIDEO_REWARDED)
        testAd(TestConstants.IN_APP_GAM, TestConstants.VIDEO_REWARDED)*/

        testAd(TestConstants.IN_APP, TestConstants.VIDEO_REWARDED)
        testAd(TestConstants.IN_APP_ADMOB, TestConstants.VIDEO_REWARDED)
    }

    @Test
    fun videoBannerAdsShouldBeDisplayed() {
        testAd(TestConstants.GAM,TestConstants.VIDEO_BANNER)
        testAd(TestConstants.IN_APP,TestConstants.VIDEO_BANNER)
    }

    override fun checkAd(adServer: String, adName: String) {
        when (adName) {
            TestConstants.VIDEO_BANNER -> checkVideoBannerAd()
            TestConstants.VIDEO_REWARDED -> checkVideoRewardedAd()
            TestConstants.VIDEO_INTERSTITIAL_WITH_END_CARD -> checkVideoInterstitialAd()
        }
    }

    private fun checkVideoInterstitialAd() {
        val ad = By.res(packageName, "exo_subtitles")
        val endCard = By.text("Pbs_intestitial_320x480")
        val closeButton = By.res(packageName, "iv_close_interstitial")
        val skipButton = By.res(packageName, "iv_skip")

        val findAd = device.wait(Until.findObject(ad), timeout)
        TestCase.assertNotNull(findAd)

        val findSkipButton = device.wait(Until.findObject(skipButton), timeout * 3)
        TestCase.assertNotNull(findSkipButton)
        findSkipButton.click()

        val findEndCard = device.wait(Until.findObject(endCard), timeout)
        TestCase.assertNotNull(findEndCard)

        val findCloseButton = device.wait(Until.findObject(closeButton), timeout)
        TestCase.assertNotNull(findCloseButton)
        findCloseButton.click()
        Thread.sleep(1000)
        device.pressBack()
    }

    private fun checkVideoBannerAd() {
        val ad = By.res(packageName, "exo_subtitles")
        val volumeButton = By.clazz("android.widget.ImageView")

        val findAd = device.wait(Until.findObject(ad), timeout)
        Assert.assertNotNull(findAd)
        val findVolumeButton = device.wait(Until.findObject(volumeButton), timeout)
        Assert.assertNotNull(findVolumeButton)
        device.pressBack()
    }

    private fun checkVideoRewardedAd() {
        val ad = By.res(packageName, "exo_subtitles")
        val endCard = By.text("Pbs_intestitial_320x480")
        val closeButton = By.res(packageName, "iv_close_interstitial")

        val findAd = device.wait(Until.findObject(ad), timeout)
        Assert.assertNotNull(findAd)

        val findEndCard = device.wait(Until.findObject(endCard), timeout * 3)
        Assert.assertNotNull(findEndCard)

        val findCloseButton = device.wait(Until.findObject(closeButton), timeout)
        Assert.assertNotNull(findCloseButton)
        findCloseButton.click()
        Thread.sleep(1000)
        device.pressBack()
    }
}