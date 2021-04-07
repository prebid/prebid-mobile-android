package com.openx.internal_test_app.uiAutomator.tests.gam;

import com.openx.internal_test_app.R;
import com.openx.internal_test_app.uiAutomator.utils.BaseUiAutomatorTest;

import org.junit.Test;

public class GamBannerTests extends BaseUiAutomatorTest {
    @Test
    public void testGamBanner320x50AppEvent() {
        homePage.getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_banner_320_50_app_event))
                .openxViewShouldBePresent()
                .checkCommonEvents();
    }

    @Test
    public void testGamBanner320x50GamAd() {
        homePage.getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_banner_320_50_gam_ad))
                .gamViewShouldBePresent()
                .checkCommonEvents();
    }

    @Test
    public void testGamBanner320x50NoBids() {
        homePage.setUseMockServer(false)
                .getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_banner_320_50_no_bids))
                .gamViewShouldBePresent()
                .checkCommonEvents();
    }

    @Test
    public void testGamBanner320x50Random() {
        homePage.getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_banner_320_50_random))
                .checkCommonEvents();
    }

    @Test
    public void testGamBanner300x250() {
        homePage.getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_banner_300_250))
                .openxViewShouldBePresent()
                .checkCommonEvents();
    }

    @Test
    public void testGamBanner728x90() {
        homePage.getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_banner_728_90))
                .openxViewShouldBePresent()
                .checkCommonEvents();
    }

    @Test
    public void testGamBannerMultisize() {
        homePage.getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_banner_multisize))
                .openxViewShouldBePresent()
                .checkCommonEvents();
    }

    @Test
    public void testApolloGamBanner320x50AppEvent() {
        homePage.setUseMockServer(false)
                .getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_banner_320_50_app_event))
                .openxViewShouldBePresent()
                .checkCommonEvents();
    }
}
