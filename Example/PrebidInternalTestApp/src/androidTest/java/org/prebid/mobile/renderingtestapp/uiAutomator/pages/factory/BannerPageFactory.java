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
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.gam.GamBannerPage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.mopub.MoPubBiddingBannerPage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.ppm.PpmBannerPage;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.ppm.PpmBannerVideoPage;

public class BannerPageFactory extends PageFactory {

    public BannerPageFactory(UiDevice device) {
        super(device);
    }

    public GamBannerPage goToGamBannerExample(String example) {
        findListItem(example);
        return new GamBannerPage(device);
    }

    public PpmBannerPage goToPpmBannerExample(String example) {
        findListItem(example);
        return new PpmBannerPage(device);
    }

    public PpmBannerVideoPage goToPpmBannerVideoExample(String example) {
        findListItem(example);
        return new PpmBannerVideoPage(device);
    }

    public MoPubBiddingBannerPage goToBiddingMoPubBannerExample(String example) {
        findListItem(example);
        return new MoPubBiddingBannerPage(device);
    }
}
