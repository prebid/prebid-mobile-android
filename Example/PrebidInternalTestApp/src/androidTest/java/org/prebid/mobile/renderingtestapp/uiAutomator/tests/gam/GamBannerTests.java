package org.prebid.mobile.renderingtestapp.uiAutomator.tests.gam;

import org.junit.Test;
import org.prebid.mobile.renderingtestapp.R;
import org.prebid.mobile.renderingtestapp.uiAutomator.utils.BaseUiAutomatorTest;

public class GamBannerTests extends BaseUiAutomatorTest {
    @Test
    public void testGamBanner320x50AppEvent() {
        homePage.getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_banner_320_50_app_event))
                .prebidViewShouldBePresent()
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
                .prebidViewShouldBePresent()
                .checkCommonEvents();
    }

    @Test
    public void testGamBanner728x90() {
        homePage.getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_banner_728_90))
                .prebidViewShouldBePresent()
                .checkCommonEvents();
    }

    @Test
    public void testGamBannerMultisize() {
        homePage.getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_banner_multisize))
                .prebidViewShouldBePresent()
                .checkCommonEvents();
    }

    @Test
    public void testPrebidGamBanner320x50AppEvent() {
        homePage.setUseMockServer(false)
                .getBannerPageFactory()
                .goToGamBannerExample(getStringResource(R.string.demo_bidding_gam_banner_320_50_app_event))
                .prebidViewShouldBePresent()
                .checkCommonEvents();
    }
}
