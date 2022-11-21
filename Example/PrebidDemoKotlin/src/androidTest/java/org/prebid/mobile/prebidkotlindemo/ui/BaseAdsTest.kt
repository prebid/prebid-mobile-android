package org.prebid.mobile.prebidkotlindemo.ui

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
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
import org.prebid.mobile.prebidkotlindemo.testcases.TestCase
import org.prebid.mobile.prebidkotlindemo.testcases.TestCaseRepository
import org.prebid.mobile.prebidkotlindemo.utils.RetryRule


@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
abstract class BaseAdsTest {
    protected val packageName = "org.prebid.mobile.prebidkotlindemo"
    protected val timeout = 8000L
    protected lateinit var device: UiDevice
    private lateinit var context: Context

    @get:Rule
    val retryRule = RetryRule(3)

    @Before
    fun startMainActivityFromHomeScreen() {
        initDevice()
        context = ApplicationProvider.getApplicationContext()
    }

    fun testAd(@StringRes stringResId: Int) {
        val testCase = TestCaseRepository.getList().first { it.titleStringRes == stringResId }
        TestCaseRepository.lastTestCase = testCase

        goToAd(testCase)
        checkAd(testCase)
    }

    protected abstract fun checkAd(testCase: TestCase)

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

    private fun goToAd(testCase: TestCase) {
//        Runtime.getRuntime().exec(arrayOf("am", "force-stop", packageName))

        val intent = Intent(context, testCase.activity).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

}
