package com.openx.internal_test_app.uiAutomator.pages.bidding.gam;

import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertNotNull;

public class GamNativeStylesPage extends AdBasePage<GamNativeStylesPage> {
    private static final int TIMEOUT = 15000;

    private static class Locators {
        static BySelector nativeStylesCreative = getWebViewSelectorVersionDepends("OpenX (Brand)");
        static BySelector ctaLocator = getWebViewSelectorVersionDepends("Click here to visit our site!");
    }

    public GamNativeStylesPage(UiDevice device) {
        super(device);
    }

    public GamNativeStylesPage nativeShouldLoad() {
        assertNotNull("Native is not displayed", device.wait(Until.findObject(Locators.nativeStylesCreative), TIMEOUT));
        return this;
    }

    public GamNativeStylesPage clickCta() {
        clickOnView(Locators.ctaLocator, TIMEOUT);
        return this;
    }
}
