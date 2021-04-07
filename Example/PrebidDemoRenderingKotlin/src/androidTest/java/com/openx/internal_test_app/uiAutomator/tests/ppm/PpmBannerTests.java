package com.openx.internal_test_app.uiAutomator.tests.ppm;

import com.openx.internal_test_app.R;
import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;
import com.openx.internal_test_app.uiAutomator.utils.BaseUiAutomatorTest;

import org.junit.Test;

public class PpmBannerTests extends BaseUiAutomatorTest {
    @Test
    public void testPpmBanner320x50() {
        homePage.getBannerPageFactory()
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_ppm_banner_320_50))
                .bannerShouldLoad()
                .clickBanner()
                .browserShouldOpen()
                .closeBrowser()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPpmBanner300x250() {
        homePage.getBannerPageFactory()
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_ppm_banner_300_250))
                .bannerShouldLoad()
                .clickBanner()
                .browserShouldOpen()
                .closeBrowser()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPpmBanner728x90() {
        homePage.getBannerPageFactory()
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_ppm_banner_728_90))
                .bannerShouldLoad()
                .clickBanner()
                .browserShouldOpen()
                .closeBrowser()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPpmBannerMultisize() {
        homePage.getBannerPageFactory()
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_ppm_banner_multisize))
                .bannerShouldLoad()
                .clickBanner()
                .browserShouldOpen()
                .closeBrowser()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPpmBannerLayout() {
        homePage.getBannerPageFactory()
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_ppm_banner_layout))
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPpmBannerNoBids() {
        homePage.setUseMockServer(false)
                .getBannerPageFactory()
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_ppm_banner_320_50_no_bids))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdFailed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdClicked);
    }

    @Test
    public void testPpmBannerWithIncorrectDataVast() {
        homePage.getBannerPageFactory()
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_ppm_banner_320_50_vast))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdFailed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdClicked);
    }

    @Test
    public void testApolloPpmBanner320x50() {
        homePage.setUseMockServer(false)
                .getBannerPageFactory()
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_ppm_banner_320_50))
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }
}
