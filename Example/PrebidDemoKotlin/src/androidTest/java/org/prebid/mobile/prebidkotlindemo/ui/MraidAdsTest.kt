package org.prebid.mobile.prebidkotlindemo.ui

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import org.prebid.mobile.prebidkotlindemo.utils.TestConstants

class MraidAdsTest:BaseAdsTest() {
    @Test
    fun mraidAdsShouldBeDisplayed(){
        testAd(TestConstants.IN_APP,TestConstants.MRAID_RESIZE)
        testAd(TestConstants.IN_APP,TestConstants.MRAID_RESIZE_WITH_ERRORS)
        testAd(TestConstants.IN_APP,TestConstants.MRAID_EXPAND)
    }

    override fun checkAd(adServer: String,adName:String) {
        when (adName){
            TestConstants.MRAID_EXPAND -> checkMraidExpand()
            TestConstants.MRAID_RESIZE -> checkMraidResize()
            TestConstants.MRAID_RESIZE_WITH_ERRORS -> checkMraidResizeWithErrors()
        }
    }

    private fun checkMraidResize() {
        val clickToResize = By.text("Click to Resize")
        val findClickToResize = device.wait(Until.findObject(clickToResize), timeout)
        assertNotNull(findClickToResize)
        findClickToResize.click()

        val closeButton = By.text("X")
        val findCloseButton = device.wait(Until.findObject(closeButton), timeout)
        assertNotNull(findCloseButton)
        findCloseButton.click()
        device.pressBack()
    }
    private fun checkMraidResizeWithErrors() {
        val clickToggleScreen = By.res(packageName,"toggleOffscreenDiv")
        val findClickToggleScreen = device.wait(Until.findObject(clickToggleScreen), timeout)
        assertNotNull(findClickToggleScreen)
        findClickToggleScreen.click()

        val resizeLeft = By.res(packageName,"resizeLeftText")
        val findResizeLeft = device.wait(Until.findObject(resizeLeft), timeout)
        assertNotNull(findResizeLeft)
        findResizeLeft.click()

        val resizeRight = By.res(packageName,"resizeRightText")
        val findResizeRight = device.wait(Until.findObject(resizeRight), timeout)
        assertNotNull(findResizeRight)
        findResizeRight.click()

        val resizeUp = By.res(packageName,"resizeUpText")
        val findResizeUp = device.wait(Until.findObject(resizeUp), timeout)
        assertNotNull(findResizeUp)
        findResizeUp.click()

        val resizeDown = By.res(packageName,"resizeDownText")
        val findResizeDown = device.wait(Until.findObject(resizeDown), timeout)
        assertNotNull(findResizeDown)
        findResizeDown.click()

        val closeButton = By.res(packageName,"closeButtonDiv")
        val findCloseButton = device.wait(Until.findObject(closeButton),timeout)
        assertNotNull(findCloseButton)
        findCloseButton.click()

        device.pressBack()
    }
    private fun checkMraidExpand() {
        val clickToExpand = By.res(packageName,"expandbtn")
        val findClickToExpand = device.wait(Until.findObject(clickToExpand), timeout)
        assertNotNull(findClickToExpand)
        findClickToExpand.click()

        val closeButton = By.res(packageName,"closediv")
        val findCloseButton = device.wait(Until.findObject(closeButton), timeout)
        assertNotNull(findCloseButton)
        findCloseButton.click()
        device.pressBack()
    }

}