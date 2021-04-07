package com.openx.internal_test_app.uiAutomator.pages.mraidBannerAds;

import android.view.KeyEvent;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import com.openx.apollo.sdk.ApolloSettings;
import com.openx.apollo.utils.helpers.AdIdManager;
import com.openx.apollo.utils.helpers.AppInfoManager;

import org.json.JSONException;
import org.json.JSONObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class Mraid3TestProperties extends MraidBasicPage {

    public Mraid3TestProperties(UiDevice device) {
        super(device);
    }

    public Mraid3TestProperties bannerShouldLoad() {
        assertNotNull("Banner is not displayed",
                      device.wait(Until.findObject(Locators.bannerCreative), TIMEOUT * 2));
        return this;
    }

    public Mraid3TestProperties clickBanner() {
        clickOnView(Locators.bannerCreative, TIMEOUT);
        return this;
    }

    public Mraid3TestProperties checkMraidEnv() throws JSONException {
        JSONObject envJson = new JSONObject();
        envJson.put("version", ApolloSettings.MRAID_VERSION);
        envJson.put("sdk", ApolloSettings.SDK_NAME);
        envJson.put("sdkVersion", ApolloSettings.SDK_VERSION);
        envJson.put("appId", AppInfoManager.getPackageName());
        envJson.put("ifa", AdIdManager.getAdId());
        envJson.put("limitAdTracking", AdIdManager.isLimitAdTrackingEnabled());
        envJson.put("coppa", ApolloSettings.isCoppaEnabled);

        String viewText = clearStringFromWhitespace(device.wait(Until.findObject(Locators.envContainer), TIMEOUT).getText());
        assertEquals(envJson.toString(), viewText);
        return this;
    }

    public Mraid3TestProperties checkGetLocationFinishedWithoutError() {
        String viewText = clearStringFromWhitespace(device.wait(Until.findObject(Locators.locationContainer), TIMEOUT).getText());
        assertFalse(viewText.isEmpty());
        return this;
    }

    public Mraid3TestProperties checkGetCurrentAppOrientation() throws JSONException {
        JSONObject appOrientationJson = new JSONObject();
        appOrientationJson.put("orientation", getDeviceOrientation());
        appOrientationJson.put("locked", false); // activity is not locked.

        String viewText = clearStringFromWhitespace(device.wait(Until.findObject(Locators.orientationContainer), TIMEOUT).getText());

        assertEquals(appOrientationJson.toString(), viewText);
        return this;
    }

    private String clearStringFromWhitespace(String text) {
        return text.replace("\n", "")
                   .replace(" ", "");
    }

    public Mraid3TestProperties adShouldBeExpanded() {
        assertNotNull("Ad is not expanded",
                      device.wait(Until.findObject(Mraid3TestProperties.Locators.expandedAdName), TIMEOUT));
        return this;
    }

    public Mraid3TestProperties pressPlayButton() {
        clickOnView(Mraid3TestProperties.Locators.playButton, TIMEOUT);
        return this;
    }

    public Mraid3TestProperties closeExpandedAd() {
        clickOnView(Mraid3TestProperties.Locators.closeButton, TIMEOUT);
        return this;
    }

    public Mraid3TestProperties turnVolumeDown() {
        while (getVolumeLevel() != 0) {
            volumeDown();
        }
        return this;
    }

    public Mraid3TestProperties pressVolumeUpAndCheckVolume(int times) {
        for (int i = 0; i < times; i++) {
            volumeUp();
        }

        double volumeChangeStep = 100.0 / 15.0;
        assertEquals("Expected volume level doesn't match actual.", volumeChangeStep * (double) times, getVolumeLevel(), 0);

        return this;
    }

    private void volumeUp() {
        device.pressKeyCode(KeyEvent.KEYCODE_VOLUME_UP);
    }

    private void volumeDown() {
        device.pressKeyCode(KeyEvent.KEYCODE_VOLUME_DOWN);
    }

    private Double getVolumeLevel() {
        String volumeString = device.wait(Until.findObject(Mraid3TestProperties.Locators.volumeIndicator), TIMEOUT)
                                    .getText()
                                    .replace("%", "");
        return Double.valueOf(volumeString);
    }

    private static class Locators {
        static BySelector bannerCreative = getWebViewSelectorVersionDepends("Expand!");
        static BySelector envContainer = By.res("mraidEnvNode");
        static BySelector locationContainer = By.res("locationNode");
        static BySelector orientationContainer = By.res("currentAppOrientationNode");
        static BySelector expandedAdName = By.res("posdiv");
        static BySelector closeButton = By.res("com.openx.internal_test_app:id/iv_close_interstitial");
        static BySelector volumeIndicator = By.res("indicator");
        static BySelector playButton = getWebViewSelectorVersionDepends("play");
    }
}
