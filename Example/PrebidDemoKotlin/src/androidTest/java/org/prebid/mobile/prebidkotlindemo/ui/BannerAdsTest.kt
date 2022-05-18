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
        testAd(TestConstants.IN_APP,TestConstants.BANNER_320x50)
        testAd(TestConstants.GAM,TestConstants.BANNER_320x50)
        testAd(TestConstants.IN_APP_GAM,TestConstants.BANNER_320x50)
        testAd(TestConstants.IN_APP_ADMOB,TestConstants.BANNER_320x50)
        displayErrorMessages()
    }

    override fun checkAd(adServer: String) {
        val frameAdWrapperSelector = By.text("Pbs_banner_320x50")
        val findAd = device.wait(Until.findObject(frameAdWrapperSelector), timeout)
        assertNotNull(findAd)
    }

    override fun teardownAd(adServer: String) {
        device.pressBack()
    }

}