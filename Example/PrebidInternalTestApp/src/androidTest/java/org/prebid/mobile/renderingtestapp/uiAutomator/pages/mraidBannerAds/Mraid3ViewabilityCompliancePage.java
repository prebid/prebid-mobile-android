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
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class Mraid3ViewabilityCompliancePage extends MraidBasicPage {

    private static class Locators {
        static BySelector bannerCreative = By.res("viewabilityChart");
        static BySelector exposureError = By.textContains("exposureChange event is not compliant with IAB specification");
        static BySelector mraidEnvError = By.textContains("Environment is not MRAIDV3 Compatible because");
    }

    public Mraid3ViewabilityCompliancePage(UiDevice device) {
        super(device);
    }

    public Mraid3ViewabilityCompliancePage bannerShouldLoad() {
        assertNotNull("Banner is not displayed",
                      device.wait(Until.findObject(Locators.bannerCreative), TIMEOUT * 2));
        return this;
    }

    public Mraid3ViewabilityCompliancePage verifyNoErrorsArePresent() {
        final UiObject2 exposureError = device.wait(Until.findObject(Locators.exposureError), TIMEOUT);
        final UiObject2 mraidEnvError = device.wait(Until.findObject(Locators.mraidEnvError), TIMEOUT);
        assertNull("Exposure error present!", exposureError);
        assertNull("MraidEnv error present!", mraidEnvError);
        return this;
    }
}
