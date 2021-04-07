package com.openx.internal_test_app.uiAutomator.pages.bidding.gam;

import androidx.test.espresso.web.webdriver.Locator;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;
import com.openx.internal_test_app.uiAutomator.utils.WebViewLocator;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class GamInterstitialPage extends AdBasePage<GamInterstitialPage> {
    private static final int GAM_TIMEOUT = 9 * 1000;
    public static final int VIDEO_DURATION_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(20);

    private static class Locators {
        static BySelector oxbVideoCreative = By.res(TAG, "exo_content_frame");
        static WebViewLocator gamVideoCreative = new WebViewLocator(Locator.ID, "adContainer");

        static BySelector oxbHtmlCreative = getWebViewSelectorVersionDepends("www.openx");
        static BySelector gamHtmlCreative = By.res("aw0");

        static BySelector btnCloseGamInterstitial = By.desc("Interstitial close button");
    }

    public GamInterstitialPage(UiDevice device) {
        super(device);
    }

    public GamInterstitialPage gamOrOpenXHtmlCreativeShouldBePresent() {
        boolean isCreativePresent = getUiObject(Locators.oxbHtmlCreative) != null
                                    || getUiObject(Locators.gamHtmlCreative) != null;
        assertTrue("Interstitial is not displayed", isCreativePresent);
        return this;
    }

    public GamInterstitialPage gamOrOpenXVideoCreativeShouldBePresent() {

        boolean isCreativePresent = getGamVideoCreative() != null
                                    || getUiObject(Locators.oxbVideoCreative) != null;
        assertTrue("Interstitial is not displayed", isCreativePresent);
        return this;
    }

    /**
     * Click is performed on top right corner after video duration delay in order to work with GAM and OXB end cards.
     */
    public GamInterstitialPage closeEndCard() throws InterruptedException {
        synchronized (device) {
            device.wait(VIDEO_DURATION_TIMEOUT);
        }
        device.click(device.getDisplayWidth() - 50, 50);

        return this;
    }

    @Override
    public GamInterstitialPage closeInterstitial() {
        final UiObject2 gamCreative = getUiObject(Locators.gamHtmlCreative);
        if (gamCreative != null) {
            clickOnView(Locators.btnCloseGamInterstitial, TIMEOUT);
            return this;
        }
        return super.closeInterstitial();
    }

    public GamInterstitialPage clickInterstitial() {
        final UiObject2 oxbCreative = getUiObject(Locators.oxbHtmlCreative);
        BySelector creativeSelector = oxbCreative != null
                                      ? Locators.oxbHtmlCreative
                                      : Locators.gamHtmlCreative;
        clickOnView(creativeSelector, TIMEOUT * 2);
        return this;
    }

    @Override
    public GamInterstitialPage goBackOnce() {
        synchronized (device) {
            try {
                device.wait(TIMEOUT);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return super.goBackOnce();
    }

    private UiObject2 getUiObject(BySelector locator) {
        return device.wait(Until.findObject(locator), GAM_TIMEOUT);
    }

    private GamInterstitialPage getGamVideoCreative() {
        try {
            return webViewElementShouldExist(Locators.gamVideoCreative, GAM_TIMEOUT);
        }
        catch (Exception ignore) {

        }
        return null;
    }
}
