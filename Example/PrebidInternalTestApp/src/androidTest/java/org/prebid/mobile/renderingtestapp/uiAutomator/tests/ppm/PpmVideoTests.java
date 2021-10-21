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

public class PpmVideoTests extends BaseUiAutomatorTest {
    // TODO: 2/8/21 Remove commented out lines when refactoring expand on click action
    @Test
    public void testOutstreamVideoAdEvents() throws InterruptedException {
        homePage.getBannerPageFactory()
                .goToPpmBannerVideoExample(getStringResource(R.string.demo_bidding_in_app_banner_video_outstream))
                .isLoaded()
                // .sleepFor(2)
                // .clickFullScreen()
                // .clickLearnMore()
                // .browserShouldOpen()
                // .closeBrowser()
                // .closeFullScreen()
                .waitTillVideoEnd()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                // .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testOutstreamVideoAdWithEndCardEvents() throws InterruptedException {
        homePage.getBannerPageFactory()
                .goToPpmBannerVideoExample(getStringResource(R.string.demo_bidding_in_app_banner_video_outstream_end_card))
                .isLoaded()
                // .sleepFor(2)
                // .clickFullScreen()
                // .clickLearnMore()
                // .browserShouldOpen()
                // .closeBrowser()
                .waitTillVideoEnd()
                .clickEndCard()
                .closeBrowser()
                // .closeFullScreen()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testOutstreamVideoNoBids() {
        homePage.setUseMockServer(false)
                .getBannerPageFactory()
                .goToPpmBannerVideoExample(getStringResource(R.string.demo_bidding_in_app_banner_video_outstream_no_bids))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdFailed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdClicked);
    }

    @Test
    public void testPpmVideoInterstitial320x480() {
        verifyPpmVideoInterstitialExampleWithValidBid(R.string.demo_bidding_in_app_interstitial_video_320_480);
    }

    @Test
    public void testPpmVideoInterstitial320x480NoBids() {
        verifyPpmVideoInterstitialNoBids(R.string.demo_bidding_in_app_interstitial_video_320_480_no_bids);
    }

    @Test
    public void testPpmVideoRewardedEndCard320x480() {
        final String exampleName = getStringResource(R.string.demo_bidding_in_app_video_rewarded_end_card_320_480);
        final int videoDurationTimeout = 20 * 1000;

        homePage.getInterstitialPageFactory()
                .goToPpmInterstitialExample(exampleName)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .showPrebidInterstitial()
                .videoCreativeWithShouldBePresent()
                .closeInterstitial(videoDurationTimeout)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed, videoDurationTimeout)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPpmVideoRewardedEndCard320x480NoBids() {
        verifyPpmVideoInterstitialNoBids(R.string.demo_bidding_in_app_video_rewarded_end_card_320_480_no_bids);
    }

    @Test
    public void testPpmOffsetVideo() throws InterruptedException {
        homePage.getInterstitialPageFactory()
                .goToPpmInterstitialExample(getStringResource(R.string.demo_bidding_in_app_interstitial_video_320_480_skipoffset))
                .showPrebidInterstitial()
                .videoCreativeWithShouldBePresent()
                .closeButtonShouldBeDisplayedAfter(7)
                .learnMore()
                .browserShouldOpen()
                .closeBrowser()
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testPpmRewardedVideo() throws InterruptedException {
        homePage.getInterstitialPageFactory()
                .goToPpmInterstitialExample(getStringResource(R.string.demo_bidding_in_app_video_rewarded_320_480))
                .showPrebidInterstitial()
                .videoCreativeShouldBePresent()
                .learnMore()
                .browserShouldOpen()
                .closeBrowser()
                .videoCreativeShouldNotBePresent()
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testPrebidOutstreamVideoAdEvents() throws InterruptedException {
        homePage.setUseMockServer(false)
                .getBannerPageFactory()
                .goToPpmBannerVideoExample(getStringResource(R.string.demo_bidding_in_app_banner_video_outstream))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPrebidPpmVideoRewardedEndCard320x480() {
        final String exampleName = getStringResource(R.string.demo_bidding_in_app_video_rewarded_end_card_320_480);
        homePage.setUseMockServer(false)
                .getInterstitialPageFactory()
                .goToPpmInterstitialExample(exampleName)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    private void verifyPpmVideoInterstitialNoBids(int stringResId) {
        final String exampleName = getStringResource(stringResId);

        homePage.setUseMockServer(false)
                .getInterstitialPageFactory()
                .goToPpmInterstitialExample(exampleName)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdFailed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdClicked);
    }

    private void verifyPpmVideoInterstitialExampleWithValidBid(int stringResId) {
        final String exampleName = getStringResource(stringResId);

        homePage.getInterstitialPageFactory()
                .goToPpmInterstitialExample(exampleName)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .showPrebidInterstitial()
                .videoCreativeWithShouldBePresent()
                .closeInterstitial()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }
}
