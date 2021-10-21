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

package org.prebid.mobile.renderingtestapp.uiAutomator.pages;

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
