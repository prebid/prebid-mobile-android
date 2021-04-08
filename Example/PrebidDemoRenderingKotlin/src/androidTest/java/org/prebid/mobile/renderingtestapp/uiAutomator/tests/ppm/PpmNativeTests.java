package org.prebid.mobile.renderingtestapp.uiAutomator.tests.ppm;

import androidx.test.uiautomator.By;

import org.junit.Test;
import org.prebid.mobile.renderingtestapp.R;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;
import org.prebid.mobile.renderingtestapp.uiAutomator.utils.BaseUiAutomatorTest;

public class PpmNativeTests extends BaseUiAutomatorTest {
    @Test
    public void testPpmNativeStylesMap() {
        homePage.getNativePageFactory()
                .goToPpmNativeStyles(getStringResource(R.string.demo_bidding_in_app_native_styles_map))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPpmNativeStylesKeys() {
        homePage.getNativePageFactory()
                .goToPpmNativeStyles(getStringResource(R.string.demo_bidding_in_app_native_styles_keys))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPpmNativeStylesNoAssets() {
        homePage.getNativePageFactory()
                .goToPpmNativeStyles(getStringResource(R.string.demo_bidding_in_app_native_styles_no_assets))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPrebidPpmNativeStylesMap() {
        homePage.setUseMockServer(false)
                .getNativePageFactory()
                .goToPpmNativeStyles(getStringResource(R.string.demo_bidding_in_app_native_styles_map))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testPpmNative() {
        homePage.getNativePageFactory()
                .goToPpmNativeStyles(getStringResource(R.string.demo_bidding_in_app_native))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.fetchDemandSuccess)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.getNativeAdSuccess)
                .clickOnView(By.res(INTERNAL_APP_PACKAGE, "btnNativeAction"), TIMEOUT)
                .browserShouldOpen()
                .closeBrowser()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.impressionEvent)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.mrc50Event)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.mrc100Event)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.fetchDemandFailure)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.getNativeAdFailure)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.video50Event);
    }

    @Test
    public void testPpmNativeVideo() throws InterruptedException {
        homePage.getNativePageFactory()
                .goToPpmNative(getStringResource(R.string.demo_bidding_in_app_native_video))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.fetchDemandSuccess)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.getNativeAdSuccess)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onVideoLoadingFinished)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onPlaybackStarted)
                .videoCreativeWithAdIndicatorShouldBePresent()
                .clickUnMute()
                .clickMute()
                .clickPause()
                .clickResume()
                .sleepFor(15)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onPlaybackUnMuted)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onPlaybackMuted)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onPlaybackPaused)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onPlaybackResumed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onPlaybackFinished)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.impressionEvent)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.mrc50Event)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.mrc100Event)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.fetchDemandFailure)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.getNativeAdFailure)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onFailure)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.video50Event);
    }
}
