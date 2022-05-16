package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.Assert
import junit.framework.Assert.assertNotNull
import org.junit.Test
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

class VideoOutstreamAdsTest: BaseAdsTest() {

    @Test
    fun inAppVideoOutstreamShouldBeDisplayed(){
        testAd(TestConstants.IN_APP,TestConstants.VIDEO_BANNER)
    }
    @Test
    fun inAppGamVideoOutstreamShouldBeDisplayed(){
        testAd(TestConstants.IN_APP_GAM,TestConstants.VIDEO_BANNER)
    }

    override fun checkAd() {
        val ad = By.res(packageName, "exo_subtitles")
        val volumeButton = By.clazz("android.widget.ImageView")


        val findAd = device.wait(Until.findObject(ad), timeout)
        val findVolumeButton = device.wait(Until.findObject(volumeButton), timeout)
        assertNotNull(findAd)
        assertNotNull(findVolumeButton)
    }
}