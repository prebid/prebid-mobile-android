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

import androidx.test.uiautomator.UiDevice;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.PageFactory;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.gam.GamNativeStylesPage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.mopub.MoPubNativeStylesPage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.ppm.PpmNativePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.ppm.PpmNativeStylesPage;

public class NativePageFactory extends PageFactory{
    public NativePageFactory(UiDevice device) {
        super(device);
    }

    public MoPubNativeStylesPage goToMoPubNativeStyles(String example) {
        findListItem(example);
        return new MoPubNativeStylesPage(device);
    }

    public GamNativeStylesPage goToGamNativeStyles(String example) {
        findListItem(example);
        return new GamNativeStylesPage(device);
    }

    public PpmNativeStylesPage goToPpmNativeStyles(String example) {
        findListItem(example);
        return new PpmNativeStylesPage(device);
    }

    public PpmNativePage goToPpmNative(String example) {
        findListItem(example);
        return new PpmNativePage(device);
    }

    public GamNativePage goToGamNative(String example) {
        findListItem(example);
        return new GamNativePage(device);
    }
}
