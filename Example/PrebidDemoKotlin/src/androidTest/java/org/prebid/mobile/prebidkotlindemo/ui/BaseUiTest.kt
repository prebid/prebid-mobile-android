package org.prebid.mobile.prebidkotlindemo.ui

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import junit.framework.Assert
import junit.framework.Assert.assertNotNull
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
abstract class BaseUiTest {
    protected lateinit var device: UiDevice
    protected val packageName = "org.prebid.mobile.prebidkotlindemo"
    protected val timeout = 5000L

    private lateinit var adServerSpinner: UiObject
    private lateinit var adTypeSpinner: UiObject
    private lateinit var showAdButton: UiObject
    @Before
    fun startMainActivityFromHomeScreen() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressHome()
        val launcherPackage: String = device.launcherPackageName
        assertThat(launcherPackage, notNullValue())
        device.wait(
            Until.hasObject(By.pkg(launcherPackage).depth(0)),
            timeout
        )
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(
            packageName).apply {
            this?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
        device.wait(
            Until.hasObject(By.pkg(packageName).depth(0)),
            timeout
        )
        initMainScreenComponents()
    }

    protected fun testAd(adServer:String,adName:String){
        adServerSpinner.click()
        selectSpinnerValue(adServer)
        adTypeSpinner.click()
        selectSpinnerValue(adName)
        showAdButton.click()

        checkAd()
    }
    protected abstract fun checkAd()

    private fun selectSpinnerValue(value:String){
        device.findObject(By.text(value)).click()
    }

    private fun initMainScreenComponents(){
        adServerSpinner = device.findObject(
            UiSelector().resourceId("$packageName:id/spinnerAdServer")
        )
        adTypeSpinner = device.findObject(
            UiSelector().resourceId("$packageName:id/spinnerAdType")
        )
        showAdButton = device.findObject(
            UiSelector().resourceId("$packageName:id/btnShowAd")
        )
    }

}
