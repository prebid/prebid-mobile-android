package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.Assert
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

class BannerTest : BaseUiTest() {
    @Test
    fun inAppBannerShouldBeDisplayed() {
        testAd(TestConstants.IN_APP,TestConstants.BANNER_320x50)
    }
    @Test
    fun gamBannerShouldBeDisplayed() {
        testAd(TestConstants.GAM,TestConstants.BANNER_320x50)
    }
    @Test
    fun inAppGamBannerShouldBeDisplayed() {
        testAd(TestConstants.IN_APP_GAM,TestConstants.BANNER_320x50)
    }
    @Test
    fun inAppAdMobBannerShouldBeDisplayed() {
        testAd(TestConstants.IN_APP_ADMOB,TestConstants.BANNER_320x50)
    }
    /*@Test
    fun inAppMaxBannerShouldBeDisplayed() {
        testAd(TestConstants.IN_APP_MAX,TestConstants.BANNER_320x50)
    }*/

    override fun checkAd() {
        val frameAdWrapperSelector = By.text("Pbs_banner_320x50")
        val findAd = device.wait(Until.findObject(frameAdWrapperSelector), timeout)
        assertTrue(findAd.isClickable)
    }

}