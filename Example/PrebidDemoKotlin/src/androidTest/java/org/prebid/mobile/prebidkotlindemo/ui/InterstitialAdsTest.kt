package org.prebid.mobile.prebidkotlindemo.ui

import androidx.annotation.StringRes
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.testcases.IntegrationKind
import org.prebid.mobile.prebidkotlindemo.testcases.TestCase

@RunWith(Parameterized::class)
class InterstitialAdsTest(
    @StringRes private val testCaseTitleId: Int,
    private val testCaseName: String
) : BaseAdsTest() {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{1}")
        fun arguments() = listOf(
            arrayOf(R.string.gam_original_display_interstitial, "GAM Original API Display Interstitial"),
            arrayOf(R.string.gam_rendering_display_interstitial, "GAM Rendering API Display Interstitial"),
            arrayOf(R.string.in_app_display_interstitial, "In-App Display Interstitial"),
            arrayOf(R.string.ad_mob_display_interstitial, "AdMob Display Interstitial"),
        )
    }

    @Test
    fun test() {
        testAd(testCaseTitleId)
    }

    override fun checkAd(testCase: TestCase) {
        val closeButton = By.res(packageName, "iv_close_interstitial")
        val gamCloseButton = By.desc("Interstitial close button")
        val ad = By.clazz("android.view.View")

        val findAd = device.wait(Until.findObject(ad), timeout)
        assertNotNull(findAd)

        val findCloseButton = if (testCase.integrationKind == IntegrationKind.GAM_ORIGINAL) {
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