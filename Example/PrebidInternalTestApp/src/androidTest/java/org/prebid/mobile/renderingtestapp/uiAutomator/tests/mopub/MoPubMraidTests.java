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

package org.prebid.mobile.renderingtestapp.uiAutomator.tests.mopub;

import org.junit.Test;
import org.prebid.mobile.renderingtestapp.R;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.utils.BaseUiAutomatorTest;

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
    public void testPrebidMraidResize() {
        homePage.setUseMockServer(false)
                .getMraidPageFactory()
                .goToMraidResize(getStringResource(R.string.demo_bidding_mopub_mraid_resize))
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }
}
