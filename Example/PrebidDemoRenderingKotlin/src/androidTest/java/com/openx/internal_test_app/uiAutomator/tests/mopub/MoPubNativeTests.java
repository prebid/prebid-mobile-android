package com.openx.internal_test_app.uiAutomator.tests.mopub;

import androidx.test.uiautomator.By;

import com.openx.internal_test_app.R;
import com.openx.internal_test_app.uiAutomator.pages.AdBasePage;
import com.openx.internal_test_app.uiAutomator.utils.BaseUiAutomatorTest;

import org.junit.Test;

public class MoPubNativeTests extends BaseUiAutomatorTest {
    @Test
    public void testMoPubNativeStyles() throws InterruptedException {
        homePage.getNativePageFactory()
                .goToMoPubNativeStyles(getStringResource(R.string.demo_bidding_mopub_native_styles))
                .nativeShouldLoad()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidLoad)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }

    @Test
    public void testMoPubNativeStylesNoAssets() {
        homePage.getNativePageFactory()
                .goToMoPubNativeStyles(getStringResource(R.string.demo_bidding_mopub_native_styles_no_assets))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }

    @Test
    public void testApolloMoPubNativeStyles() throws InterruptedException {
        homePage.setUseMockServer(false)
                .getNativePageFactory()
                .goToMoPubNativeStyles(getStringResource(R.string.demo_bidding_mopub_native_styles))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.adDidFailToLoad);
    }

    @Test
    public void testMoPubNative() {
        homePage.getNativePageFactory()
                .goToMoPubNativeStyles(getStringResource(R.string.demo_bidding_mopub_native_adapter))
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.fetchDemandSuccess)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.nativeAdLoaded)
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.impressionEvent)
                .clickOnView(By.res(INTERNAL_APP_PACKAGE, "btnNativeAction"), TIMEOUT)
                .browserShouldOpen()
                .closeBrowser()
                .sdkEventShouldBePresent(AdBasePage.SdkEvent.onAdClicked)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.nativeAdFailed)
                .sdkEventShouldNotBePresent(AdBasePage.SdkEvent.fetchDemandFailure);
    }
}
