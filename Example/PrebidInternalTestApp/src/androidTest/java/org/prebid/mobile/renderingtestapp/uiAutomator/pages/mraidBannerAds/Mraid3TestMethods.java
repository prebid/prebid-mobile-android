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

package org.prebid.mobile.renderingtestapp.uiAutomator.pages.mraidBannerAds;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertNotNull;

public class Mraid3TestMethods extends MraidBasicPage {
    public Mraid3TestMethods(UiDevice device) {
        super(device);
    }

    private static class Locators {
        static BySelector bannerCreative = getWebViewSelectorVersionDepends("Click to Resize");
        static BySelector resizeButton = getWebViewSelectorVersionDepends("Resize");
        static BySelector resizeInExpandButton = getWebViewSelectorVersionDepends("Resize(320)");
        static BySelector expandButton = getWebViewSelectorVersionDepends("Expand");
        static BySelector closeButton = By.res(TAG, "iv_close_interstitial");
    }

    public Mraid3TestMethods bannerShouldLoad() {
        assertNotNull("Banner is not displayed",
                      device.wait(Until.findObject(Locators.resizeButton), TIMEOUT * 2));
        return this;
    }

    public Mraid3TestMethods clickBanner() {
        clickOnView(Locators.bannerCreative, TIMEOUT);
        return this;
    }

    public Mraid3TestMethods clickResize() {
        clickOnView(Locators.resizeButton, TIMEOUT);
        return this;
    }

    public Mraid3TestMethods clickResizeInExpand() {
        clickOnView(Locators.resizeInExpandButton, TIMEOUT);
        return this;
    }

    public Mraid3TestMethods clickExpand() {
        clickOnView(Locators.expandButton, TIMEOUT);
        return this;
    }

    public Mraid3TestMethods adShouldBeExpanded() {
        assertNotNull("Ad is not expanded",
                      device.wait(Until.findObject(Locators.closeButton), TIMEOUT));
        return this;
    }

    public Mraid3TestMethods adShouldBeResized() {
        assertNotNull("Ad is not resized",
                      device.wait(Until.findObject(Locators.expandButton), TIMEOUT));
        return this;
    }

    public Mraid3TestMethods closeExpandedAd() {
        clickOnView(Locators.closeButton, TIMEOUT);
        return this;
    }
}
