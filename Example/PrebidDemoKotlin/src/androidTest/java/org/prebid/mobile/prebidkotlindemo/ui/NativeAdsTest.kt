package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Until
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

class NativeAdsTest : BaseAdsTest() {
    @Test
    fun nativeAdsShouldBeDisplayed() {
        testAd(TestConstants.GAM,TestConstants.NATIVE_AD)
        testAd(TestConstants.IN_APP, TestConstants.NATIVE_AD)
        testAd(TestConstants.IN_APP_GAM, TestConstants.NATIVE_AD)
        testAd(TestConstants.IN_APP_ADMOB, TestConstants.NATIVE_AD)
    }

    override fun checkAd(adServer: String, adName: String) {
        val ad = nativeAd(adServer)
        val findAd = device.wait(Until.findObject(ad), timeout)
        assertNotNull(findAd)
        device.pressBack()
    }

    private fun nativeAd(adServer: String): BySelector {
        return when (adServer) {
            TestConstants.GAM -> By.clazz("android.webkit.WebView")
            TestConstants.IN_APP_ADMOB -> By.clazz("android.widget.LinearLayout")
                .hasChild(
                    By.clazz("android.widget.LinearLayout")
                )
                .hasChild(
                    By.res(packageName,"tvBody")
                )
                .hasChild(
                    By.res(packageName,"imgMedia")
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