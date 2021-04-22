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

package org.prebid.mobile.renderingtestapp.uiAutomator.tests.gam;

import org.junit.Test;
import org.prebid.mobile.renderingtestapp.R;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.factory.GamNativePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.utils.BaseUiAutomatorTest;

import androidx.test.uiautomator.BySelector;

public class GamNativeTests extends BaseUiAutomatorTest {
    @Test
    public void testGamNativeStylesMrect() throws InterruptedException {
        homePage.getNativePageFactory()
                .goToGamNativeStyles(getStringResource(R.string.demo_bidding_gam_native_styles_mrect))
                .nativeShouldLoad()
                .clickCta()
                .sleepFor(2)
                .goBackOnce()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testGamNativeStylesFluid() throws InterruptedException {
        homePage.getNativePageFactory()
                .goToGamNativeStyles(getStringResource(R.string.demo_bidding_gam_native_styles_fluid))
                .nativeShouldLoad()
                .clickCta()
                .sleepFor(2)
                .goBackOnce()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testGamNativeStylesNoAssets() {
        homePage.getNativePageFactory()
                .goToGamNativeStyles(getStringResource(R.string.demo_bidding_gam_native_styles_no_assets))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPrebidGamNativeStylesMrect() throws InterruptedException {
        homePage.setUseMockServer(false)
                .getNativePageFactory()
                .goToGamNativeStyles(getStringResource(R.string.demo_bidding_gam_native_styles_mrect))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testGamNativeCustomFormatPrebidWin() throws InterruptedException {
        final GamNativePage gamNativePage =
            homePage.getNativePageFactory()
                    .goToGamNative(getStringResource(R.string.demo_bidding_gam_native_custom_templates));

        verifyGamNativeAdPrebidWin(gamNativePage, AdBasePage.SdkEvent.customAdRequestSuccess);
    }

    @Test
    public void testGamNativeNativeAdPrebidWin() throws InterruptedException {
        final GamNativePage gamNativePage =
            homePage.getNativePageFactory()
                    .goToGamNative(getStringResource(R.string.demo_bidding_gam_native_unified_ads));

        verifyGamNativeAdPrebidWin(gamNativePage, AdBasePage.SdkEvent.nativeAdRequestSuccess);
    }

    @Test
    public void testGamNativeCustomFormatGamWin() throws InterruptedException {
        homePage.getNativePageFactory()
                .goToGamNative(getStringResource(R.string.demo_bidding_gam_native_custom_templates_no_bids))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.fetchDemandFailure)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.customPrimaryAdWin)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.customAdRequestSuccess)
                .clickCta()
                .sleepFor(2)
                .goBackOnce()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.impressionEvent)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.mrc50Event)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.mrc100Event)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.nativeAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.fetchDemandSuccess)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.primaryAdRequestFailed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.nativePrimaryAdWin)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.nativeAdRequestSuccess)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.video50Event);
    }

    @Test
    public void testGamNativeAdGamWin() throws InterruptedException {
        homePage.getNativePageFactory()
                .goToGamNative(getStringResource(R.string.demo_bidding_gam_native_unified_ads_no_bids))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.fetchDemandFailure)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.nativePrimaryAdWin)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.nativeAdRequestSuccess)
                .clickNativeGamCta()
                .sleepFor(2)
                .goBackOnce()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.impressionEvent)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.mrc50Event)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.mrc100Event)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.nativeAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.fetchDemandSuccess)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.primaryAdRequestFailed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.customPrimaryAdWin)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.customAdRequestSuccess)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.video50Event);
    }

    private void verifyGamNativeAdPrebidWin(GamNativePage nativePage, BySelector winnerRequestSelector)
    throws InterruptedException {
        nativePage.sdkEventShouldBePresent(AdBasePage.SdkEvent.fetchDemandSuccess)
                  .sdkEventShouldBePresent(winnerRequestSelector)
                  .sdkEventShouldBePresent(AdBasePage.SdkEvent.nativeAdLoaded)
                  .clickCta()
                  .sleepFor(2)
                  .goBackOnce()
                  .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                  .sdkEventShouldBePresent(AdBasePage.SdkEvent.impressionEvent)
                  .sdkEventShouldBePresent(AdBasePage.SdkEvent.mrc50Event)
                  .sdkEventShouldBePresent(AdBasePage.SdkEvent.mrc100Event)
                  .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.fetchDemandFailure)
                  .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.primaryAdRequestFailed)
                  .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.customPrimaryAdWin)
                  .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.nativePrimaryAdWin)
                  .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.video50Event);
    }
}
