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

package org.prebid.mobile.renderingtestapp.uiAutomator.pages.factory;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;

public class GamNativePage extends AdBasePage<GamNativePage> {

    private static class Locators {
        static BySelector ctaButton = By.res(TAG, "btnNativeAction");
        static BySelector unifiedCtaButton = By.res(TAG, "ad_call_to_action");
    }

    public GamNativePage(UiDevice device) {
        super(device);
    }

    public GamNativePage clickCta() {
        clickOnView(Locators.ctaButton, TIMEOUT);
        return this;
    }

    public GamNativePage clickNativeGamCta() {
        clickOnView(Locators.unifiedCtaButton, TIMEOUT);
        return this;
    }
}
