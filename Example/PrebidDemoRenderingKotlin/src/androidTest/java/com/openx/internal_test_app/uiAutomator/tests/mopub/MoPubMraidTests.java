package com.openx.internal_test_app.uiAutomator.tests.mopub;

import com.openx.internal_test_app.R;
import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;
import com.openx.internal_test_app.uiAutomator.utils.BaseUiAutomatorTest;

import org.junit.Test;

public class MoPubMraidTests extends BaseUiAutomatorTest {
    @Test
    public void testMraidExpand() {
        homePage.getMraidPageFactory()
                .goToMraidExpand(getStringResource(R.string.demo_bidding_mopub_mraid_expand))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldBeExpanded()
                .closeExpandedAd()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adWasClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidCollapse)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }

    @Test
    public void testMraidResize() {
        homePage.getMraidPageFactory()
                .goToMraidResize(getStringResource(R.string.demo_bidding_mopub_mraid_resize))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldResize()
                .closeInterstitial()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }

    @Test
    public void testApolloMraidResize() {
        homePage.setUseMockServer(false)
                .getMraidPageFactory()
                .goToMraidResize(getStringResource(R.string.demo_bidding_mopub_mraid_resize))
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }
}
