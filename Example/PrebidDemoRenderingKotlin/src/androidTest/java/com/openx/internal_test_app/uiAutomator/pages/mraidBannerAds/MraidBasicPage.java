package com.openx.internal_test_app.uiAutomator.pages.mraidBannerAds;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertNotNull;

public class MraidBasicPage extends AdBasePage<MraidBasicPage> {

    protected static final int TIMEOUT = 10000;

    private static class Locators {
        static BySelector bannerCreative = By.res(TAG, "viewContainer").hasChild(
            By.clazz("android.widget.FrameLayout").hasChild(
                By.clazz("android.widget.FrameLayout").hasChild(
                    By.clazz("android.webkit.WebView"))));
        static BySelector expandedCreative = By.res("expanded");
        static BySelector closeButton = By.res("iv_close_interstitial");
    }

    public MraidBasicPage(UiDevice device) {
        super(device);
    }

    public MraidBasicPage bannerShouldLoad() {
        assertNotNull("Banner is not displayed",
                      device.wait(Until.findObject(Locators.bannerCreative), TIMEOUT * 2));
        return this;
    }

    public MraidBasicPage clickBanner() {
        device.wait(Until.findObject(Locators.bannerCreative), TIMEOUT)
              .click();
        return this;
    }

    public MraidBasicPage adShouldBeExpanded() {
        assertNotNull("Mraid banner is not displayed",
                      device.wait(Until.findObject(Locators.expandedCreative), TIMEOUT));
        return this;
    }

}
