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

import static org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.gam.GamInterstitialPage.VIDEO_DURATION_TIMEOUT;

public class GamVideoTests extends BaseUiAutomatorTest {
    @Test
    public void testGamOutstreamAppEvent() {
        homePage.getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_video_oustream_app_event))
                .prebidViewShouldBePresent()
                .checkCommonEvents();
    }

    @Test
    public void testGamOutstreamNoBids() {
        homePage.getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_video_outstream_no_bids))
                .gamVideoViewShouldBePresent()
                .checkCommonEvents();
    }

    @Test
    public void testGamOutstreamRandom() {
        homePage.getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_video_outstream_random))
                .checkCommonEvents();
    }

    @Test
    public void testGamVideoInterstitial320x480AppEvent() throws InterruptedException {
        verifyGamVideoInterstitialExample(R.string.demo_bidding_gam_interstitial_video_320_480_app_event);
    }

    @Test
    public void testGamVideoInterstitial320x480NoBids() throws InterruptedException {
        verifyGamVideoInterstitialExample(R.string.demo_bidding_gam_interstitial_video_320_480_no_bids);
    }

    @Test
    public void testGamVideoInterstitial320x480Random() {
        verifyGamVideoInterstitialRandom(R.string.demo_bidding_gam_interstitial_video_320_480_random);
    }

    @Test
    public void testGamVideoRewardedEndCard320x480Metadata() {
        verifyGamVideoRewardedInterstitialExample(R.string.demo_bidding_gam_video_rewarded_end_card_320_480_metadata);
    }

    @Test
    public void testGamVideoRewardedEndCard320x480NoBids() {
        verifyGamVideoRewardedInterstitialExample(R.string.demo_bidding_gam_video_rewarded_end_card_320_480_no_bids);
    }

    @Test
    public void testGamVideoRewardedEndCard320x480Random() {
        verifyGamVideoRewardedInterstitialExample(R.string.demo_bidding_gam_video_rewarded_end_card_320_480_random);
    }

    @Test
    public void testGamRewardedVideo() throws InterruptedException {
        verifyGamVideoInterstitialExample(R.string.demo_bidding_gam_video_rewarded_320_480_metadata);
    }

    @Test
    public void testPrebidGamOutstreamAppEvent() {
        homePage.getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_video_oustream_app_event))
                .prebidViewShouldBePresent()
                .checkCommonEvents();
    }

    @Test
    public void testPrebidGamRewardedVideo() throws InterruptedException {
        String exampleName = getStringResource(R.string.demo_bidding_gam_video_rewarded_end_card_320_480_metadata);
        homePage.getInterstitialPageFactory()
                .goToGamInterstitialExample(exampleName)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    private void verifyGamVideoInterstitialExample(int stringResId) throws InterruptedException {
        String exampleName = getStringResource(stringResId);

        homePage.getInterstitialPageFactory()
                .goToGamInterstitialExample(exampleName)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .showPrebidInterstitial()
                .gamOrPrebidVideoCreativeShouldBePresent()
                .closeEndCard()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed, VIDEO_DURATION_TIMEOUT) // wait until video is finished
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    private void verifyGamVideoRewardedInterstitialExample(int stringResId) {
        String exampleName = getStringResource(stringResId);

        try {
            homePage.getInterstitialPageFactory()
                    .goToGamInterstitialExample(exampleName)
                    .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                    .showPrebidInterstitial()
                    .closeEndCard()
                    .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                    .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed)
                    .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void verifyGamVideoInterstitialRandom(int stringResId) {
        final String exampleName = getStringResource(stringResId);

        homePage.getInterstitialPageFactory()
                .goToGamInterstitialExample(exampleName)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .showPrebidInterstitial()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed, VIDEO_DURATION_TIMEOUT) // wait until video is finished
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }
}
