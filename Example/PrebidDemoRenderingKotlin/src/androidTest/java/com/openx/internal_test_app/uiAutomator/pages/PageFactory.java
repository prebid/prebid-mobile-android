package com.openx.internal_test_app.uiAutomator.pages;

import android.widget.EditText;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

public abstract class PageFactory {
    protected static final int TIMEOUT = 5000;
    protected UiDevice device;

    private static class Locators {
        static BySelector searchView = By.clazz(EditText.class);
        static BySelector listView = By.res("listDemos");
        static BySelector listViewItem = By.clazz("android.widget.RelativeLayout");
    }

    public PageFactory(UiDevice device) {
        this.device = device;
    }

    protected void findListItem(String target) {
        synchronized (device) {
            try {
                device.wait(2000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        UiObject2 uiObject = device.wait(Until.findObject(Locators.searchView), 5000);
        if (uiObject != null) {
            uiObject.click();
            uiObject.clear();
            uiObject.setText(target);

            device.wait(Until.findObject(Locators.listView), TIMEOUT);
            device.wait(Until.findObject(By.copy(Locators.listViewItem).hasChild(By.text(target))), TIMEOUT).click();
        }
    }
}
