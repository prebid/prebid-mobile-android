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

package org.prebid.mobile.renderingtestapp.uiAutomator.tests.ppm;

import org.junit.Test;
import org.prebid.mobile.renderingtestapp.R;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.utils.BaseUiAutomatorTest;

public class PpmBannerTests extends BaseUiAutomatorTest {
    @Test
    public void testPpmBanner320x50() {
        homePage.getBannerPageFactory()
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_in_app_banner_320_50))
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
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_in_app_banner_300_250))
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
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_in_app_banner_728_90))
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
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_in_app_banner_multisize))
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
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_in_app_banner_layout))
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPpmBannerNoBids() {
        homePage.setUseMockServer(false)
                .getBannerPageFactory()
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_in_app_banner_320_50_no_bids))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdFailed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdClicked);
    }

    @Test
    public void testPpmBannerWithIncorrectDataVast() {
        homePage.getBannerPageFactory()
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_in_app_banner_320_50_vast))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdFailed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdClicked);
    }

    @Test
    public void testPrebidPpmBanner320x50() {
        homePage.setUseMockServer(false)
                .getBannerPageFactory()
                .goToPpmBannerExample(getStringResource(R.string.demo_bidding_in_app_banner_320_50))
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }
}
