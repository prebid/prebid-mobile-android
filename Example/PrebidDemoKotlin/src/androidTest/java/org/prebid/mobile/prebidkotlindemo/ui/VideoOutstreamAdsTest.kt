package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.Assert
import junit.framework.Assert.assertNotNull
import org.junit.Test
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

class VideoOutstreamAdsTest: BaseAdsTest() {

    @Test
    fun videoOutstreamAdsShouldBeDisplayed(){
        testAd(TestConstants.IN_APP,TestConstants.VIDEO_BANNER)
        testAd(TestConstants.IN_APP_GAM,TestConstants.VIDEO_BANNER)
        displayErrorMessages()
    }

    override fun checkAd(adServer: String) {
        val ad = By.res(packageName, "exo_subtitles")
        val volumeButton = By.clazz("android.widget.ImageView")

        val findAd = device.wait(Until.findObject(ad), timeout)
        assertNotNull(findAd)
        val findVolumeButton = device.wait(Until.findObject(volumeButton), timeout)
        assertNotNull(findVolumeButton)
    }

    override fun teardownAd(adServer: String) {
        device.pressBack()
    }

}