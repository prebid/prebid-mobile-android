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

package org.prebid.mobile.renderingtestapp.uiAutomator.pages;

import android.util.Log;

import org.prebid.mobile.renderingtestapp.BuildConfig;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.factory.BannerPageFactory;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.factory.InterstitialPageFactory;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.factory.MraidPageFactory;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.factory.NativePageFactory;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

public class HomePage extends BasePage {

    private static class Locators {

        static BySelector dismissDialog = By.res(TAG, "dismiss_button");
        static BySelector allowButton = By.res("com.android.packageinstaller", "permission_allow_button");
        static BySelector mockServerSwitch = By.res(TAG, "switchUseMock");
        static BySelector gdprSwitch = By.res(TAG, "switchEnableGdpr");
    }

    public HomePage(UiDevice device) {
        super(device);
        setUseMockServer(BuildConfig.FLAVOR == "mock");
        setUseGdpr(false);
    }

    public void dismissWelcomeDialog() {

        try {
            device.wait(Until.findObject(Locators.dismissDialog), 2000)
                  .click();
        }
        catch (NullPointerException e) {
            Log.i(TAG, "dismissWelcomeDialog: not visible");
        }
    }

    public HomePage allowPermissionsIfNeeded() {
        UiObject2 dialogButton = device.wait(Until.findObject(Locators.allowButton), 3000);
        if (dialogButton != null) {
            while (device.findObject(Locators.allowButton) != null) {
                //it can take time for a permission Dialog to close, so we better check if the ALLOW button still exists every time we need to interact with it
                try {
                    device.findObject(Locators.allowButton).click();
                }
                catch (NullPointerException e) {

                }
            }
        }
        return this;
    }

    public BannerPageFactory getBannerPageFactory() {
        return new BannerPageFactory(device);
    }

    public MraidPageFactory getMraidPageFactory() {
        return new MraidPageFactory(device);
    }

    public InterstitialPageFactory getInterstitialPageFactory() {
        return new InterstitialPageFactory(device);
    }

    public NativePageFactory getNativePageFactory() {
        return new NativePageFactory(device);
    }

    public HomePage setUseMockServer(boolean state) {
        changeSwitchState(state, Locators.mockServerSwitch);
        return this;
    }

    public HomePage setUseGdpr(boolean state) {
        changeSwitchState(state, Locators.gdprSwitch);
        return this;
    }

    private void changeSwitchState(boolean state, BySelector selector) {
        UiObject2 switchUiObject = device.wait(Until.findObject(selector), 2000);
        if (switchUiObject != null && switchUiObject.isChecked() != state) {
            switchUiObject.click();
        }
    }
}
