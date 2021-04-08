package org.prebid.mobile.renderingtestapp.uiAutomator.pages.mraidBannerAds;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class Mraid3LoadAndEventsPage extends MraidBasicPage {

    private static class Locators {
        static BySelector btnExpandStateChangeCheck = getWebViewSelectorVersionDepends("Tap For Expand/stateChange Check");
        static BySelector btnExpandSizeChangeCheck = getWebViewSelectorVersionDepends("Tap For Expand/sizeChange Check");
        static BySelector btnSdkClose = By.res(TAG, "iv_close_interstitial");

        static BySelector txtTapSdkClose = getWebViewSelectorVersionDepends("Tap SDK Close Button");
        static BySelector txtTapJsClose = getWebViewSelectorVersionDepends("Tap To Close Expand");
        static BySelector txtTapToCheckLogs = getWebViewSelectorVersionDepends("Tap To Check Logs");
        static BySelector txtTapToUnload = getWebViewSelectorVersionDepends("Tap To Unload");

        static BySelector lytWebViewBanner = By.res(TAG, "web_view_banner");
        static BySelector logDiv = By.res("log");
    }

    public Mraid3LoadAndEventsPage(UiDevice device) {
        super(device);
    }

    public Mraid3LoadAndEventsPage bannerShouldLoad() {
        assertNotNull("Banner is not displayed",
                      device.wait(Until.findObject(Locators.btnExpandStateChangeCheck), TIMEOUT * 2));
        return this;
    }

    public Mraid3LoadAndEventsPage clickExpandStateChangeCheck() {
        clickOnView(Locators.btnExpandStateChangeCheck, TIMEOUT);
        return this;
    }

    public Mraid3LoadAndEventsPage clickSdkClose() {
        clickOnView(Locators.btnSdkClose, TIMEOUT);
        return this;
    }

    public Mraid3LoadAndEventsPage clickJsClose() {
        clickOnView(Locators.txtTapJsClose, TIMEOUT);
        return this;
    }

    public Mraid3LoadAndEventsPage clickCheckLogs() {
        clickOnView(Locators.txtTapToCheckLogs, TIMEOUT);
        return this;
    }

    public Mraid3LoadAndEventsPage clickUnload() {
        clickOnView(Locators.txtTapToUnload, TIMEOUT);
        return this;
    }

    public Mraid3LoadAndEventsPage clickExpandSizeChangeCheck() {
        clickOnView(Locators.btnExpandSizeChangeCheck, TIMEOUT);
        return this;
    }

    public Mraid3LoadAndEventsPage sdkCloseButtonShouldBePresent() {
        assertNotNull("Sdk close button is not displayed!",
                      device.wait(Until.findObject(Locators.btnSdkClose), TIMEOUT));
        return this;
    }

    public Mraid3LoadAndEventsPage logShouldBePresent() {
        assertNotNull("Log is not displayed!",
                      device.wait(Until.findObject(Locators.logDiv), TIMEOUT));
        return this;
    }

    public Mraid3LoadAndEventsPage tapSdkCloseTextShouldBePresent() {
        assertNotNull("Tap Sdk close button is not displayed!",
                      device.wait(Until.findObject(Locators.txtTapSdkClose), TIMEOUT));
        return this;
    }

    public Mraid3LoadAndEventsPage adShouldNotBeDisplayed() {
        assertNull("Ad should not be displayed!",
                   device.wait(Until.findObject(Locators.lytWebViewBanner), TIMEOUT));
        return this;
    }
}
