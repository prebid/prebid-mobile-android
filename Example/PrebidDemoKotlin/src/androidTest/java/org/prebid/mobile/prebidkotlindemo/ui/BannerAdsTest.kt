package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

@RunWith(Parameterized::class)
class BannerAdsTest(
    private val adServer: String,
    private val adName: String
) : BaseAdsTest() {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} {1}")
        fun data() = listOf(
            arrayOf(TestConstants.GAM,TestConstants.BANNER_320x50),
            arrayOf(TestConstants.IN_APP,TestConstants.BANNER_320x50),
            arrayOf(TestConstants.IN_APP_GAM,TestConstants.BANNER_320x50),
            arrayOf(TestConstants.IN_APP_ADMOB,TestConstants.BANNER_320x50),
            arrayOf(TestConstants.IN_APP,TestConstants.BANNER_MULTISIZE),
            arrayOf(TestConstants.GAM,TestConstants.BANNER_MULTISIZE),
        )
    }

    @Test
    fun bannerAdsShouldBeDisplayed() {
        testAd(adServer, adName)
    }

    override fun checkAd(adServer: String, adName: String) {
        val frameAdWrapperSelector = By.hasChild(By.clazz("android.webkit.WebView"))
        val findAd = device.wait(Until.findObject(frameAdWrapperSelector), timeout)
        assertNotNull(findAd)
        device.pressBack()
    }

}