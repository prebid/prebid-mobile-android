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
import org.prebid.mobile.renderingtestapp.uiAutomator.utils.BaseUiAutomatorTest;

public class GamInterstitialTests extends BaseUiAutomatorTest {
    @Test
    public void testGamInterstitial320x480AppEvent() {
        verifyGamHtmlInterstitialExample(R.string.demo_bidding_gam_interstitial_320_480_app_event);
    }

    @Test
    public void testGamInterstitial320x480NoBids() {
        homePage.setUseMockServer(false);
        verifyGamHtmlInterstitialExample(R.string.demo_bidding_gam_interstitial_320_480_no_bids);
    }

    @Test
    public void testGamInterstitialWithBannersCloseAfterUpdate()
    throws InterruptedException {
        homePage.getInterstitialPageFactory()
                .goToGamInterstitialExample(getStringResource(R.string.demo_bidding_gam_banners_and_interstitial))
                .showPrebidInterstitial()
                .gamOrPrebidHtmlCreativeShouldBePresent()
                .sleepFor(20)
                .closeInterstitial();
    }

    @Test
    public void testPrebidGamInterstitial320x480AppEvent() {
        String exampleName = getStringResource(R.string.demo_bidding_gam_interstitial_320_480_app_event);
        homePage.setUseMockServer(false)
                .getInterstitialPageFactory()
                .goToGamInterstitialExample(exampleName)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    private void verifyGamHtmlInterstitialExample(int stringResId) {
        String exampleName = getStringResource(stringResId);

        homePage.getInterstitialPageFactory()
                .goToGamInterstitialExample(exampleName)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .showPrebidInterstitial()
                .gamOrPrebidHtmlCreativeShouldBePresent()
                .clickInterstitial()
                .goBackOnce()
                .closeInterstitial()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }
}
