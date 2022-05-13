package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.Test

class BannerTest : BaseUiTest() {
    @Test
    fun inAppBannerShouldBeDisplayed() {
        testBanner("In-App")
    }
    @Test
    fun gamBannerShouldBeDisplayed() {
        testBanner("Google Ad Manager")
    }
    @Test
    fun inAppGamBannerShouldBeDisplayed() {
        testBanner("In-App + Google Ad Manager")
    }
    @Test
    fun inAppAdMobBannerShouldBeDisplayed() {
        testBanner("In-App + AdMob")
    }
    @Test
    fun inAppMaxBannerShouldBeDisplayed() {
        testBanner("In-App + Applovin MAX")
    }
    private fun testBanner(adServer:String){
        adServerSpinner.click()
        selectSpinnerValue(adServer)
        adTypeSpinner.click()
        selectSpinnerValue("Banner 320x50")
        showAdButton.click()
        val frameAdWrapperSelector = By.res(packageName, "frameAdWrapper")
        val ad = device.wait(Until.findObject(frameAdWrapperSelector), timeout)
        assertNotNull(ad)
    }
}