package org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.ppm;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PpmNativePage extends AdBasePage<PpmNativePage> {

    private static class Locators {
        static BySelector videoCreative = By.res(TAG, "exo_content_frame");
        static BySelector adIndicator = By.res(TAG, "adIndicatorIV");

        static BySelector btnPause = By.res(TAG, "btnPause");
        static BySelector btnResume = By.res(TAG, "btnResume");
        static BySelector btnMute = By.res(TAG, "btnMute");
        static BySelector btnUnMute = By.res(TAG, "btnUnMute");
    }

    public PpmNativePage(UiDevice device) {
        super(device);
    }

    public PpmNativePage videoCreativeWithAdIndicatorShouldBePresent() {
        final boolean isCreativeDisplayed = device.wait(Until.findObject(Locators.videoCreative), TIMEOUT) != null;

        assertTrue("Native video is not displayed", isCreativeDisplayed);
        assertNotNull("Ad indicator is not displayed",
                      device.wait(Until.findObject(Locators.adIndicator), TIMEOUT));
        return this;
    }

    public PpmNativePage clickPause() {
        clickOnView(Locators.btnPause, TIMEOUT);
        return this;
    }

    public PpmNativePage clickResume() {
        clickOnView(Locators.btnResume, TIMEOUT);
        return this;
    }

    public PpmNativePage clickMute() {
        clickOnView(Locators.btnMute, TIMEOUT);
        return this;
    }

    public PpmNativePage clickUnMute() {
        clickOnView(Locators.btnUnMute, TIMEOUT);
        return this;
    }
}
