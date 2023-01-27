package org.prebid.mobile.prebidkotlindemo.ui

import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Until
import junit.framework.TestCase.assertNotNull
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.testcases.AdFormat
import org.prebid.mobile.prebidkotlindemo.testcases.IntegrationKind
import org.prebid.mobile.prebidkotlindemo.testcases.TestCase
import java.util.regex.Pattern

@RunWith(Parameterized::class)
class VideoAdsTest(
    @StringRes private val testCaseTitleId: Int,
    private val testCaseName: String
) : BaseAdsTest() {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{1}")
        fun arguments() = listOf(
            arrayOf(R.string.gam_original_video_rewarded, "GAM Original API Video Rewarded"),
            arrayOf(R.string.ad_mob_video_rewarded, "AdMob Video Rewarded"),

            arrayOf(R.string.gam_rendering_video_banner, "GAM Video Banner"),
            arrayOf(R.string.gam_rendering_video_rewarded, "GAM Rendering API Video Rewarded"),

            arrayOf(R.string.in_app_video_banner, "In-App Video Interstitial End Card"),
            arrayOf(R.string.in_app_video_interstitial_end_card, "In-App Video Interstitial End Card"),
            arrayOf(R.string.in_app_video_rewarded, "In-App Video Rewarded"),
        )
    }

    @Test
    fun test() {
        testAd(testCaseTitleId)
    }

    override fun checkAd(testCase: TestCase) {
        when (testCase.adFormat) {
            AdFormat.VIDEO_BANNER -> checkVideoBannerAd()
            AdFormat.VIDEO_REWARDED -> checkVideoRewardedAd(testCase)
            AdFormat.VIDEO_INTERSTITIAL -> checkVideoInterstitialAd()
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

    private fun checkVideoRewardedAd(testCase: TestCase) {
        val ad: BySelector
        val endCard: BySelector
        val closeButton: BySelector
        if (testCase.integrationKind == IntegrationKind.GAM_ORIGINAL) {
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
        if (findEndCard == null && testCase.integrationKind != IntegrationKind.GAM_ORIGINAL) {
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