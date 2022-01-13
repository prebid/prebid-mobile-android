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

package org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.ppm;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertTrue;

public class PpmNativePage extends AdBasePage<PpmNativePage> {

    private static class Locators {
        static BySelector videoCreative = By.res(TAG, "exo_content_frame");

        static BySelector btnPause = By.res(TAG, "btnPause");
        static BySelector btnResume = By.res(TAG, "btnResume");
        static BySelector btnMute = By.res(TAG, "btnMute");
        static BySelector btnUnMute = By.res(TAG, "btnUnMute");
    }

    public PpmNativePage(UiDevice device) {
        super(device);
    }

    public PpmNativePage videoCreativeShouldBePresent() {
        final boolean isCreativeDisplayed = device.wait(Until.findObject(Locators.videoCreative), TIMEOUT) != null;

        assertTrue("Native video is not displayed", isCreativeDisplayed);
        return this;
    }

    public PpmNativePage clickPause() {
        clickOnView(Locators.btnPause, TIMEOUT);
        return this;
    }

    public PpmNativePage clickResume() {
        clickOnView(Locators.btnResume, TIMEOUT);
        return this;
    }

    public PpmNativePage clickMute() {
        clickOnView(Locators.btnMute, TIMEOUT);
        return this;
    }

    public PpmNativePage clickUnMute() {
        clickOnView(Locators.btnUnMute, TIMEOUT);
        return this;
    }
}
