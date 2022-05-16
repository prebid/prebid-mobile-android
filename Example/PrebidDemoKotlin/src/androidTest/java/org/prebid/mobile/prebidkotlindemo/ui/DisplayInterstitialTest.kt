package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

class DisplayInterstitialTest: BaseUiTest() {
    @Test
    fun inAppDisplayInterstitialShouldBeDisplayed(){
        testAd(TestConstants.IN_APP,TestConstants.DISPLAY_INTERSTITIAL)
    }
    @Test
    fun gamDisplayInterstitialShouldBeDisplayed(){
        testAd(TestConstants.GAM,TestConstants.DISPLAY_INTERSTITIAL)
    }
    @Test
    fun inAppGamDisplayInterstitialShouldBeDisplayed(){
        testAd(TestConstants.IN_APP_GAM,TestConstants.DISPLAY_INTERSTITIAL)
    }
    @Test
    fun inAppAdMobDisplayInterstitialShouldBeDisplayed(){
        testAd(TestConstants.IN_APP_ADMOB,TestConstants.DISPLAY_INTERSTITIAL)
    }


    override fun checkAd() {
        val closeButton = By.res(packageName, "iv_close_interstitial")
        val gamCloseButton = By.desc("Interstitial close button")
        val ad = By.textContains("prebid")

        val findAd = device.wait(Until.findObject(ad), timeout)
        var findCloseButton = device.wait(Until.findObject(closeButton), timeout)
        if (findCloseButton == null){
            findCloseButton = device.wait(Until.findObject(gamCloseButton), timeout)
        }
        assertTrue(findAd.isClickable)
        assertNotNull(findCloseButton)

    }
}