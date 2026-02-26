package org.prebid.mobile.prebidnextgendemo.ui

import androidx.annotation.StringRes
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.prebid.mobile.prebidnextgendemo.R
import org.prebid.mobile.prebidnextgendemo.testcases.IntegrationKind
import org.prebid.mobile.prebidnextgendemo.testcases.TestCase

@RunWith(Parameterized::class)
class InterstitialAdsTest(
    @StringRes private val testCaseTitleId: Int,
    private val testCaseName: String,
) : BaseAdsTest() {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{1}")
        fun arguments() = listOf(
            arrayOf(R.string.original_display_interstitial, "Original API Display Interstitial"),
            arrayOf(R.string.rendering_display_interstitial, "Rendering API Display Interstitial")
        )
    }

    @Test
    fun test() {
        testAd(testCaseTitleId)
    }

    override fun checkAd(testCase: TestCase) {
        val closeButton = By.res(packageName, "iv_close_interstitial")
        val nextCloseButton = By.desc("Fullscreen ad close button")
        val ad = By.clazz("android.view.View")

        val findAd = device.wait(Until.findObject(ad), timeout)
        assertNotNull(findAd)

        val findCloseButton = if (testCase.integrationKind == IntegrationKind.ORIGINAL) {
            device.wait(Until.findObject(nextCloseButton), timeout)
        } else {
            device.wait(Until.findObject(closeButton), timeout)
        }
        assertNotNull(findCloseButton)

        findCloseButton.click()
        Thread.sleep(1000)
        device.pressBack()
    }

}