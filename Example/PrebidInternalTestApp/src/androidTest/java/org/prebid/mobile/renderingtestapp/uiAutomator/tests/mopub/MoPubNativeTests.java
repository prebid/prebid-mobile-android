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

import androidx.test.uiautomator.By;

import org.junit.Test;
import org.prebid.mobile.renderingtestapp.R;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.utils.BaseUiAutomatorTest;

public class MoPubNativeTests extends BaseUiAutomatorTest {
    @Test
    public void testMoPubNativeStyles() throws InterruptedException {
        homePage.getNativePageFactory()
                .goToMoPubNativeStyles(getStringResource(R.string.demo_bidding_mopub_native_styles))
                .nativeShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }

    @Test
    public void testMoPubNativeStylesNoAssets() {
        homePage.getNativePageFactory()
                .goToMoPubNativeStyles(getStringResource(R.string.demo_bidding_mopub_native_styles_no_assets))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }

    @Test
    public void testPrebidMoPubNativeStyles() throws InterruptedException {
        homePage.setUseMockServer(false)
                .getNativePageFactory()
                .goToMoPubNativeStyles(getStringResource(R.string.demo_bidding_mopub_native_styles))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }

    @Test
    public void testMoPubNative() {
        homePage.getNativePageFactory()
                .goToMoPubNativeStyles(getStringResource(R.string.demo_bidding_mopub_native_adapter))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.fetchDemandSuccess)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.nativeAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.impressionEvent)
                .clickOnView(By.res(INTERNAL_APP_PACKAGE, "btnNativeAction"), TIMEOUT)
                .browserShouldOpen()
                .closeBrowser()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.nativeAdFailed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.fetchDemandFailure);
    }
}
