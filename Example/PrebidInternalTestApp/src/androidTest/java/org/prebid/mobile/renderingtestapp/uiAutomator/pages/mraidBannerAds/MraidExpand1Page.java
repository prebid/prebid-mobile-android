package org.prebid.mobile.renderingtestapp.uiAutomator.pages.mraidBannerAds;

import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MraidExpand1Page extends AdBasePage<MraidExpand1Page> {

    private static class Locators {
        static BySelector bannerCreative = getWebViewSelectorVersionDepends("Expand!");
        static BySelector expandedAdName = getWebViewSelectorVersionDepends("IAB MRAID2 Expandable Compliance Ad.");
        static BySelector closeButton = getWebViewSelectorVersionDepends("X");
        static BySelector lockToLandscape = getWebViewSelectorVersionDepends("Lock to Landscape");
        static BySelector lockToPortrait = getWebViewSelectorVersionDepends("Lock to Portrait");
        static BySelector releaseLock = getWebViewSelectorVersionDepends("Release Lock");
        static BySelector rotateToPortrait = getWebViewSelectorVersionDepends("Rotate To Portrait");
    }

    public MraidExpand1Page(UiDevice device) {
        super(device);
    }

    public MraidExpand1Page bannerShouldLoad() {
        assertNotNull("Banner is not displayed",
                      device.wait(Until.findObject(Locators.bannerCreative), TIMEOUT * 2));
        return this;
    }

    public MraidExpand1Page clickBanner() {
        clickOnView(Locators.bannerCreative, TIMEOUT);
        return this;
    }

    public MraidExpand1Page adShouldBeExpanded() {
        assertNotNull("Ad is not expanded",
                      device.wait(Until.findObject(Locators.expandedAdName), TIMEOUT));
        return this;
    }

    public MraidExpand1Page closeExpandedAd() {
        clickOnView(Locators.closeButton, TIMEOUT);
        return this;
    }

    public MraidExpand1Page rotateToPortraitShouldBeDisplayed() {
        assertNotNull("Rotate To Portrait is not displayed",
                      device.wait(Until.findObject(Locators.rotateToPortrait), TIMEOUT));
        return this;
    }

    public MraidExpand1Page lockToLandscape() {
        clickOnView(Locators.lockToLandscape, TIMEOUT);
        return this;
    }

    public MraidExpand1Page lockToPortrait() {
        clickOnView(Locators.lockToPortrait, TIMEOUT);
        return this;
    }

    public MraidExpand1Page releaseLock() {
        clickOnView(Locators.releaseLock, TIMEOUT);
        return this;
    }

    public MraidExpand1Page lockButtonShouldNotBeDisplayed() {
        assertNull("Lock button should not be displayed", device.wait(Until.findObject(Locators.lockToPortrait), 2));
        assertNull("Lock button should not be displayed", device.wait(Until.findObject(Locators.lockToLandscape), 2));
        assertNull("Lock button should not be displayed", device.wait(Until.findObject(Locators.releaseLock), 2));
        return this;
    }
}
