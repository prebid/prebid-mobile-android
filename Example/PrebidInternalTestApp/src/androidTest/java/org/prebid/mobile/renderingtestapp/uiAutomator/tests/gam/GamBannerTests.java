/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
