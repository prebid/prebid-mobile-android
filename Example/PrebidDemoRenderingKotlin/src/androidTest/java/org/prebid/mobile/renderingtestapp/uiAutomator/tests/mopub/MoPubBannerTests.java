package org.prebid.mobile.renderingtestapp.uiAutomator.tests.mopub;

import org.junit.Test;
import org.prebid.mobile.renderingtestapp.R;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.utils.BaseUiAutomatorTest;

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
    public void testPrebidMoPubBanner320x50Adapter() {
        homePage.setUseMockServer(false)
                .getBannerPageFactory()
                .goToBiddingMoPubBannerExample(getStringResource(R.string.demo_bidding_mopub_banner_320_50_adapter))
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }
}
