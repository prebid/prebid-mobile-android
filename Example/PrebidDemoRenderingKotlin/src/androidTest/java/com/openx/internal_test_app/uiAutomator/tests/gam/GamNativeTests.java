package com.openx.internal_test_app.uiAutomator.tests.gam;

import androidx.test.uiautomator.BySelector;

import com.openx.internal_test_app.R;
import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;
import com.openx.internal_test_app.uiAutomator.pages.factory.GamNativePage;
import com.openx.internal_test_app.uiAutomator.utils.BaseUiAutomatorTest;

import org.junit.Test;

public class GamNativeTests extends BaseUiAutomatorTest {
    @Test
    public void testGamNativeStylesMrect() throws InterruptedException {
        homePage.getNativePageFactory()
                .goToGamNativeStyles(getStringResource(R.string.demo_bidding_gam_native_styles_mrect))
                .nativeShouldLoad()
                .clickCta()
                .sleepFor(2)
                .goBackOnce()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testGamNativeStylesFluid() throws InterruptedException {
        homePage.getNativePageFactory()
                .goToGamNativeStyles(getStringResource(R.string.demo_bidding_gam_native_styles_fluid))
                .nativeShouldLoad()
                .clickCta()
                .sleepFor(2)
                .goBackOnce()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdDisplayed)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClosed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testGamNativeStylesNoAssets() {
        homePage.getNativePageFactory()
                .goToGamNativeStyles(getStringResource(R.string.demo_bidding_gam_native_styles_no_assets))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testApolloGamNativeStylesMrect() throws InterruptedException {
        homePage.setUseMockServer(false)
                .getNativePageFactory()
                .goToGamNativeStyles(getStringResource(R.string.demo_bidding_gam_native_styles_mrect))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdFailed);
    }

    @Test
    public void testGamNativeCustomTemplatesApolloWin() throws InterruptedException {
        final GamNativePage gamNativePage =
            homePage.getNativePageFactory()
                    .goToGamNative(getStringResource(R.string.demo_bidding_gam_native_custom_templates));

        verifyGamNativeAdApolloWin(gamNativePage, AdBasePage.SdkEvent.customAdRequestSuccess);
    }

    @Test
    public void testGamNativeUnifiedAdApolloWin() throws InterruptedException {
        final GamNativePage gamNativePage =
            homePage.getNativePageFactory()
                    .goToGamNative(getStringResource(R.string.demo_bidding_gam_native_unified_ads));

        verifyGamNativeAdApolloWin(gamNativePage, AdBasePage.SdkEvent.unifiedAdRequestSuccess);
    }

    @Test
    public void testGamNativeCustomTemplatesGamWin() throws InterruptedException {
        homePage.getNativePageFactory()
                .goToGamNative(getStringResource(R.string.demo_bidding_gam_native_custom_templates_no_bids))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.fetchDemandFailure)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.customPrimaryAdWin)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.customAdRequestSuccess)
                .clickCta()
                .sleepFor(2)
                .goBackOnce()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.impressionEvent)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.mrc50Event)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.mrc100Event)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.nativeAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.fetchDemandSuccess)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.primaryAdRequestFailed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.unifiedPrimaryAdWin)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.unifiedAdRequestSuccess)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.video50Event);
    }

    @Test
    public void testGamNativeUnifiedAdGamWin() throws InterruptedException {
        homePage.getNativePageFactory()
                .goToGamNative(getStringResource(R.string.demo_bidding_gam_native_unified_ads_no_bids))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.fetchDemandFailure)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.unifiedPrimaryAdWin)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.unifiedAdRequestSuccess)
                .clickUnifiedCta()
                .sleepFor(2)
                .goBackOnce()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.impressionEvent)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.mrc50Event)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.mrc100Event)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.nativeAdLoaded)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.fetchDemandSuccess)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.primaryAdRequestFailed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.customPrimaryAdWin)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.customAdRequestSuccess)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.video50Event);
    }

    private void verifyGamNativeAdApolloWin(GamNativePage nativePage, BySelector winnerRequestSelector)
    throws InterruptedException {
        nativePage.sdkEventShouldBePresent(AdBasePage.SdkEvent.fetchDemandSuccess)
                  .sdkEventShouldBePresent(winnerRequestSelector)
                  .sdkEventShouldBePresent(AdBasePage.SdkEvent.nativeAdLoaded)
                  .clickCta()
                  .sleepFor(2)
                  .goBackOnce()
                  .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                  .sdkEventShouldBePresent(AdBasePage.SdkEvent.impressionEvent)
                  .sdkEventShouldBePresent(AdBasePage.SdkEvent.mrc50Event)
                  .sdkEventShouldBePresent(AdBasePage.SdkEvent.mrc100Event)
                  .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.fetchDemandFailure)
                  .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.primaryAdRequestFailed)
                  .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.customPrimaryAdWin)
                  .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.unifiedPrimaryAdWin)
                  .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.video50Event);
    }
}
