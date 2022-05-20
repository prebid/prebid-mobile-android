package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

class NativeAdsTest: BaseAdsTest() {
    @Test
    fun nativeAdsShouldBeDisplayed(){
        testAd(TestConstants.GAM,TestConstants.NATIVE_AD)
        testAd(TestConstants.IN_APP,TestConstants.NATIVE_AD)
        testAd(TestConstants.IN_APP_GAM,TestConstants.NATIVE_AD)
        testAd(TestConstants.IN_APP_ADMOB,TestConstants.NATIVE_AD)
    }
    override fun checkAd(adServer: String,adName:String) {
        val ad = if (adServer == TestConstants.GAM){
            By.clazz("android.webkit.WebView")
        } else {
            By.clazz("android.widget.LinearLayout")
        }
        val findAd = device.wait(Until.findObject(ad),timeout)
        assertNotNull(findAd)
        device.pressBack()
    }
}