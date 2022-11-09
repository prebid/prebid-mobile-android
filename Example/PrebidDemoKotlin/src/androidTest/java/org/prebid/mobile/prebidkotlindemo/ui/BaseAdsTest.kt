package org.prebid.mobile.prebidkotlindemo.ui

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.prebid.mobile.prebidkotlindemo.DemoActivity
import org.prebid.mobile.prebidkotlindemo.utils.RetryRule
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants


@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
abstract class BaseAdsTest {
    protected val packageName = "org.prebid.mobile.prebidkotlindemo"
    protected val timeout = TestConstants.WAITING_TIME
    protected lateinit var device: UiDevice
    private lateinit var context: Context

    @get:Rule
    val retryRule = RetryRule(3)

    @Before
    fun startMainActivityFromHomeScreen() {
        initDevice()
        context = ApplicationProvider.getApplicationContext()
    }

    fun testAd(adServer: String, adName: String) {
        goToAd(adServer, adName)
        checkAd(adServer, adName)
    }

    protected abstract fun checkAd(adServer: String, adName: String)

    private fun initDevice() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressHome()
        val launcherPackage: String = device.launcherPackageName
        assertThat(launcherPackage, notNullValue())
        device.wait(
            Until.hasObject(By.pkg(launcherPackage).depth(0)),
            timeout
        )
    }

    private fun goToAd(adServer: String, adName: String) {
        Runtime.getRuntime().exec(arrayOf("am", "force-stop", packageName))
        val intent = DemoActivity.getIntent(context, adServer, adName, 30000).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
