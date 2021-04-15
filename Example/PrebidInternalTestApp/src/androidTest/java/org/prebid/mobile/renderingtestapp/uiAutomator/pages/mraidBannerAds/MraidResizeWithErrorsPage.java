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

import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;

import static androidx.test.uiautomator.By.copy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class MraidResizeWithErrorsPage extends AdBasePage<MraidResizeWithErrorsPage> {

    private static final int CLOSE_BUTTON_TIMEOUT = 1000;

    private static class Locators {
        static BySelector bannerCreative = getWebViewSelectorVersionDepends("Test properties:");
        static BySelector badTiming = getWebViewSelectorVersionDepends("1bad timing");
        static BySelector badValues = getWebViewSelectorVersionDepends("2bad values");
        static BySelector tooSmall = isAtLeastAPI25()
                                     ? getWebViewSelectorVersionDepends("too\n"
                                                                        + "small")
                                     : getWebViewSelectorVersionDepends("small");
        static BySelector tooBig = isAtLeastAPI25()
                                   ? getWebViewSelectorVersionDepends("too\n"
                                                                      + "big")
                                   : getWebViewSelectorVersionDepends("big");
        static BySelector iabLogo = getWebViewSelectorVersionDepends("logo");
        static BySelector toggleOffscreen = getWebViewSelectorVersionDepends("toggleOffscreenDiv");
        static BySelector toggleOffscreenTrue = getWebViewSelectorVersionDepends("TRUE");
        static BySelector toggleOffscreenFalse = getWebViewSelectorVersionDepends("FALSE");
        static BySelector resizeLeft = getWebViewSelectorVersionDepends("←");
        static BySelector resizeRight = getWebViewSelectorVersionDepends("→");
        static BySelector resizeUp = getWebViewSelectorVersionDepends("↑");
        static BySelector resizeDown = getWebViewSelectorVersionDepends("↓");
        static BySelector closeButton = getWebViewSelectorVersionDepends("X");
    }

    public MraidResizeWithErrorsPage(UiDevice device) {
        super(device);
    }

    public MraidResizeWithErrorsPage adShouldBeDisplayed() {
        assertNotNull("Ad is not displayed",
                      device.wait(Until.findObject(Locators.bannerCreative), TIMEOUT));
        assertNotNull("<bad timing> is not displayed",
                      device.wait(Until.findObject(Locators.badTiming), TIMEOUT));
        assertNotNull("<bad values> is not displayed",
                      device.wait(Until.findObject(Locators.badValues), TIMEOUT));
        assertNotNull("<too small> is not displayed",
                      device.wait(Until.findObject(Locators.tooSmall), TIMEOUT));
        assertNotNull("<too big> is not displayed",
                      device.wait(Until.findObject(Locators.tooBig), TIMEOUT));
        return this;
    }

    private void setOffscreen(String state) {
        UiObject2 toggleOffscreen;
        BySelector changedStateSelector;
        if (state.equals("TRUE")) {
            toggleOffscreen = device.findObject(Locators.toggleOffscreenFalse);
            changedStateSelector = Locators.toggleOffscreenTrue;
        }
        else {
            toggleOffscreen = device.findObject(Locators.toggleOffscreenTrue);
            changedStateSelector = Locators.toggleOffscreenFalse;
        }

        if (toggleOffscreen != null && !getViewDescVersionDepends(toggleOffscreen).equals(state)) {
            toggleOffscreen.click();
            assertNotNull(String.format("Offscreen should be: <%s> but is: <%s>",
                                        state, toggleOffscreen.getText()),
                          device.wait(Until.findObject(copy(changedStateSelector)), TIMEOUT));
        }
    }

    public MraidResizeWithErrorsPage setOffscreenTrue() {
        setOffscreen("TRUE");
        return this;
    }

    public MraidResizeWithErrorsPage setOffscreenFalse() {
        setOffscreen("FALSE");
        return this;
    }

    public MraidResizeWithErrorsPage resizeLeft() {
        clickOnView(Locators.resizeLeft, TIMEOUT);
        return this;
    }

    public MraidResizeWithErrorsPage resizeRight() {
        clickOnView(Locators.resizeRight, TIMEOUT);
        return this;
    }

    public MraidResizeWithErrorsPage resizeUp() {
        clickOnView(Locators.resizeUp, TIMEOUT);
        return this;
    }

    public MraidResizeWithErrorsPage resizeDown() {
        clickOnView(Locators.resizeDown, TIMEOUT);
        return this;
    }

    private boolean isCloseButtonDisplayed() {
        try { // needed because test was catching intermediate state
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return device.wait(Until.findObject(Locators.closeButton), CLOSE_BUTTON_TIMEOUT) != null;
    }

    public MraidResizeWithErrorsPage closeButtonShouldNotBeDisplayed() {
        assertFalse("Close button should not be displayed", isCloseButtonDisplayed());
        return this;
    }

    public MraidResizeWithErrorsPage closeResizedAd() {
        clickOnView(Locators.closeButton, TIMEOUT);
        return this;
    }
}
