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
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.mraidBannerAds.Mraid3LoadAndEventsPage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.mraidBannerAds.Mraid3ResizeNegativePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.mraidBannerAds.Mraid3TestMethods;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.mraidBannerAds.Mraid3TestProperties;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.mraidBannerAds.Mraid3ViewabilityCompliancePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.mraidBannerAds.MraidExpand1Page;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.mraidBannerAds.MraidExpand2Page;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.mraidBannerAds.MraidResizePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.mraidBannerAds.MraidResizeWithErrorsPage;

public class MraidPageFactory extends PageFactory {

    public MraidPageFactory(UiDevice device) {
        super(device);
    }

    public MraidExpand2Page goToMraidExpand2(String exampleName) {
        findListItem(exampleName);
        return new MraidExpand2Page(device);
    }

    public MraidResizeWithErrorsPage goToMraidResizeErrors(String exampleName) {
        findListItem(exampleName);
        return new MraidResizeWithErrorsPage(device);
    }

    public Mraid3TestMethods goToMraid3TestMethods(String exampleName) {
        findListItem(exampleName);
        return new Mraid3TestMethods(device);
    }

    public Mraid3TestProperties goToMraid3TestProperties(String exampleName) {
        findListItem(exampleName);
        return new Mraid3TestProperties(device);
    }

    public Mraid3ViewabilityCompliancePage goToMraid3ViewabilityCompliancePage(String exampleName) {
        findListItem(exampleName);
        return new Mraid3ViewabilityCompliancePage(device);
    }

    public Mraid3ResizeNegativePage goToMraid3ResizeNegativePage(String exampleName) {
        findListItem(exampleName);
        return new Mraid3ResizeNegativePage(device);
    }

    public Mraid3LoadAndEventsPage goToMraid3LoadAndEvents(String exampleName) {
        findListItem(exampleName);
        return new Mraid3LoadAndEventsPage(device);
    }

    public MraidExpand1Page goToMraidExpand(String exampleName) {
        findListItem(exampleName);
        return new MraidExpand1Page(device);
    }

    public MraidResizePage goToMraidResize(String exampleName) {
        findListItem(exampleName);
        return new MraidResizePage(device);
    }
}
