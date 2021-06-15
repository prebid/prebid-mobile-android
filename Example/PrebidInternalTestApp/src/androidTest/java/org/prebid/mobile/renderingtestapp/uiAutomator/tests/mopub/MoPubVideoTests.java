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

package org.prebid.mobile.renderingtestapp.uiAutomator.tests.mopub;

import org.junit.Test;
import org.prebid.mobile.renderingtestapp.R;
import org.prebid.mobile.renderingtestapp.uiAutomator.utils.BaseUiAutomatorTest;

public class MoPubVideoTests extends BaseUiAutomatorTest {
    @Test
    public void testMoPubVideoInterstitialNoBids() {
        homePage.setUseMockServer(false)
                .getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_video_interstitial_320_480_no_bids))
                .showPrebidInterstitial()
                .checkFullScreenHint()
                .waitTillVideoEnds()
                .closeInterstitial()
                .commonInterstitialEventsShouldPresent();
    }

    @Test
    public void testMoPubVideoInterstitialRandom() {
        homePage.getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_video_interstitial_320_480_random))
                .showPrebidInterstitial()
                .checkFullScreenHint()
                .waitTillVideoEnds()
                .closeInterstitial()
                .commonInterstitialEventsShouldPresent();
    }

    @Test
    public void testMoPubVideoInterstitialAdapter() {
        homePage.getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_video_interstitial_320_480_adapter))
                .showPrebidInterstitial()
                .waitTillVideoEnds()
                .commonInterstitialEventsShouldPresent();
    }

    @Test
    public void testMoPubRewardedVideoEndCardNoBids() {
        homePage.setUseMockServer(false)
                .getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_rewarded_video_end_card_320_480_no_bids))
                .showPrebidInterstitial()
                .checkFullScreenHint()
                .waitTillVideoEnds()
                .closeInterstitial()
                .commonRewardedVideoEventsShouldPresent();
    }

    @Test
    public void testMoPubRewardedVideoEndCardRandom() {
        homePage.getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_rewarded_video_end_card_320_480_random))
                .showPrebidInterstitial()
                .checkFullScreenHint()
                .waitTillVideoEnds()
                .closeInterstitial()
                .commonRewardedVideoEventsShouldPresent();
    }

    @Test
    public void testMoPubRewardedVideoEndCardAdapter() {
        homePage.getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_rewarded_video_end_card_320_480_adapter))
                .showPrebidInterstitial()
                .waitTillVideoEnds()
                .closeInterstitial()
                .commonRewardedVideoEventsShouldPresent();
    }

    @Test
    public void testMoPubRewardedVideo() throws InterruptedException {
        homePage.getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_rewarded_video_320_480_adapter))
                .showPrebidInterstitial()
                .learnMore()
                .browserShouldOpen()
                .closeBrowser()
                .waitTillVideoEnds()
                .commonRewardedVideoEventsShouldPresent();
    }

    @Test
    public void testPrebidMoPubRewardedVideoEndCardAdapter() {
        homePage.setUseMockServer(false)
                .getInterstitialPageFactory()
                .goToBiddingMoPubInterstitialExample(getStringResource(R.string.demo_bidding_mopub_rewarded_video_end_card_320_480_adapter))
                .showPrebidInterstitial()
                .waitTillVideoEnds()
                .closeInterstitial()
                .commonRewardedVideoEventsShouldPresent();
    }
}
