package org.prebid.mobile.prebidkotlindemo.ui

import android.os.Build
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.TestCase.assertNotNull
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.prebid.mobile.prebidkotlindemo.test.BuildConfig
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

@RunWith(JUnitParamsRunner::class)
class MraidAdsTest : BaseAdsTest() {

    @Test
    @Parameters(value = [
        "${TestConstants.IN_APP}, ${TestConstants.MRAID_RESIZE}",
        "${TestConstants.IN_APP}, ${TestConstants.MRAID_RESIZE_WITH_ERRORS}",
        "${TestConstants.IN_APP}, ${TestConstants.MRAID_EXPAND}"
    ])
    fun mraidAdsShouldBeDisplayed(adServer: String, adName: String) {
        testAd(adServer, adName)
    }

    override fun checkAd(adServer: String, adName: String) {
        when (adName) {
            TestConstants.MRAID_EXPAND -> checkMraidExpand()
            TestConstants.MRAID_RESIZE -> checkMraidResize()
            TestConstants.MRAID_RESIZE_WITH_ERRORS -> checkMraidResizeWithErrors()
        }
    }

    private fun checkMraidResize() {
        val clickToResize = By.res("adContainer")
        val findClickToResize = device.wait(Until.findObject(clickToResize), timeout)
        assertNotNull(findClickToResize)
        findClickToResize.click()

        // For now we don't have proper accessibility id for close button
        val closeButton = if (Build.VERSION.SDK_INT >= 29) {
            By.text("X")
        } else {
            Thread.sleep(2000)
            By.desc("X")
        }
        val findCloseButton = device.wait(Until.findObject(closeButton), timeout)
        assertNotNull(findCloseButton)
        findCloseButton.click()
        device.pressBack()

    }

    private fun checkMraidResizeWithErrors() {
        val clickToggleScreen = By.res("toggleOffscreenDiv")
        val findClickToggleScreen = device.wait(Until.findObject(clickToggleScreen), timeout)
        assertNotNull(findClickToggleScreen)
        findClickToggleScreen.click()

        val resizeLeft = By.res("resizeLeftText")
        val findResizeLeft = device.wait(Until.findObject(resizeLeft), timeout)
        assertNotNull(findResizeLeft)
        findResizeLeft.click()

        val resizeRight = By.res("resizeRightText")
        val findResizeRight = device.wait(Until.findObject(resizeRight), timeout)
        assertNotNull(findResizeRight)
        findResizeRight.click()

        val resizeUp = By.res("resizeUpText")
        val findResizeUp = device.wait(Until.findObject(resizeUp), timeout)
        assertNotNull(findResizeUp)
        findResizeUp.click()

        val resizeDown = By.res("resizeDownText")
        val findResizeDown = device.wait(Until.findObject(resizeDown), timeout)
        assertNotNull(findResizeDown)
        findResizeDown.click()

        val closeButton = By.res("closeButtonDiv")
        val findCloseButton = device.wait(Until.findObject(closeButton), timeout)
        assertNotNull(findCloseButton)
        findCloseButton.click()

        device.pressBack()
    }

    private fun checkMraidExpand() {
        val clickToExpand = By.res("maindiv")
        val findClickToExpand = device.wait(Until.findObject(clickToExpand), timeout)
        assertNotNull(findClickToExpand)
        findClickToExpand.click()

        val closeButton = By.res("closediv")
        val findCloseButton = device.wait(Until.findObject(closeButton), timeout)
        assertNotNull(findCloseButton)
        findCloseButton.click()
        device.pressBack()
    }

}