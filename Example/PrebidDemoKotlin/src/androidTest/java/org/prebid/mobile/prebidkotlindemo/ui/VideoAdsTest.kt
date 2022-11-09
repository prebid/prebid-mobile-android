package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Until
import junit.framework.TestCase.assertNotNull
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants
import java.util.regex.Pattern

@RunWith(JUnitParamsRunner::class)
class VideoAdsTest : BaseAdsTest() {

    @Test
    @Parameters(value = [
        "${TestConstants.GAM}, ${TestConstants.VIDEO_REWARDED}",
        "${TestConstants.IN_APP_GAM}, ${TestConstants.VIDEO_REWARDED}",
        "${TestConstants.IN_APP}, ${TestConstants.VIDEO_REWARDED}",
        "${TestConstants.IN_APP_ADMOB}, ${TestConstants.VIDEO_REWARDED}",
        "${TestConstants.IN_APP}, ${TestConstants.VIDEO_INTERSTITIAL_WITH_END_CARD}",
        "${TestConstants.IN_APP}, ${TestConstants.VIDEO_BANNER}",
        "${TestConstants.IN_APP_GAM}, ${TestConstants.VIDEO_BANNER}",
    ])
    fun videoAdsShouldBeDisplayed(adServer: String, adName: String) {
        testAd(adServer, adName)
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
        val endCard = By.clazz(Pattern.compile(".*PrebidWebViewInterstitial"))
        val closeButton = By.res(packageName, "iv_close_interstitial")
        val skipButton = By.res(packageName, "iv_skip")

        val findAd = device.wait(Until.findObject(ad), timeout)
        assertNotNull(findAd)

        val findSkipButton = device.wait(Until.findObject(skipButton), timeout * 3)
        assertNotNull(findSkipButton)
        findSkipButton.click()

        val findEndCard = device.wait(Until.findObject(endCard), timeout)
        if (findEndCard == null) {
            searchInAllTrees("PrebidWebViewInterstitial")
        }

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
        if (adServer == TestConstants.GAM) {
            ad = By.res("video_container")
            endCard = By.text("recycling_300x250")
            closeButton = By.clazz("android.widget.Button")
        } else {
            ad = By.res(packageName, "exo_subtitles")
            endCard = By.clazz(Pattern.compile(".*PrebidWebViewInterstitial"))
            closeButton = By.res(packageName, "iv_close_interstitial")
        }
        val findAd = device.wait(Until.findObject(ad), timeout)
        assertNotNull(findAd)

        val findEndCard = device.wait(Until.findObject(endCard), timeout * 3)
        if (findEndCard == null) {
            searchInAllTrees("PrebidWebViewInterstitial")
        }

        val findCloseButton = device.wait(Until.findObject(closeButton), timeout)
        assertNotNull(findCloseButton)
        findCloseButton.click()
        Thread.sleep(1000)
        device.pressBack()
    }

    private fun searchInAllTrees(className: String) {
        onView(withClassName(Matchers.containsString(className)))
            .check(matches(isDisplayed()))
    }

}