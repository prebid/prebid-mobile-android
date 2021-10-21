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

package org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.mopub;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertTrue;

public class MoPubBiddingBannerPage extends AdBasePage<MoPubBiddingBannerPage> {
    private static final int TIMEOUT = 15000;

    private static class Locators {
        static BySelector bannerCreative = getWebViewSelectorVersionDepends("mobile-demo-banner-640x100");
        static BySelector bannerView = By.desc("adView");
        static BySelector viewContainer = By.res(TAG,"viewContainer");
    }

    public MoPubBiddingBannerPage(UiDevice device) {
        super(device);
    }

    public MoPubBiddingBannerPage bannerShouldLoad() {
        boolean creativePresent = device.wait(Until.findObject(Locators.bannerCreative), TIMEOUT) != null;
        boolean adViewPresent = device.wait(Until.findObject(Locators.bannerView), TIMEOUT) != null;
        assertTrue("Banner is not displayed", creativePresent || adViewPresent);
        return this;
    }

    public MoPubBiddingBannerPage clickBanner() {
        clickOnView(Locators.viewContainer, TIMEOUT);
        return this;
    }
}
