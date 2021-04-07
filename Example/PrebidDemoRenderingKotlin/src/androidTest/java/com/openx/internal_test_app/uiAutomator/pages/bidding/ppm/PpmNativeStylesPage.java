package com.openx.internal_test_app.uiAutomator.pages.bidding.ppm;

import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertNotNull;

public class PpmNativeStylesPage extends AdBasePage<PpmNativeStylesPage> {
    private static final int TIMEOUT = 15000;

    private static class Locators {
        static BySelector nativeStylesCreative = getWebViewSelectorVersionDepends("OpenX (Brand)");
    }

    public PpmNativeStylesPage(UiDevice device) {
        super(device);
    }

    public PpmNativeStylesPage nativeShouldLoad() {
        assertNotNull("Native is not displayed", device.wait(Until.findObject(Locators.nativeStylesCreative), TIMEOUT));
        return this;
    }
}
