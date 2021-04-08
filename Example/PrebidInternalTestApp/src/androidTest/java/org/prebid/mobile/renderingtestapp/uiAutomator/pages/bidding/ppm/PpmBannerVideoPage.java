package org.prebid.mobile.renderingtestapp.uiAutomator.pages.bidding.ppm;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertTrue;

public class PpmBannerVideoPage extends AdBasePage<PpmBannerVideoPage> {

    private static final int VIDEO_LOAD_TIMEOUT = 5000;
    private static final int DEFAULT_WAIT_TIMEOUT_MS = 1000;

    public PpmBannerVideoPage(UiDevice device) {
        super(device);
    }

    public PpmBannerVideoPage isLoaded() {
        assertTrue("Not loaded", device.wait(Until.hasObject(SdkEvent.onAdDisplayed), VIDEO_LOAD_TIMEOUT));
        return this;
    }

    public PpmBannerVideoPage waitTillVideoEnd() {
        device.wait(Until.findObject(Locators.btnWatchAgain), 16000);
        return this;
    }

    public PpmBannerVideoPage clickFullScreen() {
        clickOnView(Locators.videoAdView, DEFAULT_WAIT_TIMEOUT_MS);
        return this;
    }

    public PpmBannerVideoPage clickLearnMore() {
        clickOnView(Locators.learnMore, DEFAULT_WAIT_TIMEOUT_MS);
        return this;
    }

    public PpmBannerVideoPage closeFullScreen() {
        clickOnView(Locators.closeButton, DEFAULT_WAIT_TIMEOUT_MS);
        return this;
    }

    public PpmBannerVideoPage clickWatchAgain() {
        clickOnView(Locators.btnWatchAgain, DEFAULT_WAIT_TIMEOUT_MS);
        return this;
    }

    public PpmBannerVideoPage pressMute() {
        clickOnView(Locators.btnMute, DEFAULT_WAIT_TIMEOUT_MS);
        return this;
    }

    public PpmBannerVideoPage pressPlay() {
        clickOnView(Locators.btnPlay, DEFAULT_WAIT_TIMEOUT_MS);
        return this;
    }

    public PpmBannerVideoPage pressPause() {
        clickOnView(Locators.btnPause, DEFAULT_WAIT_TIMEOUT_MS);
        return this;
    }

    public PpmBannerVideoPage pressStop() {
        clickOnView(Locators.btnStop, DEFAULT_WAIT_TIMEOUT_MS);
        return this;
    }

    public PpmBannerVideoPage clickEndCard() {
        clickOnView(Locators.endCard, DEFAULT_WAIT_TIMEOUT_MS);
        return this;
    }

    private static class Locators {
        static BySelector videoAdView = By.res(TAG, "viewContainer");
        static BySelector closeButton = By.res(TAG, "iv_close_interstitial");
        static BySelector learnMore = By.res(TAG, "LearnMore");
        static BySelector btnWatchAgain = By.res(TAG, "btn_watch_again");
        static BySelector btnPlay = By.res(TAG, "btnPlay");
        static BySelector btnStop = By.res(TAG, "btnStop");
        static BySelector btnMute = By.res(TAG, "btnMute");
        static BySelector btnPause = By.res(TAG, "btnPause");
        static BySelector endCard = getWebViewSelectorVersionDepends("d24b6903a1a54afcaf8777d47d569ae1");
    }
}
