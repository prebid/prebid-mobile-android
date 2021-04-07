package com.openx.internal_test_app.uiAutomator.tests.ppm;

import com.openx.internal_test_app.R;
import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;
import com.openx.internal_test_app.uiAutomator.utils.BaseUiAutomatorTest;

import org.junit.Test;

public class PpmVideoTests extends BaseUiAutomatorTest {
    // TODO: 2/8/21 Remove commented out lines when refactoring expand on click action
    @Test
    public void testOutstreamVideoAdEvents() throws InterruptedException {
        homePage.getBannerPageFactory()
                .goToPpmBannerVideoExample(getStringResource(R.string.demo_bidding_ppm_banner_video_outstream))
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
                .goToPpmBannerVideoExample(getStringResource(R.string.demo_bidding_ppm_banner_video_outstream_end_card))
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
                .goToPpmBannerVideoExample(getStringResource(R.string.demo_bidding_ppm_banner_video_outstream_no_bids))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdFailed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdClicked);
    }

    @Test
    public void testPpmVideoInterstitial320x480() {
        verifyPpmVideoInterstitialExampleWithValidBid(R.string.demo_bidding_ppm_interstitial_video_320_480);
    }

    @Test
    public void testPpmVideoInterstitial320x480NoBids() {
        verifyPpmVideoInterstitialNoBids(R.string.demo_bidding_ppm_interstitial_video_320_480_no_bids);
    }

    @Test
    public void testPpmVideoRewardedEndCard320x480() {
        final String exampleName = getStringResource(R.string.demo_bidding_ppm_video_rewarded_end_card_320_480);
        final int videoDurationTimeout = 20 * 1000;

        homePage.getInterstitialPageFactory()
                .goToPpmInterstitialExample(exampleName)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .showPrebidInterstitial()
                .videoCreativeWithAdIndicatorShouldBePresent()
                .closeInterstitial(videoDurationTimeout)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed, videoDurationTimeout)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPpmVideoRewardedEndCard320x480NoBids() {
        verifyPpmVideoInterstitialNoBids(R.string.demo_bidding_ppm_video_rewarded_end_card_320_480_no_bids);
    }

    @Test
    public void testPpmOffsetVideo() throws InterruptedException {
        homePage.getInterstitialPageFactory()
                .goToPpmInterstitialExample(getStringResource(R.string.demo_bidding_ppm_interstitial_video_320_480_skipoffset))
                .showPrebidInterstitial()
                .videoCreativeWithAdIndicatorShouldBePresent()
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
                .goToPpmInterstitialExample(getStringResource(R.string.demo_bidding_ppm_video_rewarded_320_480))
                .showPrebidInterstitial()
                .videoCreativeShouldBePresent()
                .learnMore()
                .browserShouldOpen()
                .closeBrowser()
                .videoCreativeWithAdIndicatorShouldNotBePresent()
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testApolloOutstreamVideoAdEvents() throws InterruptedException {
        homePage.setUseMockServer(false)
                .getBannerPageFactory()
                .goToPpmBannerVideoExample(getStringResource(R.string.demo_bidding_ppm_banner_video_outstream))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testApolloPpmVideoRewardedEndCard320x480() {
        final String exampleName = getStringResource(R.string.demo_bidding_ppm_video_rewarded_end_card_320_480);
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
                .videoCreativeWithAdIndicatorShouldBePresent()
                .closeInterstitial()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }
}
