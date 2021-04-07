package com.openx.internal_test_app.uiAutomator.tests.ppm;

import com.openx.internal_test_app.R;
import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;
import com.openx.internal_test_app.uiAutomator.utils.BaseUiAutomatorTest;

import org.junit.Test;

public class PpmInterstitialTests extends BaseUiAutomatorTest {
    @Test
    public void testPpmInterstitial320x480() {
        final String exampleName = getStringResource(R.string.demo_bidding_ppm_interstitial_320_480);
        verifyPpmHtmlInterstitialExampleWithValidBid(exampleName);
    }

    @Test
    public void testPpmInterstitial320x480NoBids() {
        final String exampleName = getStringResource(R.string.demo_bidding_ppm_interstitial_320_480_no_bids);
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
                .goToPpmInterstitialExample(getStringResource(R.string.demo_bidding_ppm_banners_and_interstitial))
                .showPrebidInterstitial()
                .htmlCreativeWithAdIndicatorShouldBePresent()
                .sleepFor(20)
                .closeInterstitial();
    }

    @Test
    public void testApolloPpmInterstitial320x480() {
        final String exampleName = getStringResource(R.string.demo_bidding_ppm_interstitial_320_480);
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
                .htmlCreativeWithAdIndicatorShouldBePresent()
                .clickInterstitial()
                .browserShouldOpen()
                .closeBrowser()
                .htmlCreativeWithAdIndicatorShouldBePresent()
                .closeInterstitial()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }
}
