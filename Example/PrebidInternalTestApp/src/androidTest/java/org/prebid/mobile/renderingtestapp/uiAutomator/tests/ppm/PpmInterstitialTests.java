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
                .htmlCreativeWithAdIndicatorShouldBePresent()
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
