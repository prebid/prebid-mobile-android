package com.openx.internal_test_app.uiAutomator.tests.mopub;

import com.openx.internal_test_app.R;
import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;
import com.openx.internal_test_app.uiAutomator.utils.BaseUiAutomatorTest;

import org.junit.Test;

public class MoPubBannerTests extends BaseUiAutomatorTest {
    @Test
    public void testMoPubBanner320x50Adapter() {
        homePage.getBannerPageFactory()
                .goToBiddingMoPubBannerExample(getStringResource(R.string.demo_bidding_mopub_banner_320_50_adapter))
                .bannerShouldLoad()
                .clickBanner()
                .browserShouldOpen()
                .closeBrowser()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adWasClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }

    @Test
    public void testMoPubBanner320x50NoBids() {
        homePage.setUseMockServer(false)
                .getBannerPageFactory()
                .goToBiddingMoPubBannerExample(getStringResource(R.string.demo_bidding_mopub_banner_320_50_no_bids))
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }

    @Test
    public void testMoPubBanner320x50Random() {
        homePage.getBannerPageFactory()
                .goToBiddingMoPubBannerExample(getStringResource(R.string.demo_bidding_mopub_banner_320_50_random))
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }

    @Test
    public void testMoPubBanner300x250() {
        homePage.getBannerPageFactory()
                .goToBiddingMoPubBannerExample(getStringResource(R.string.demo_bidding_mopub_banner_300_250))
                .bannerShouldLoad()
                .clickBanner()
                .browserShouldOpen()
                .closeBrowser()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adWasClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }

    @Test
    public void testMoPubBanner728x90() {
        homePage.getBannerPageFactory()
                .goToBiddingMoPubBannerExample(getStringResource(R.string.demo_bidding_mopub_banner_728_90))
                .bannerShouldLoad()
                .clickBanner()
                .browserShouldOpen()
                .closeBrowser()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adWasClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }

    @Test
    public void testMoPubBannerMultisize() {
        homePage.getBannerPageFactory()
                .goToBiddingMoPubBannerExample(getStringResource(R.string.demo_bidding_mopub_banner_multisize))
                .bannerShouldLoad()
                .clickBanner()
                .browserShouldOpen()
                .closeBrowser()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adWasClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }

    @Test
    public void testApolloMoPubBanner320x50Adapter() {
        homePage.setUseMockServer(false)
                .getBannerPageFactory()
                .goToBiddingMoPubBannerExample(getStringResource(R.string.demo_bidding_mopub_banner_320_50_adapter))
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }
}
