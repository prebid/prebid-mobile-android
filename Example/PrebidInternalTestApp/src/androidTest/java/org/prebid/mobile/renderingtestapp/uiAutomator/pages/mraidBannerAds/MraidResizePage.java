package org.prebid.mobile.renderingtestapp.uiAutomator.pages.mraidBannerAds;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import org.prebid.mobile.renderingtestapp.uiAutomator.pages.AdBasePage;

import static org.junit.Assert.assertNotNull;

public class MraidResizePage extends AdBasePage<MraidResizePage> {

    private static class Locators {
        static BySelector bannerCreative = By.res("normal");
        static BySelector videoCreative = By.clazz("android.widget.VideoView");
        static BySelector closeButton = getWebViewSelectorVersionDepends("X");
        static BySelector openUrlButton = getWebViewSelectorVersionDepends("Open URL");
        static BySelector clickToMapButton = getWebViewSelectorVersionDepends("Click to Map");
        static BySelector clickToAppButton = getWebViewSelectorVersionDepends("Click to App");
        static BySelector playVideoButton = getWebViewSelectorVersionDepends("Play Video");
        static BySelector smsButton = getWebViewSelectorVersionDepends("SMS");
        static BySelector calendarButton = getWebViewSelectorVersionDepends("Create Calendar Event");
        static BySelector callButton = getWebViewSelectorVersionDepends("Click to Call");
        static BySelector storePictureButton = getWebViewSelectorVersionDepends("Store Picture");

    }

    public MraidResizePage(UiDevice device) {
        super(device);
    }

    public MraidResizePage clickBanner() {
        device.wait(Until.findObject(Locators.bannerCreative), TIMEOUT)
              .click();
        return this;
    }

    public MraidResizePage bannerShouldLoad() {
        assertNotNull("Banner is not displayed",
                      device.wait(Until.findObject(Locators.bannerCreative), TIMEOUT * 2));
        return this;
    }

    public MraidResizePage adShouldResize() {
        assertNotNull("Banner is not resized", device.wait(Until.findObject(Locators.closeButton), TIMEOUT));
        return this;
    }

    public MraidResizePage openUrl() {
        clickOnView(Locators.openUrlButton, TIMEOUT);
        return this;
    }

    public MraidResizePage clickToMap() {
        clickOnView(Locators.clickToMapButton, TIMEOUT);
        return this;
    }

    public MraidResizePage clickToApp() {
        clickOnView(Locators.clickToAppButton, TIMEOUT);
        return this;
    }

    public MraidResizePage playVideo() {
        clickOnView(Locators.playVideoButton, TIMEOUT);
        return this;
    }

    public MraidResizePage sendSMS() {
        clickOnView(Locators.smsButton, TIMEOUT);
        return this;
    }

    public MraidResizePage createCalendarEvent() {
        clickOnView(Locators.calendarButton, TIMEOUT);
        return this;
    }

    public MraidResizePage clickToCall() {
        clickOnView(Locators.callButton, TIMEOUT);
        return this;
    }

    public MraidResizePage videoShouldBePlayed() {
        assertNotNull("Video is not played",
                      device.wait(Until.findObject(Locators.videoCreative), 500));
        return this;
    }

    private boolean isVideoPlaying() {
        return device.wait(Until.findObject(Locators.videoCreative), 500) != null;
    }

    public MraidResizePage closeVideoPlayer() {
        while (isVideoPlaying()) {
            device.pressBack();
        }
        return this;
    }

    public MraidResizePage closeDialApp() {
        device.pressBack();
        device.pressBack();
        device.pressBack();
        return this;
    }
}
