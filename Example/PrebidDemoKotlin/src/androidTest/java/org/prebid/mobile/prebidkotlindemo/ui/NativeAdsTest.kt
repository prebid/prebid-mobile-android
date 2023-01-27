package org.prebid.mobile.prebidkotlindemo.ui

import androidx.annotation.StringRes
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Until
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.testcases.IntegrationKind
import org.prebid.mobile.prebidkotlindemo.testcases.TestCase

@RunWith(Parameterized::class)
class NativeAdsTest(
    @StringRes private val testCaseTitleId: Int,
    private val testCaseName: String
) : BaseAdsTest() {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{1}")
        fun arguments() = listOf(
            arrayOf(R.string.gam_original_native_styles, "GAM Original API Native"),
            arrayOf(R.string.gam_rendering_native, "GAM Rendering API Native"),
            arrayOf(R.string.in_app_native, "In-App Native"),
            arrayOf(R.string.ad_mob_native, "AdMob Native"),
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
            IntegrationKind.GAM_ORIGINAL -> By.clazz("android.webkit.WebView")
            IntegrationKind.ADMOB -> By.clazz("android.widget.LinearLayout")
                .hasChild(
                    By.clazz("android.widget.LinearLayout")
                )
                .hasChild(
                    By.res(packageName, "tvBody")
                )
                .hasChild(
                    By.res(packageName, "imgMedia")
                )

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