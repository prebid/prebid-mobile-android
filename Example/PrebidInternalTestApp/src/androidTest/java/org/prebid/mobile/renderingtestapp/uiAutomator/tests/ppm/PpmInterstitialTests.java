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

package org.prebid.mobile.renderingtestapp.uiAutomator.tests.ppm;

import org.junit.Test;
import org.prebid.mobile.renderingtestapp.R;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.utils.BaseUiAutomatorTest;

public class PpmInterstitialTests extends BaseUiAutomatorTest {
    @Test
    public void testPpmInterstitial320x480() {
        final String exampleName = getStringResource(R.string.demo_bidding_in_app_interstitial_320_480);
        verifyPpmHtmlInterstitialExampleWithValidBid(exampleName);
    }

    @Test
    public void testPpmInterstitial320x480NoBids() {
        final String exampleName = getStringResource(R.string.demo_bidding_in_app_interstitial_320_480_no_bids);
        homePage.setUseMockServer(false)
                .getInterstitialPageFactory()
                .goToPpmInterstitialExample(exampleName)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdFailed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdClicked);
    }

    @Test
    public void testPpmInterstitialWithBannersCloseAfterUpdate()
    throws InterruptedException {
        homePage.getInterstitialPageFactory()
                .goToPpmInterstitialExample(getStringResource(R.string.demo_bidding_in_app_banners_and_interstitial))
                .showPrebidInterstitial()
                .htmlCreativeShouldBePresent()
                .sleepFor(20)
                .closeInterstitial();
    }

    @Test
    public void testPrebidPpmInterstitial320x480() {
        final String exampleName = getStringResource(R.string.demo_bidding_in_app_interstitial_320_480);
        homePage.setUseMockServer(false)
                .getInterstitialPageFactory()
                .goToPpmInterstitialExample(exampleName)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    private void verifyPpmHtmlInterstitialExampleWithValidBid(String exampleName) {
        homePage.getInterstitialPageFactory()
                .goToPpmInterstitialExample(exampleName)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .showPrebidInterstitial()
                .htmlCreativeShouldBePresent()
                .clickInterstitial()
                .browserShouldOpen()
                .closeBrowser()
                .htmlCreativeShouldBePresent()
                .closeInterstitial()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }
}
