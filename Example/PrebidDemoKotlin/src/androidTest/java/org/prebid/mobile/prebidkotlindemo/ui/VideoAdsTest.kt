package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Until
import junit.framework.Assert
import junit.framework.TestCase
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

class VideoAdsTest : BaseAdsTest() {
    @Test
    fun videoAdsShouldBeDisplayed(){
        testAd(TestConstants.GAM, TestConstants.VIDEO_REWARDED)
        testAd(TestConstants.IN_APP_GAM, TestConstants.VIDEO_REWARDED)
        testAd(TestConstants.IN_APP, TestConstants.VIDEO_REWARDED)
        testAd(TestConstants.IN_APP_ADMOB, TestConstants.VIDEO_REWARDED)
        testAd(TestConstants.IN_APP, TestConstants.VIDEO_INTERSTITIAL_WITH_END_CARD)
        testAd(TestConstants.IN_APP,TestConstants.VIDEO_BANNER)
        testAd(TestConstants.IN_APP_GAM,TestConstants.VIDEO_BANNER)
    }

    override fun checkAd(adServer: String, adName: String) {
        when (adName) {
            TestConstants.VIDEO_BANNER -> checkVideoBannerAd()
            TestConstants.VIDEO_REWARDED -> checkVideoRewardedAd(adServer)
            TestConstants.VIDEO_INTERSTITIAL_WITH_END_CARD -> checkVideoInterstitialAd()
        }
    }

    private fun checkVideoInterstitialAd() {
        val ad = By.res(packageName, "exo_subtitles")
        val endCard = By.text("Pbs_intestitial_320x480")
        val closeButton = By.res(packageName, "iv_close_interstitial")
        val skipButton = By.res(packageName, "iv_skip")

        val findAd = device.wait(Until.findObject(ad), timeout)
        assertNotNull(findAd)

        val findSkipButton = device.wait(Until.findObject(skipButton), timeout * 3)
        assertNotNull(findSkipButton)
        findSkipButton.click()

        val findEndCard = device.wait(Until.findObject(endCard), timeout)
        assertNotNull(findEndCard)

        val findCloseButton = device.wait(Until.findObject(closeButton), timeout)
        assertNotNull(findCloseButton)
        findCloseButton.click()
        Thread.sleep(1000)
        device.pressBack()
    }

    private fun checkVideoBannerAd() {
        val ad = By.res(packageName, "exo_subtitles")
        val volumeButton = By.clazz("android.widget.ImageView")

        val findAd = device.wait(Until.findObject(ad), timeout)
        assertNotNull(findAd)
        val findVolumeButton = device.wait(Until.findObject(volumeButton), timeout)
        assertNotNull(findVolumeButton)
        device.pressBack()
    }

    private fun checkVideoRewardedAd(adServer: String) {
        val ad: BySelector
        val endCard: BySelector
        val closeButton: BySelector
        if (adServer == TestConstants.GAM){
            ad = By.res("video_container")
            endCard = By.text("recycling_300x250")
            closeButton = By.clazz("android.widget.Button")
        } else {
            ad = By.res(packageName, "exo_subtitles")
            endCard = By.text("Pbs_intestitial_320x480")
            closeButton = By.res(packageName, "iv_close_interstitial")
        }
        val findAd = device.wait(Until.findObject(ad), timeout)
        assertNotNull(findAd)

        val findEndCard = device.wait(Until.findObject(endCard), timeout * 3)
        assertNotNull(findEndCard)

        val findCloseButton = device.wait(Until.findObject(closeButton), timeout)
        assertNotNull(findCloseButton)
        findCloseButton.click()
        Thread.sleep(1000)
        device.pressBack()
    }

}