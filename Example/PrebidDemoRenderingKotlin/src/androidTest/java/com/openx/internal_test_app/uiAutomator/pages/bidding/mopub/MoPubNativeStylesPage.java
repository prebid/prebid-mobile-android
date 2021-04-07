package com.openx.internal_test_app.uiAutomator.pages.bidding.mopub;

import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertNotNull;

public class MoPubNativeStylesPage extends AdBasePage<MoPubNativeStylesPage> {
    private static final int TIMEOUT = 15000;

    private static class Locators {
        static BySelector nativeStylesCreative = getWebViewSelectorVersionDepends("OpenX (Brand)");
        static BySelector ctaLocator = getWebViewSelectorVersionDepends("Click here to visit our site!");
    }

    public MoPubNativeStylesPage(UiDevice device) {
        super(device);
    }

    public MoPubNativeStylesPage nativeShouldLoad() {
        assertNotNull("Native is not displayed", device.wait(Until.findObject(Locators.nativeStylesCreative), TIMEOUT));
        return this;
    }

    public MoPubNativeStylesPage clickCta() {
        clickOnView(Locators.ctaLocator, TIMEOUT);
        return this;
    }
}
