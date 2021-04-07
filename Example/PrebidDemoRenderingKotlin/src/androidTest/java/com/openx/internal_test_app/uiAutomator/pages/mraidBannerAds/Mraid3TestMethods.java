package com.openx.internal_test_app.uiAutomator.pages.mraidBannerAds;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertNotNull;

public class Mraid3TestMethods extends MraidBasicPage {
    public Mraid3TestMethods(UiDevice device) {
        super(device);
    }

    private static class Locators {
        static BySelector bannerCreative = getWebViewSelectorVersionDepends("Click to Resize");
        static BySelector resizeButton = getWebViewSelectorVersionDepends("Resize");
        static BySelector resizeInExpandButton = getWebViewSelectorVersionDepends("Resize(320)");
        static BySelector expandButton = getWebViewSelectorVersionDepends("Expand");
        static BySelector closeButton = By.res(TAG, "iv_close_interstitial");
    }

    public Mraid3TestMethods bannerShouldLoad() {
        assertNotNull("Banner is not displayed",
                      device.wait(Until.findObject(Locators.resizeButton), TIMEOUT * 2));
        return this;
    }

    public Mraid3TestMethods clickBanner() {
        clickOnView(Locators.bannerCreative, TIMEOUT);
        return this;
    }

    public Mraid3TestMethods clickResize() {
        clickOnView(Locators.resizeButton, TIMEOUT);
        return this;
    }

    public Mraid3TestMethods clickResizeInExpand() {
        clickOnView(Locators.resizeInExpandButton, TIMEOUT);
        return this;
    }

    public Mraid3TestMethods clickExpand() {
        clickOnView(Locators.expandButton, TIMEOUT);
        return this;
    }

    public Mraid3TestMethods adShouldBeExpanded() {
        assertNotNull("Ad is not expanded",
                      device.wait(Until.findObject(Locators.closeButton), TIMEOUT));
        return this;
    }

    public Mraid3TestMethods adShouldBeResized() {
        assertNotNull("Ad is not resized",
                      device.wait(Until.findObject(Locators.expandButton), TIMEOUT));
        return this;
    }

    public Mraid3TestMethods closeExpandedAd() {
        clickOnView(Locators.closeButton, TIMEOUT);
        return this;
    }
}
