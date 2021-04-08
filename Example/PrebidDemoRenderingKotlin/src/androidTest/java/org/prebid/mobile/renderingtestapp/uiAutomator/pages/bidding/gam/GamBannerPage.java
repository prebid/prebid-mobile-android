package org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.gam;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertNotNull;

public class GamBannerPage extends AdBasePage<GamBannerPage> {

    private static class Locators {
        static BySelector bannerCreative = By.desc("adView");
        static BySelector gamBannerCreative = By.res("aw0");
        static BySelector gamVideoCreative = By.res("ad-container");
    }

    public GamBannerPage(UiDevice device) {
        super(device);
    }

    public GamBannerPage gamViewShouldBePresent() {
        assertNotNull("Banner is not displayed",
                      device.wait(Until.findObject(Locators.gamBannerCreative), TIMEOUT));
        return this;
    }

    public GamBannerPage gamVideoViewShouldBePresent() {
        assertNotNull("Video is not displayed",
                      device.wait(Until.findObject(Locators.gamVideoCreative), TIMEOUT));
        return this;
    }

    public GamBannerPage prebidViewShouldBePresent() {
        assertNotNull("Banner is not displayed",
                      device.wait(Until.findObject(Locators.bannerCreative), TIMEOUT));
        return this;
    }

    public GamBannerPage checkCommonEvents() {
        sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded);
        sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed);
        sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
        return this;
    }
}
