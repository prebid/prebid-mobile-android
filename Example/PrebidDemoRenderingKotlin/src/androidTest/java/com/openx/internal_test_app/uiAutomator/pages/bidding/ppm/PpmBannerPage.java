package com.openx.internal_test_app.uiAutomator.pages.bidding.ppm;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertNotNull;

public class PpmBannerPage extends AdBasePage<PpmBannerPage> {
    private static final int TIMEOUT = 15000;

    private static class Locators {
        static BySelector bannerCreative = By.res(TAG, "viewContainer").hasChild(
            By.clazz("android.widget.FrameLayout").hasChild(
                By.clazz("android.widget.FrameLayout")));
    }

    public PpmBannerPage(UiDevice device) {
        super(device);
    }

    public PpmBannerPage bannerShouldLoad() {
        assertNotNull("Banner is not displayed",
                      device.wait(Until.findObject(Locators.bannerCreative), TIMEOUT));
        return this;
    }

    public PpmBannerPage clickBanner() {
        clickOnView(Locators.bannerCreative, TIMEOUT);
        return this;
    }
}
