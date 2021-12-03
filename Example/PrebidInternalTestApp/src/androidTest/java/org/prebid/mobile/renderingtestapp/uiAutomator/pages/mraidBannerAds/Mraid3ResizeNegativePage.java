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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class Mraid3ResizeNegativePage extends MraidBasicPage {

    private static class Locators {
        static BySelector btnSdkClose = By.res(TAG, "iv_close_interstitial");

        static List<BySelector> testCaseLabelList = Arrays.asList(
            getWebViewSelectorVersionDependsByContains("PASSED: [Resizing without setting size properties first should trigger an error.]"),
            getWebViewSelectorVersionDependsByContains("PASSED: [Resizing before setting size properties first should trigger an error.]"),
            getWebViewSelectorVersionDependsByContains("PASSED: [Calling setResizeProperties with undefined size properties should trigger an error.]"),
            getWebViewSelectorVersionDependsByContains("PASSED: [Calling setResizeProperties without actual size properties should trigger an error.]"),
            getWebViewSelectorVersionDependsByContains("PASSED: [Calling setResizeProperties with incomplete list of required size properties should trigger an error.]"),
            getWebViewSelectorVersionDependsByContains("PASSED: [Calling setResizeProperties with invalid size properties should trigger an error.]"),
            getWebViewSelectorVersionDependsByContains("PASSED: [Setting ad size less than 50x50 should trigger an error.]"),
            getWebViewSelectorVersionDependsByContains("PASSED: [Setting size more than the ad max size should trigger an error.]"),
            getWebViewSelectorVersionDependsByContains("PASSED: [Setting offsetX that moves close button offscreen should trigger an error.]"),
            getWebViewSelectorVersionDependsByContains("PASSED: [Resizing in expanded state should trigger an error.]"),
            getWebViewSelectorVersionDependsByContains("PASSED: [Setting width that moves close button offscreen should trigger an error.]"),
            getWebViewSelectorVersionDependsByContains("PASSED: [Setting offsetY that moves close button offscreen should trigger an error.]"),
            getWebViewSelectorVersionDependsByContains("PASSED: [Setting offsetX and offsetY that moves close button partially offscreen should trigger an error.]")
        );
    }

    public Mraid3ResizeNegativePage(UiDevice device) {
        super(device);
    }

    public Mraid3ResizeNegativePage verifyTestCasesPassed() {

        for (BySelector testCase : Locators.testCaseLabelList) {
            assertNotNull("Test case failed! " + testCase.toString(),
                          device.wait(Until.findObject(testCase), TIMEOUT));
        }
        return this;
    }

    public Mraid3ResizeNegativePage clickSdkClose() {
        clickOnView(Locators.btnSdkClose, TIMEOUT);
        return this;
    }

    public Mraid3ResizeNegativePage sdkCloseButtonShouldBePresent() {
        assertNotNull("Sdk close button is not displayed!",
                      device.wait(Until.findObject(Locators.btnSdkClose), TIMEOUT));
        return this;
    }
}
