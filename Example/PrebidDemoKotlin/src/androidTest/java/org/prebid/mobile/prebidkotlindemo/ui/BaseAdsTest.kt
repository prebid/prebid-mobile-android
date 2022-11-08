package org.prebid.mobile.prebidkotlindemo.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.runner.RunWith
import org.prebid.mobile.prebidkotlindemo.DemoActivity
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants
import java.io.IOException


@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
abstract class BaseAdsTest {
    protected val packageName = "org.prebid.mobile.prebidkotlindemo"
    protected val timeout = TestConstants.WAITING_TIME
    protected lateinit var device: UiDevice
    private lateinit var context: Context

    @Before
    fun startMainActivityFromHomeScreen() {
        initDevice()
        context = ApplicationProvider.getApplicationContext()
    }

    fun testAd(adServer: String, adName: String, retryCount: Int = 2) {
        runCatching {
            goToAd(adServer, adName)
            checkAd(adServer, adName)
        }.getOrElse { throwable ->
            if (retryCount != 0) {
                testAd(adServer, adName, retryCount - 1)
            } else {
                throw throwable
            }
        }
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
        val intent = DemoActivity.getIntent(context, adServer, adName, 30000).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
