package com.openx.internal_test_app.uiAutomator.pages.bidding.mopub;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertTrue;

public class MoPubBiddingBannerPage extends AdBasePage<MoPubBiddingBannerPage> {
    private static final int TIMEOUT = 15000;

    private static class Locators {
        static BySelector bannerCreative = getWebViewSelectorVersionDepends("mobile-demo-banner-640x100");
        static BySelector bannerView = By.desc("adView");
        static BySelector viewContainer = By.res(TAG,"viewContainer");
    }

    public MoPubBiddingBannerPage(UiDevice device) {
        super(device);
    }

    public MoPubBiddingBannerPage bannerShouldLoad() {
        boolean creativePresent = device.wait(Until.findObject(Locators.bannerCreative), TIMEOUT) != null;
        boolean adViewPresent = device.wait(Until.findObject(Locators.bannerView), TIMEOUT) != null;
        assertTrue("Banner is not displayed", creativePresent || adViewPresent);
        return this;
    }

    public MoPubBiddingBannerPage clickBanner() {
        clickOnView(Locators.viewContainer, TIMEOUT);
        return this;
    }
}
