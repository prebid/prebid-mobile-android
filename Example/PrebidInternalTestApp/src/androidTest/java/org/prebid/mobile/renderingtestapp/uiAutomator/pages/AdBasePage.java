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

package org.prebid.mobile.renderingtestapp.uiAutomator.pages;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.test.uiautomator.By.copy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AdBasePage<T> extends BasePage<T> {

    protected static int TIMEOUT = 15000;
    private static final int PAGE_TIMEOUT = 20000;

    public static class SdkEvent {
        public static BySelector adDidLoad = By.res(TAG, "btnAdDidLoad");
        public static BySelector adDidFailToLoad = By.res(TAG, "btnAdFailed");
        public static BySelector adDidDisplay = By.res(TAG, "btnAdDisplayed");
        public static BySelector adDidComplete = By.res(TAG, "btnAdCompleted");
        public static BySelector adWasClicked = By.res(TAG, "btnAdClicked");
        public static BySelector adDidCollapse = By.res(TAG, "btnAdCollapsed");

        //Mopub locators
        public static BySelector mopubAdDismissed = By.res(TAG, "btnAdDismissed");
        public static BySelector mopubAdVideoStarted = By.res(TAG, "btnAdVideoStarted");
        public static BySelector mopubAdPlaybackError = By.res(TAG, "btnAdVideoPlaybackError");

        // Bidding events
        public static BySelector onAdLoaded = By.res(TAG, "btnAdLoaded");
        public static BySelector onAdDisplayed = By.res(TAG, "btnAdDisplayed");
        public static BySelector onAdFailed = By.res(TAG, "btnAdFailed");
        public static BySelector onAdClicked = By.res(TAG, "btnAdClicked");
        public static BySelector onAdClosed = By.res(TAG, "btnAdClosed");

        // Outstream events
        public static BySelector onVideoStart = By.res(TAG, "btnOnVideoStart");
        public static BySelector onVideoEnd = By.res(TAG, "btnOnVideoEnd");
        public static BySelector onVideoPlay = By.res(TAG, "btnOnVideoPlay");
        public static BySelector onVideoPause = By.res(TAG, "btnOnVideoPause");
        public static BySelector onVideoMute = By.res(TAG, "btnOnVideoMute");

        public static BySelector onVideoLoadingFinished = By.res(TAG, "btnVideoLoadingFinished");
        public static BySelector onPlaybackStarted = By.res(TAG, "btnPlaybackStarted");
        public static BySelector onPlaybackFinished = By.res(TAG, "btnPlaybackFinished");
        public static BySelector onPlaybackPaused = By.res(TAG, "btnPlaybackPaused");
        public static BySelector onPlaybackResumed = By.res(TAG, "btnPlaybackResumed");
        public static BySelector onPlaybackMuted = By.res(TAG, "btnPlaybackMuted");
        public static BySelector onPlaybackUnMuted = By.res(TAG, "btnPlaybackUnMuted");
        public static BySelector onFailure = By.res(TAG, "btnFailure");

        // Base native events
        public static BySelector fetchDemandSuccess = By.res(TAG, "btnFetchDemandResultSuccess");
        public static BySelector fetchDemandFailure = By.res(TAG, "btnFetchDemandResultFailure");

        // Ppm native / MoPub events
        public static BySelector getNativeAdSuccess = By.res(TAG, "btnGetNativeAdResultSuccess");
        public static BySelector getNativeAdFailure = By.res(TAG, "btnGetNativeAdResultFailure");
        public static BySelector nativeAdFailed = By.res(TAG, "btnNativeAdFailed");

        // GAM native events
        public static BySelector customAdRequestSuccess = By.res(TAG, "btnCustomAdRequestSuccess");
        public static BySelector unifiedAdRequestSuccess = By.res(TAG, "btnUnifiedRequestSuccess");
        public static BySelector primaryAdRequestFailed = By.res(TAG, "btnPrimaryAdRequestFailure");
        public static BySelector nativeAdLoaded = By.res(TAG, "btnNativeAdLoaded");
        public static BySelector customPrimaryAdWin = By.res(TAG, "btnPrimaryAdWinCustom");
        public static BySelector unifiedPrimaryAdWin = By.res(TAG, "btnPrimaryAdWinUnified");

        // native impression events
        public static BySelector impressionEvent = By.res(TAG, "btnAdEventImpression");
        public static BySelector mrc50Event = By.res(TAG, "btnAdEventMrc50");
        public static BySelector mrc100Event = By.res(TAG, "btnAdEventMrc100");
        public static BySelector video50Event = By.res(TAG, "btnAdEventVideo50");
    }

    private static class Locators {
        static BySelector showInterstitialAd = By.res(TAG, "btnLoad").enabled(true);
        static BySelector closeButton = By.res(TAG, "iv_close_interstitial");
        static BySelector closeBrowser = By.clazz("android.widget.Button").descContains("close");
    }

    protected AdBasePage(UiDevice device) {
        super(device);
    }

    @SuppressWarnings("unchecked")
    public T sdkEventShouldBePresent(BySelector sdkEvent) {
        sdkEventShouldBePresent(sdkEvent, TIMEOUT);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T sdkEventShouldNotBePresent(BySelector sdkEvent) {
        sdkEventShouldNotBePresent(sdkEvent, TIMEOUT);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T sdkEventShouldBePresent(BySelector sdkEvent, int timeout) {
        scrollIntoView(sdkEvent);
        assertNotNull(String.format("%s should be fired", sdkEvent.toString()),
                      device.wait(Until.findObject(copy(sdkEvent).enabled(true)), timeout));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T sdkEventShouldNotBePresent(BySelector sdkEvent, int timeout) {
        scrollIntoView(sdkEvent);
        assertNotNull(String.format("%s should not be fired", sdkEvent.toString()),
                      device.wait(Until.findObject(copy(sdkEvent).enabled(false)), timeout));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T closeBrowser() {
        device.wait(Until.findObject(Locators.closeBrowser), TIMEOUT).click();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T browserShouldOpen() {
        assertNotNull(device.wait(Until.findObject(Locators.closeBrowser), TIMEOUT));
        return (T) this;
    }

    public T showPrebidInterstitial() {
        device.wait(Until.findObject(Locators.showInterstitialAd), TIMEOUT).click();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T closeInterstitial() {
        //close button have no id/locator
        device.findObject(Locators.closeButton).click();
        device.wait(Until.gone(Locators.closeButton), TIMEOUT);
        return (T) this;
    }

    public T orientationShouldBeNatural() {
        assertTrue(device.isNaturalOrientation());
        return (T) this;
    }

    public T orientationShouldBePortrait() {
        assertTrue(device.getDisplayWidth() < device.getDisplayHeight());
        return (T) this;
    }

    public T orientationShouldBeLandscape() {
        assertTrue(device.getDisplayWidth() > device.getDisplayHeight());
        return (T) this;
    }

    public T sleepFor(long delaySeconds) throws InterruptedException {
        Thread.sleep(delaySeconds * 1000);
        return (T) this;
    }

    public T viewShouldBePresent(BySelector locator) {
        assertTrue(String.format("Desired view is not displayed after %d s", TIMEOUT / 1000),
                   device.wait(Until.hasObject(locator), TIMEOUT));
        return (T) this;
    }

    public T viewShouldNotBePresent(BySelector locator) {
        assertFalse(String.format("Desired view is visible after %d s", TIMEOUT / 1000),
                    device.wait(Until.hasObject(locator), TIMEOUT));
        return (T) this;
    }

    protected String getDeviceOrientation() {
        if (device.getDisplayWidth() > device.getDisplayHeight()) {
            return "landscape";
        }
        else {
            return "portrait";
        }
    }

    protected void scrollIntoView(final BySelector selector) {
        int y = device.getDisplayHeight() / 2;
        int x = device.getDisplayWidth() / 2;
        int step = device.getDisplayHeight() / 10;

        final AtomicBoolean isTimedOut = new AtomicBoolean(false);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isTimedOut.set(true);
            }
        }, TIMEOUT);

        while (device.findObject(selector) == null && !isTimedOut.get()) {
            device.swipe(0, y + step, 0, y, 10);
        }

        timer.cancel();
        timer.purge();
    }

    public T clickOnView(BySelector selector, int timeout) {
        UiObject2 uiObject2 = device.wait(Until.findObject(selector), TIMEOUT);
        assertNotNull("Searched view could not be found " + selector.toString(), uiObject2);
        uiObject2.click();
        return (T) this;
    }
}
