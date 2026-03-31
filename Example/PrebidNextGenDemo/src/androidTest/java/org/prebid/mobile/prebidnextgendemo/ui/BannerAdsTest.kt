package org.prebid.mobile.prebidnextgendemo.ui

import androidx.annotation.StringRes
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.prebid.mobile.prebidnextgendemo.R
import org.prebid.mobile.prebidnextgendemo.testcases.TestCase

@RunWith(Parameterized::class)
class BannerAdsTest(
    @StringRes private val testCaseTitleId: Int,
    private val testCaseName: String,
) : BaseAdsTest() {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{1}")
        fun arguments() = listOf(
            arrayOf(R.string.original_display_banner_320x50, "Original API Display Banner 320x50"),
            arrayOf(R.string.rendering_display_banner_320x50, "Rendering API Display Banner 320x50")
        )
    }

    @Test
    fun test() {
        testAd(testCaseTitleId)
    }

    override fun checkAd(testCase: TestCase) {
        val frameAdWrapperSelector = By.hasChild(By.clazz("android.webkit.WebView"))
        val findAd = device.wait(Until.findObject(frameAdWrapperSelector), timeout)
        assertNotNull(findAd)
        device.pressBack()
    }

}