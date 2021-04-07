package com.openx.internal_test_app.uiAutomator.pages;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.openx.internal_test_app.BuildConfig;
import com.openx.internal_test_app.uiAutomator.pages.factory.BannerPageFactory;
import com.openx.internal_test_app.uiAutomator.pages.factory.InterstitialPageFactory;
import com.openx.internal_test_app.uiAutomator.pages.factory.MraidPageFactory;
import com.openx.internal_test_app.uiAutomator.pages.factory.NativePageFactory;

public class HomePage extends BasePage {

    private static class Locators {

        static BySelector dismissDialog = By.res(TAG, "dismiss_button");
        static BySelector allowButton = By.res("com.android.packageinstaller", "permission_allow_button");
        static BySelector mockServerSwitch = By.res(TAG, "switchUseMock");
    }

    public HomePage(UiDevice device) {
        super(device);
        setUseMockServer(BuildConfig.FLAVOR == "mock");
    }

    public void dismissWelcomeDialog() {

        try {
            device.wait(Until.findObject(Locators.dismissDialog), 2000)
                  .click();
        }
        catch (NullPointerException e) {
            Log.i(TAG, "dismissWelcomeDialog: not visible");
        }
    }

    public HomePage allowPermissionsIfNeeded() {
        UiObject2 dialogButton = device.wait(Until.findObject(Locators.allowButton), 3000);
        if (dialogButton != null) {
            while (device.findObject(Locators.allowButton) != null) {
                //it can take time for a permission Dialog to close, so we better check if the ALLOW button still exists every time we need to interact with it
                try {
                    device.findObject(Locators.allowButton).click();
                }
                catch (NullPointerException e) {

                }
            }
        }
        return this;
    }

    public BannerPageFactory getBannerPageFactory() {
        return new BannerPageFactory(device);
    }

    public MraidPageFactory getMraidPageFactory() {
        return new MraidPageFactory(device);
    }

    public InterstitialPageFactory getInterstitialPageFactory() {
        return new InterstitialPageFactory(device);
    }

    public NativePageFactory getNativePageFactory() {
        return new NativePageFactory(device);
    }

    public HomePage setUseMockServer(boolean state) {
        UiObject2 switchUiObject = device.wait(Until.findObject(Locators.mockServerSwitch), 2000);
        if (switchUiObject != null && switchUiObject.isChecked() != state) {
            switchUiObject.click();
        }
        return this;
    }
}
