package org.prebid.mobile.renderingtestapp.uiAutomator.pages.factory;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;

public class GamNativePage extends AdBasePage<GamNativePage> {

    private static class Locators {
        static BySelector ctaButton = By.res(TAG, "btnNativeAction");
        static BySelector unifiedCtaButton = By.res(TAG, "ad_call_to_action");
    }

    public GamNativePage(UiDevice device) {
        super(device);
    }

    public GamNativePage clickCta() {
        clickOnView(Locators.ctaButton, TIMEOUT);
        return this;
    }

    public GamNativePage clickUnifiedCta() {
        clickOnView(Locators.unifiedCtaButton, TIMEOUT);
        return this;
    }
}
