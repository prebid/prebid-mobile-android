/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.gam;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.utils.WebViewLocator;

import java.util.concurrent.TimeUnit;

import androidx.test.espresso.web.webdriver.Locator;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertTrue;

public class GamInterstitialPage extends AdBasePage<GamInterstitialPage> {
    private static final int GAM_TIMEOUT = 9 * 1000;
    public static final int VIDEO_DURATION_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(20);

    private static class Locators {
        static BySelector prebidVideoCreative = By.res(TAG, "exo_content_frame");
        static WebViewLocator gamVideoCreative = new WebViewLocator(Locator.ID, "adContainer");

        static BySelector prebidHtmlCreative = getWebViewSelectorVersionDepends("www.openx");
        static BySelector gamHtmlCreative = By.res("aw0");

        static BySelector btnCloseGamInterstitial = By.desc("Interstitial close button");
    }

    public GamInterstitialPage(UiDevice device) {
        super(device);
    }

    public GamInterstitialPage gamOrPrebidHtmlCreativeShouldBePresent() {
        boolean isCreativePresent = getUiObject(Locators.prebidHtmlCreative) != null
                                    || getUiObject(Locators.gamHtmlCreative) != null;
        assertTrue("Interstitial is not displayed", isCreativePresent);
        return this;
    }

    public GamInterstitialPage gamOrPrebidVideoCreativeShouldBePresent() {

        boolean isCreativePresent = getGamVideoCreative() != null
                                    || getUiObject(Locators.prebidVideoCreative) != null;
        assertTrue("Interstitial is not displayed", isCreativePresent);
        return this;
    }

    /**
     * Click is performed on top right corner after video duration delay in order to work with GAM and prebid end cards.
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
