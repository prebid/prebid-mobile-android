package com.openx.internal_test_app.uiAutomator.pages.mraidBannerAds;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class Mraid3ViewabilityCompliancePage extends MraidBasicPage {

    private static class Locators {
        static BySelector bannerCreative = By.res("viewabilityChart");
        static BySelector exposureError = By.textContains("exposureChange event is not compliant with IAB specification");
        static BySelector mraidEnvError = By.textContains("Environment is not MRAIDV3 Compatible because");
    }

    public Mraid3ViewabilityCompliancePage(UiDevice device) {
        super(device);
    }

    public Mraid3ViewabilityCompliancePage bannerShouldLoad() {
        assertNotNull("Banner is not displayed",
                      device.wait(Until.findObject(Locators.bannerCreative), TIMEOUT * 2));
        return this;
    }

    public Mraid3ViewabilityCompliancePage verifyNoErrorsArePresent() {
        final UiObject2 exposureError = device.wait(Until.findObject(Locators.exposureError), TIMEOUT);
        final UiObject2 mraidEnvError = device.wait(Until.findObject(Locators.mraidEnvError), TIMEOUT);
        assertNull("Exposure error present!", exposureError);
        assertNull("MraidEnv error present!", mraidEnvError);
        return this;
    }
}
