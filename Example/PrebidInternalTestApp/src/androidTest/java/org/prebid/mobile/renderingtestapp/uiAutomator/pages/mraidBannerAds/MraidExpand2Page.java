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

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertNotNull;

;

public class MraidExpand2Page extends AdBasePage<MraidExpand2Page> {

    private static class Locators {
        static BySelector bannerCreative = getWebViewSelectorVersionDepends("Two Part Expand");
        static BySelector clickToClose = getWebViewSelectorVersionDepends("Click here to close.");
        static BySelector openIAB = getWebViewSelectorVersionDepends("Open IAB.net");
        static BySelector playVideo = getWebViewSelectorVersionDepends("PlayVideo");
        static BySelector expandAgain = getWebViewSelectorVersionDepends("Expand Again");
        static BySelector videoCreative = By.clazz("android.widget.VideoView");
    }

    public MraidExpand2Page(UiDevice device) {
        super(device);
    }

    public MraidExpand2Page clickBanner() {
        clickOnView(Locators.bannerCreative, TIMEOUT);
        return this;
    }

    public MraidExpand2Page bannerShouldLoad() {
        assertNotNull("Banner is not displayed",
                      device.wait(Until.findObject(Locators.bannerCreative), TIMEOUT));
        return this;
    }

    public MraidExpand2Page adShouldBeExpanded() {
        assertNotNull("<Click to close> is not displayed",
                      device.wait(Until.findObject(Locators.clickToClose), TIMEOUT));
        assertNotNull("<Open IAB.net> is not displayed",
                      device.wait(Until.findObject(Locators.openIAB), TIMEOUT));
        assertNotNull("<Play Video> is not displayed",
                      device.wait(Until.findObject(Locators.playVideo), TIMEOUT));
        assertNotNull("<Expand Again> is not displayed",
                      device.wait(Until.findObject(Locators.expandAgain), TIMEOUT));
        return this;
    }

    public MraidExpand2Page closeExpandedAd() {
        device.wait(Until.findObject(Locators.clickToClose), TIMEOUT).click();
        return this;
    }

    public MraidExpand2Page openInBrowser() {
        clickOnView(Locators.openIAB, TIMEOUT);
        return this;
    }

    public MraidExpand2Page playVideo() {
        clickOnView(Locators.playVideo, TIMEOUT);
        return this;
    }

    public MraidExpand2Page expandAgain() {
        clickOnView(Locators.expandAgain, TIMEOUT);
        return this;
    }

    public MraidExpand2Page videoShouldBePlayed() {
        assertNotNull("Video is not played",
                      device.wait(Until.findObject(Locators.videoCreative), TIMEOUT));
        return this;
    }

    private boolean isVideoPlaying() {
        return device.wait(Until.findObject(Locators.videoCreative), 500) != null;
    }

    public MraidExpand2Page closeVideoPlayer() {
        while (isVideoPlaying()) {
            device.pressBack();
        }
        return this;
    }
}
