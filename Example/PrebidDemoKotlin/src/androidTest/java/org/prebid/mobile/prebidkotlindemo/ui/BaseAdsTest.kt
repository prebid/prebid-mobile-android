package org.prebid.mobile.prebidkotlindemo.ui

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import junit.framework.AssertionFailedError
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.runner.RunWith
import androidx.test.uiautomator.UiSelector

import androidx.test.uiautomator.UiObject




@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
abstract class BaseAdsTest {
    protected val packageName = "org.prebid.mobile.prebidkotlindemo"
    protected val timeout = 7000L
    protected lateinit var device: UiDevice

    private lateinit var adServerSpinner: UiObject
    private lateinit var adTypeSpinner: UiObject
    private lateinit var showAdButton: UiObject

    private val adsErrorMessagesQueue = ArrayDeque<String>()

    @Before
    fun startMainActivityFromHomeScreen() {
        initDevice()
        startActivity()
        device.wait(
            Until.hasObject(By.pkg(packageName).depth(0)),
            timeout
        )
        initMainScreenComponents()

    }
    @After
    fun checkErrors(){
        displayErrorMessages()
    }

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

    private fun startActivity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(
            packageName
        ).apply {
            this?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

    }

    private fun initMainScreenComponents() {
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

    protected fun testAd(adServer: String, adName: String, retryCount: Int = 2) {
        runCatching {
            goToAd(adServer, adName)
            checkAd(adServer,adName)
        }.getOrElse { throwable ->
            if (retryCount != 0) {
                restartApp()
                testAd(adServer, adName, retryCount - 1)
            } else {
                adsErrorMessagesQueue.add("$adServer - $adName ${throwable.stackTraceToString()}")
                restartApp()
            }
        }
    }
    private fun displayErrorMessages() {
        val failedTestsMessage = adsErrorMessagesQueue.joinToString(separator = System.lineSeparator())
        if (failedTestsMessage.isNotEmpty()){
            adsErrorMessagesQueue.clear()
            throw AssertionError(failedTestsMessage)
        }
    }

    protected abstract fun checkAd(adServer: String,adName: String)

    private fun goToAd(adServer: String, adName: String) {
        adServerSpinner.click()
        selectSpinnerValue(adServer)
        adTypeSpinner.click()
        selectSpinnerValue(adName)
        showAdButton.click()
    }

    private fun selectSpinnerValue(value: String) {
        device.findObject(By.text(value)).click()
    }
    private fun restartApp(){
        Runtime.getRuntime().exec(arrayOf("am", "force-stop", packageName))
        device.pressHome()
        startActivity()
    }


}
