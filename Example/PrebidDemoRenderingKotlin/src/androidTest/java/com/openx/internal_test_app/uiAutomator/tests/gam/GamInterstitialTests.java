package com.openx.internal_test_app.uiAutomator.tests.gam;

import com.openx.internal_test_app.R;
import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;
import com.openx.internal_test_app.uiAutomator.utils.BaseUiAutomatorTest;

import org.junit.Test;

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
                .gamOrOpenXHtmlCreativeShouldBePresent()
                .sleepFor(20)
                .closeInterstitial();
    }

    @Test
    public void testApolloGamInterstitial320x480AppEvent() {
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
                .gamOrOpenXHtmlCreativeShouldBePresent()
                .clickInterstitial()
                .goBackOnce()
                .closeInterstitial()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }
}
