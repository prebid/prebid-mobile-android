package org.prebid.mobile.prebidnextgendemo.ui

import androidx.annotation.StringRes
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Until
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.prebid.mobile.prebidnextgendemo.R
import org.prebid.mobile.prebidnextgendemo.testcases.IntegrationKind
import org.prebid.mobile.prebidnextgendemo.testcases.TestCase

@RunWith(Parameterized::class)
class NativeAdsTest(
    @StringRes private val testCaseTitleId: Int,
    private val testCaseName: String,
) : BaseAdsTest() {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{1}")
        fun arguments() = listOf(
            arrayOf(R.string.original_native_styles, "Original API Native"),
            arrayOf(R.string.rendering_native, "Rendering API Native")
        )
    }

    @Test
    fun test() {
        testAd(testCaseTitleId)
    }

    override fun checkAd(testCase: TestCase) {
        val ad = nativeAd(testCase)
        val findAd = device.wait(Until.findObject(ad), timeout)
        assertNotNull(findAd)
        device.pressBack()
    }

    private fun nativeAd(testCase: TestCase): BySelector {
        return when (testCase.integrationKind) {
            IntegrationKind.ORIGINAL -> By.clazz("android.webkit.WebView")

            else -> By.clazz("android.widget.LinearLayout")
                .hasChild(
                    By.clazz("android.widget.LinearLayout")
                )
                .hasChild(
                    By.clazz("android.widget.ImageView")
                )
                .hasChild(
                    By.clazz("android.widget.TextView")
                )
                .hasChild(
                    By.clazz("android.widget.Button")
                )
        }
    }
}