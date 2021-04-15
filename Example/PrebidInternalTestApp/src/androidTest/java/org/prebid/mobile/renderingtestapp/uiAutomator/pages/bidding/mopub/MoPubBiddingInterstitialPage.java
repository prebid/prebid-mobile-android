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

package org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.mopub;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertNotNull;

public class MoPubBiddingInterstitialPage extends AdBasePage<MoPubBiddingInterstitialPage> {
    private static final int VIDEO_DURATION = 16 * 1000;
    private static final int TIMEOUT_FULLSCREEN_HINT = 2500;

    public MoPubBiddingInterstitialPage(UiDevice device) {
        super(device);
    }

    public MoPubBiddingInterstitialPage interstitialShouldBeDisplayed() {
        assertNotNull("Interstitial is not shown",
                      device.wait(Until.findObject(Locators.interstitialView), TIMEOUT));
        return this;
    }

    public MoPubBiddingInterstitialPage clickInterstitial() {
        device.wait(Until.findObject(Locators.creative), TIMEOUT)
              .click();
        return this;
    }

    public MoPubBiddingInterstitialPage commonInterstitialEventsShouldPresent() {
        sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad);
        sdkEventShouldBePresent(SdkEvent.adDidDisplay);
        sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
        return this;
    }

    public MoPubBiddingInterstitialPage commonRewardedVideoEventsShouldPresent() {
        sdkEventShouldBePresent(SdkEvent.adDidLoad);
        sdkEventShouldBePresent(SdkEvent.mopubAdVideoStarted);
        sdkEventShouldBePresent(SdkEvent.adDidComplete);
        sdkEventShouldNotBePresent(SdkEvent.adDidFailToLoad);
        sdkEventShouldNotBePresent(SdkEvent.mopubAdPlaybackError);
        return this;
    }

    public MoPubBiddingInterstitialPage learnMore() throws InterruptedException {
        synchronized (device) {
            device.wait(3000);
        }
        device.click(device.getDisplayWidth() - 100, device.getDisplayHeight() - 100);
        return this;
    }

    public MoPubBiddingInterstitialPage closeInterstitial() {
        try {
            Thread.sleep(1500);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        device.click(device.getDisplayWidth() - 50, 50);
        return this;
    }

    public MoPubBiddingInterstitialPage checkFullScreenHint() {
        UiObject2 gotItButton = device.wait(Until.findObject(Locators.fullscreenHint), TIMEOUT_FULLSCREEN_HINT);
        if (gotItButton != null) {
            gotItButton.click();
        }
        return this;
    }

    public MoPubBiddingInterstitialPage waitTillVideoEnds(){
        try {
            Thread.sleep(VIDEO_DURATION);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }

    private static class Locators {
        static BySelector creative = getWebViewSelectorVersionDepends("www.openx");
        static BySelector interstitialView = getWebViewSelectorVersionDepends("www.openx");
        static BySelector fullscreenHint = By.text("GOT IT");
    }
}
