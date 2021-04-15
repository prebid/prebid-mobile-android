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

import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertNotNull;

public class MoPubNativeStylesPage extends AdBasePage<MoPubNativeStylesPage> {
    private static final int TIMEOUT = 15000;

    private static class Locators {
        static BySelector nativeStylesCreative = getWebViewSelectorVersionDepends("OpenX (Brand)");
        static BySelector ctaLocator = getWebViewSelectorVersionDepends("Click here to visit our site!");
    }

    public MoPubNativeStylesPage(UiDevice device) {
        super(device);
    }

    public MoPubNativeStylesPage nativeShouldLoad() {
        assertNotNull("Native is not displayed", device.wait(Until.findObject(Locators.nativeStylesCreative), TIMEOUT));
        return this;
    }

    public MoPubNativeStylesPage clickCta() {
        clickOnView(Locators.ctaLocator, TIMEOUT);
        return this;
    }
}
