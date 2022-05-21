package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

class BannerAdsTest : BaseAdsTest() {
    @Test
    fun bannerAdsShouldBeDisplayed() {
        testAd(TestConstants.GAM,TestConstants.BANNER_320x50)
        testAd(TestConstants.IN_APP,TestConstants.BANNER_320x50)
        testAd(TestConstants.IN_APP_GAM,TestConstants.BANNER_320x50)
        testAd(TestConstants.IN_APP_ADMOB,TestConstants.BANNER_320x50)
        testAd(TestConstants.IN_APP,TestConstants.BANNER_MULTISIZE)
        testAd(TestConstants.GAM,TestConstants.BANNER_MULTISIZE)
    }

    override fun checkAd(adServer: String,adName:String) {
        val frameAdWrapperSelector = By.textContains("banner")
        val findAd = device.wait(Until.findObject(frameAdWrapperSelector), timeout)
        assertNotNull(findAd)
        device.pressBack()
    }

}