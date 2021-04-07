package com.openx.internal_test_app.uiAutomator.tests.mopub;

import com.openx.internal_test_app.R;
import com.openx.internal_test_app.uiAutomator.utils.BaseUiAutomatorTest;

import org.junit.Test;

public class MoPubVideoTests extends BaseUiAutomatorTest {
    @Test
    public void testMoPubVideoInterstitialNoBids() {
        homePage.setUseMockServer(false)
                .getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_video_interstitial_320_480_no_bids))
                .showPrebidInterstitial()
                .checkFullScreenHint()
                .waitTillVideoEnds()
                .closeInterstitial()
                .commonInterstitialEventsShouldPresent();
    }

    @Test
    public void testMoPubVideoInterstitialRandom() {
        homePage.getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_video_interstitial_320_480_random))
                .showPrebidInterstitial()
                .checkFullScreenHint()
                .waitTillVideoEnds()
                .closeInterstitial()
                .commonInterstitialEventsShouldPresent();
    }

    @Test
    public void testMoPubVideoInterstitialAdapter() {
        homePage.getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_video_interstitial_320_480_adapter))
                .showPrebidInterstitial()
                .waitTillVideoEnds()
                .commonInterstitialEventsShouldPresent();
    }

    @Test
    public void testMoPubRewardedVideoEndCardNoBids() {
        homePage.setUseMockServer(false)
                .getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_rewarded_video_end_card_320_480_no_bids))
                .showPrebidInterstitial()
                .checkFullScreenHint()
                .waitTillVideoEnds()
                .closeInterstitial()
                .commonRewardedVideoEventsShouldPresent();
    }

    @Test
    public void testMoPubRewardedVideoEndCardRandom() {
        homePage.getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_rewarded_video_end_card_320_480_random))
                .showPrebidInterstitial()
                .checkFullScreenHint()
                .waitTillVideoEnds()
                .closeInterstitial()
                .commonRewardedVideoEventsShouldPresent();
    }

    @Test
    public void testMoPubRewardedVideoEndCardAdapter() {
        homePage.getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_rewarded_video_end_card_320_480_adapter))
                .showPrebidInterstitial()
                .waitTillVideoEnds()
                .closeInterstitial()
                .commonRewardedVideoEventsShouldPresent();
    }

    @Test
    public void testMoPubRewardedVideo() throws InterruptedException {
        homePage.getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_rewarded_video_320_480_adapter))
                .showPrebidInterstitial()
                .learnMore()
                .browserShouldOpen()
                .closeBrowser()
                .waitTillVideoEnds()
                .commonRewardedVideoEventsShouldPresent();
    }

    @Test
    public void testApolloMoPubRewardedVideoEndCardAdapter() {
        homePage.setUseMockServer(false)
                .getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_rewarded_video_end_card_320_480_adapter))
                .showPrebidInterstitial()
                .waitTillVideoEnds()
                .closeInterstitial()
                .commonRewardedVideoEventsShouldPresent();
    }
}
