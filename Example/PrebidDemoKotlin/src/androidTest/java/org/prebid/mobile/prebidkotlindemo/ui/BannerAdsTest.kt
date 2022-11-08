package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertNotNull
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

@RunWith(JUnitParamsRunner::class)
class BannerAdsTest: BaseAdsTest() {

    @Test
    @Parameters(value = [
        "${TestConstants.GAM}, ${TestConstants.BANNER_320x50}",
        "${TestConstants.IN_APP}, ${TestConstants.BANNER_320x50}",
        "${TestConstants.IN_APP_ADMOB}, ${TestConstants.BANNER_320x50}",
        "${TestConstants.IN_APP_GAM}, ${TestConstants.BANNER_320x50}",
        "${TestConstants.IN_APP}, ${TestConstants.BANNER_MULTISIZE}",
        "${TestConstants.GAM}, ${TestConstants.BANNER_MULTISIZE}"
    ])
    fun bannerAdsShouldBeDisplayed(adServer: String, adName: String) {
        testAd(adServer, adName)
    }

    override fun checkAd(adServer: String, adName: String) {
        val frameAdWrapperSelector = By.hasChild(By.clazz("android.webkit.WebView"))
        val findAd = device.wait(Until.findObject(frameAdWrapperSelector), timeout)
        assertNotNull(findAd)
        device.pressBack()
    }

}