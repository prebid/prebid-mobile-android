package com.openx.internal_test_app.uiAutomator.tests.ppm;

import android.os.RemoteException;

import androidx.test.uiautomator.By;

import com.openx.internal_test_app.R;
import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;
import com.openx.internal_test_app.uiAutomator.utils.BaseUiAutomatorTest;

import org.json.JSONException;
import org.junit.Test;

public class PpmMraidTests extends BaseUiAutomatorTest {
    @Test
    public void testMraidExpand() {
        homePage.getMraidPageFactory()
                .goToMraidExpand(getStringResource(R.string.demo_bidding_ppm_mraid_expand))
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
                .goToMraidResize(getStringResource(R.string.demo_bidding_ppm_mraid_resize))
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
                .goToMraidExpand(getStringResource(R.string.demo_bidding_ppm_mraid_expand))
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
                .goToMraidExpand(getStringResource(R.string.demo_bidding_ppm_mraid_expand))
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
                .goToMraidExpand2(getStringResource(R.string.demo_bidding_ppm_mraid_expand_2))
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
                .goToMraidExpand2(getStringResource(R.string.demo_bidding_ppm_mraid_expand_2))
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
                .goToMraidExpand2(getStringResource(R.string.demo_bidding_ppm_mraid_expand_2))
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
                .goToMraidResize(getStringResource(R.string.demo_bidding_ppm_mraid_resize))
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
                .goToMraidResize(getStringResource(R.string.demo_bidding_ppm_mraid_resize))
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
                .goToMraidResize(getStringResource(R.string.demo_bidding_ppm_mraid_resize))
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
                .goToMraidResize(getStringResource(R.string.demo_bidding_ppm_mraid_resize))
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
                .goToMraidResize(getStringResource(R.string.demo_bidding_ppm_mraid_resize))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldResize()
                .clickToCall()
                .currentActivityShouldChange()
                .switchBackToOpenX()
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
                .goToMraidResize(getStringResource(R.string.demo_bidding_ppm_mraid_resize))
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
                .goToMraidResize(getStringResource(R.string.demo_bidding_ppm_mraid_resize))
                .bannerShouldLoad()
                .clickBanner()
                .adShouldResize()
                .sendSMS()
                .currentActivityShouldChange()
                .switchBackToOpenX()
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
                .goToMraidResizeErrors(getStringResource(R.string.demo_bidding_ppm_mraid_resize_with_errors))
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
                .goToMraidResizeErrors(getStringResource(R.string.demo_bidding_ppm_mraid_resize_with_errors))
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
                .goToMraid3TestMethods(getStringResource(R.string.demo_bidding_ppm_mraid_test_methods))
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
                .goToMraid3TestMethods(getStringResource(R.string.demo_bidding_ppm_mraid_test_methods))
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
                .goToMraid3TestProperties(getStringResource(R.string.demo_bidding_ppm_mraid_test_properties))
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
                .goToMraid3ViewabilityCompliancePage(getStringResource(R.string.demo_bidding_ppm_mraid_viewability_compliance))
                .bannerShouldLoad()
                .verifyNoErrorsArePresent()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed);
    }

    @Test
    public void testMraid3ResizeNegative() {
        homePage.getMraidPageFactory()
                .goToMraid3ResizeNegativePage(getStringResource(R.string.demo_bidding_ppm_mraid_resize_negative))
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
                .goToMraid3LoadAndEvents(getStringResource(R.string.demo_bidding_ppm_mraid_load_and_events))
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
        final String exampleName = getStringResource(R.string.demo_bidding_ppm_interstitial_video_320_480_mraid_end_card);
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
    public void testApolloMraidResize() {
        homePage.setUseMockServer(false)
                .getMraidPageFactory()
                .goToMraidResize(getStringResource(R.string.demo_bidding_ppm_mraid_resize))
                .bannerShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }
}
