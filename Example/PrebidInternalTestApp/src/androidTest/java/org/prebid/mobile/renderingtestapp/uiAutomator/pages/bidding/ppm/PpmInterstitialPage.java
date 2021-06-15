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

package org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.ppm;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PpmInterstitialPage extends AdBasePage<PpmInterstitialPage> {

    private static class Locators {
        static BySelector htmlCreative = getWebViewSelectorVersionDepends("www.openx");
        static BySelector videoCreative = By.res(TAG, "exo_content_frame");
        static BySelector learnMore = By.res(TAG, "LearnMore");

        static BySelector closeButton = By.res(TAG, "iv_close_interstitial");
    }

    public PpmInterstitialPage(UiDevice device) {
        super(device);
    }

    public PpmInterstitialPage htmlCreativeShouldBePresent() {
        final boolean isCreativeDisplayed = device.wait(Until.findObject(Locators.htmlCreative), TIMEOUT) != null;

        assertTrue("Interstitial is not displayed", isCreativeDisplayed);
        return this;
    }

    public PpmInterstitialPage videoCreativeWithShouldBePresent() {
        final boolean isCreativeDisplayed = device.wait(Until.findObject(Locators.videoCreative), TIMEOUT) != null;

        assertTrue("Interstitial is not displayed", isCreativeDisplayed);
        return this;
    }

    public PpmInterstitialPage videoCreativeShouldBePresent() {
        final boolean isCreativeDisplayed = device.wait(Until.findObject(Locators.videoCreative), TIMEOUT) != null;
        assertTrue("Interstitial is not displayed", isCreativeDisplayed);
        return this;
    }

    public PpmInterstitialPage videoCreativeShouldNotBePresent() {
        final boolean isCreativeDisplayed = device.wait(Until.gone(Locators.videoCreative), TIMEOUT) != null;

        assertTrue("Interstitial is displayed", isCreativeDisplayed);
        return this;
    }

    public PpmInterstitialPage closeButtonShouldBeDisplayedAfter(int delaySeconds) {
        assertNotNull(String.format("Close button is not displayed after %d s", delaySeconds),
                      device.wait(Until.findObject(Locators.closeButton), delaySeconds * 1000 + 2000));
        return this;
    }

    public PpmInterstitialPage closeInterstitial(int videoDurationTimeout) {
        clickOnView(Locators.closeButton, videoDurationTimeout);
        return this;
    }

    public PpmInterstitialPage clickInterstitial() {
        clickOnView(Locators.htmlCreative, TIMEOUT * 2);
        return this;
    }

    public PpmInterstitialPage learnMore() throws InterruptedException {
        synchronized (device) {
            device.wait(3000);
        }
        device.click(device.getDisplayWidth() - 100, device.getDisplayHeight() - 100);
        return this;
    }
}
