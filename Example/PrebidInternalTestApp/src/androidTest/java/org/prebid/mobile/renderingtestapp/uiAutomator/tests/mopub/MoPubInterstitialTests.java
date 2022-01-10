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

package org.prebid.mobile.renderingtestapp.uiAutomator.tests.mopub;

import org.junit.Test;
import org.prebid.mobile.renderingtestapp.R;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.utils.BaseUiAutomatorTest;

public class MoPubInterstitialTests extends BaseUiAutomatorTest {
    @Test
    public void testMoPubInterstitialAdapter() {
        homePage.getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_interstitial_320_480_adapter))
                .showPrebidInterstitial()
                .closeInterstitial()
                .commonInterstitialEventsShouldPresent();
    }

    @Test
    public void testMoPubInterstitialNoBids() {
        homePage.setUseMockServer(false)
                .getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_interstitial_320_480_no_bids))
                .showPrebidInterstitial()
                .checkFullScreenHint()
                .closeInterstitial()
                .commonInterstitialEventsShouldPresent();
    }

    @Test
    public void testMoPubInterstitialRandom() {
        homePage.getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_interstitial_320_480_random))
                .showPrebidInterstitial()
                .checkFullScreenHint()
                .closeInterstitial()
                .commonInterstitialEventsShouldPresent();
    }

    @Test
    public void testMoPubInterstitialWithBannersCloseAfterUpdate()
    throws InterruptedException {
        homePage.getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_banners_and_interstitial))
                .showPrebidInterstitial()
                .interstitialShouldBeDisplayed()
                .sleepFor(20)
                .closeInterstitial();
    }

    @Test
    public void testPrebidMoPubInterstitialAdapter() {
        homePage.setUseMockServer(false)
                .getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_interstitial_320_480_adapter))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }
}
