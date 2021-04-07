package com.openx.internal_test_app.uiAutomator.tests.mopub;

import com.openx.internal_test_app.R;
import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;
import com.openx.internal_test_app.uiAutomator.utils.BaseUiAutomatorTest;

import org.junit.Test;

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
    public void testApolloMoPubInterstitialAdapter() {
        homePage.setUseMockServer(false)
                .getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_interstitial_320_480_adapter))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }
}
