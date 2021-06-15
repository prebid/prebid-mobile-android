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

package org.prebid.mobile.renderingtestapp.uiAutomator.tests.ppm;

import android.os.RemoteException;

import androidx.test.uiautomator.By;

import org.json.JSONException;
import org.junit.Test;
import org.prebid.mobile.renderingtestapp.R;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.utils.BaseUiAutomatorTest;

public class PpmMraidTests extends BaseUiAutomatorTest {
    @Test
    public void testMraidExpand() {
        homePage.getMraidPageFactory()
                .goToMraidExpand(getStringResource(R.string.demo_bidding_in_app_mraid_expand))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldBeExpanded()
                .closeExpandedAd()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testMraidResize() {
        homePage.getMraidPageFactory()
                .goToMraidResize(getStringResource(R.string.demo_bidding_in_app_mraid_resize))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldResize()
                .closeInterstitial()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testExpand1Locks() {
        homePage.getMraidPageFactory()
                .goToMraidExpand(getStringResource(R.string.demo_bidding_in_app_mraid_expand))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldBeExpanded()
                .lockToLandscape()
                .lockToPortrait()
                .releaseLock()
                .lockButtonShouldNotBeDisplayed()
                .closeExpandedAd()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testExpand1Landscape() throws RemoteException, InterruptedException {
        homePage.getMraidPageFactory()
                .goToMraidExpand(getStringResource(R.string.demo_bidding_in_app_mraid_expand))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldBeExpanded()
                .setOrientationLeft()
                .rotateToPortraitShouldBeDisplayed()
                .setOrientationDefault()
                .closeExpandedAd()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testExpand2OpenBrowser() {
        homePage.getMraidPageFactory()
                .goToMraidExpand2(getStringResource(R.string.demo_bidding_in_app_mraid_expand_2))
                .clickBanner()
                .adShouldBeExpanded()
                .openInBrowser()
                .browserShouldOpen()
                .closeBrowser()
                .adShouldBeExpanded()
                .closeExpandedAd()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testExpand2PlayVideo() {
        homePage.getMraidPageFactory()
                .goToMraidExpand2(getStringResource(R.string.demo_bidding_in_app_mraid_expand_2))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldBeExpanded()
                .playVideo()
                .videoShouldBePlayed()
                .closeVideoPlayer()
                .adShouldBeExpanded()
                .goBackOnce()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testExpand2ExpandAgain() {
        homePage.getMraidPageFactory()
                .goToMraidExpand2(getStringResource(R.string.demo_bidding_in_app_mraid_expand_2))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldBeExpanded()
                .expandAgain()
                .adShouldBeExpanded()
                .closeExpandedAd()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testResizeOpenUrl() {
        homePage.getMraidPageFactory()
                .goToMraidResize(getStringResource(R.string.demo_bidding_in_app_mraid_resize))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldResize()
                .openUrl()
                .browserShouldOpen()
                .closeBrowser()
                .adShouldResize()
                .closeInterstitial()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testResizeClickToMap() {
        homePage.getMraidPageFactory()
                .goToMraidResize(getStringResource(R.string.demo_bidding_in_app_mraid_resize))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldResize()
                .clickToMap()
                .browserShouldOpen()
                .closeBrowser()
                .adShouldResize()
                .closeInterstitial()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testResizeClickToApp() {
        homePage.getMraidPageFactory()
                .goToMraidResize(getStringResource(R.string.demo_bidding_in_app_mraid_resize))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldResize()
                .clickToApp()
                .browserShouldOpen()
                .closeBrowser()
                .adShouldResize()
                .closeInterstitial()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testResizePlayVideo() {
        homePage.getMraidPageFactory()
                .goToMraidResize(getStringResource(R.string.demo_bidding_in_app_mraid_resize))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldResize()
                .playVideo()
                .videoShouldBePlayed()
                .closeVideoPlayer()
                .adShouldResize()
                .closeInterstitial()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testResizeClickToCall() throws InterruptedException {
        homePage.getMraidPageFactory()
                .goToMraidResize(getStringResource(R.string.demo_bidding_in_app_mraid_resize))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldResize()
                .clickToCall()
                .currentActivityShouldChange()
                .switchBackToApp()
                .adShouldResize()
                .closeInterstitial()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked);
    }

    @Test
    public void testResizeCreateCalendarEvent() throws InterruptedException {
        homePage.getMraidPageFactory()
                .goToMraidResize(getStringResource(R.string.demo_bidding_in_app_mraid_resize))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldResize()
                .createCalendarEvent()
                .currentActivityShouldChange()
                .pressBackToApp()
                .adShouldResize()
                .closeInterstitial()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked);
    }

    @Test
    public void testResizeSMS() throws InterruptedException {
        homePage.getMraidPageFactory()
                .goToMraidResize(getStringResource(R.string.demo_bidding_in_app_mraid_resize))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldResize()
                .sendSMS()
                .currentActivityShouldChange()
                .switchBackToApp()
                .adShouldResize()
                .closeInterstitial()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked);
    }

    @Test
    public void testResizeWithErrorsOffscreenTrue() {
        homePage.getMraidPageFactory()
                .goToMraidResizeErrors(getStringResource(R.string.demo_bidding_in_app_mraid_resize_with_errors))
                .adShouldBeDisplayed()
                .setOffscreenTrue()
                .resizeLeft()
                .closeButtonShouldNotBeDisplayed()
                .resizeRight()
                .closeButtonShouldNotBeDisplayed()
                .resizeUp()
                .closeButtonShouldNotBeDisplayed()
                .resizeDown()
                .closeButtonShouldNotBeDisplayed()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testResizeWithErrorsOffscreenFalse() {
        homePage.getMraidPageFactory()
                .goToMraidResizeErrors(getStringResource(R.string.demo_bidding_in_app_mraid_resize_with_errors))
                .adShouldBeDisplayed()
                .setOffscreenFalse()
                .resizeLeft()
                .closeResizedAd()
                .closeButtonShouldNotBeDisplayed()
                .resizeRight()
                .closeResizedAd()
                .closeButtonShouldNotBeDisplayed()
                .resizeUp()
                .closeResizedAd()
                .closeButtonShouldNotBeDisplayed()
                .resizeDown()
                .closeResizedAd()
                .closeButtonShouldNotBeDisplayed()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testMraid3MethodsResizeExpandChain() {
        homePage.getMraidPageFactory()
                .goToMraid3TestMethods(getStringResource(R.string.demo_bidding_in_app_mraid_test_methods))
                .bannerShouldLoad()
                .clickResize()
                .adShouldBeResized()
                .clickExpand()
                .adShouldBeExpanded()
                .closeExpandedAd()
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testMraid3MethodsExpandResizeChain() {
        homePage.getMraidPageFactory()
                .goToMraid3TestMethods(getStringResource(R.string.demo_bidding_in_app_mraid_test_methods))
                .bannerShouldLoad()
                .clickExpand()
                .adShouldBeExpanded()
                .clickResizeInExpand()
                .adShouldBeExpanded()
                .closeExpandedAd()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testMraid3Properties() throws JSONException {
        homePage.getMraidPageFactory()
                .goToMraid3TestProperties(getStringResource(R.string.demo_bidding_in_app_mraid_test_properties))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldBeExpanded()
                .checkMraidEnv()
                .checkGetLocationFinishedWithoutError()
                .checkGetCurrentAppOrientation()
                .pressPlayButton()
                .turnVolumeDown()
                .pressVolumeUpAndCheckVolume(3)
                .closeExpandedAd()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testViewabilityCompliance() {
        homePage.getMraidPageFactory()
                .goToMraid3ViewabilityCompliancePage(getStringResource(R.string.demo_bidding_in_app_mraid_viewability_compliance))
                .bannerShouldLoad()
                .verifyNoErrorsArePresent()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed);
    }

    @Test
    public void testMraid3ResizeNegative() {
        homePage.getMraidPageFactory()
                .goToMraid3ResizeNegativePage(getStringResource(R.string.demo_bidding_in_app_mraid_resize_negative))
                .sdkCloseButtonShouldBePresent()
                .clickSdkClose()
                .verifyTestCasesPassed()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
        ;
    }

    @Test
    public void testMraid3LoadAndEventsFlow() {
        homePage.getMraidPageFactory()
                .goToMraid3LoadAndEvents(getStringResource(R.string.demo_bidding_in_app_mraid_load_and_events))
                .bannerShouldLoad()
                .clickExpandStateChangeCheck()
                .tapSdkCloseTextShouldBePresent()
                .logShouldBePresent()
                .sdkCloseButtonShouldBePresent()
                .clickSdkClose()
                .clickExpandSizeChangeCheck()
                .sdkCloseButtonShouldBePresent()
                .logShouldBePresent()
                .clickJsClose()
                .clickCheckLogs()
                .logShouldBePresent()
                .sdkCloseButtonShouldBePresent()
                .clickUnload()
                .adShouldNotBeDisplayed()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed);
    }

    @Test
    public void testVastWithMraidEndCard() {
        final String exampleName = getStringResource(R.string.demo_bidding_in_app_interstitial_video_320_480_mraid_end_card);
        homePage.getInterstitialPageFactory()
                .goToPpmInterstitialExample(exampleName)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .showPrebidInterstitial()
                .viewShouldBePresent(By.text("Off screen timer: "))
                .viewShouldNotBePresent(By.text("00:00:00"))
                .closeInterstitial()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPrebidMraidResize() {
        homePage.setUseMockServer(false)
                .getMraidPageFactory()
                .goToMraidResize(getStringResource(R.string.demo_bidding_in_app_mraid_resize))
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }
}
