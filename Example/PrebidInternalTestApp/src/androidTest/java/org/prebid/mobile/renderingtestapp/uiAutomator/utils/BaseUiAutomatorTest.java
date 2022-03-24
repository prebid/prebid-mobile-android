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

package org.prebid.mobile.renderingtestapp.uiAutomator.utils;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.RemoteException;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.prebid.mobile.renderingtestapp.uiAutomator.pages.HomePage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(AndroidJUnit4ClassRunner.class)
public class BaseUiAutomatorTest {

    public static final String INTERNAL_APP_PACKAGE = "org.prebid.mobile.renderingtestapp";
    public static final int TIMEOUT = 5000;
    protected UiDevice device;
    protected HomePage homePage;
    protected Resources resources;

    @Rule
    public RetryRule retryRule = new RetryRule(5);

    @Before
    public void setup() throws RemoteException, InterruptedException {

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        resources = instrumentation.getContext().getResources();
        device = UiDevice.getInstance(instrumentation);
        device.wakeUp();
        device.pressHome();

        final String launcherPackage = device.getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)),
                    TIMEOUT);

        Context context = instrumentation.getContext();

        final Intent intent = context.getPackageManager()
                                     .getLaunchIntentForPackage(INTERNAL_APP_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        device.wait(Until.hasObject(By.pkg(INTERNAL_APP_PACKAGE).depth(0)), TIMEOUT);

        homePage = new HomePage(device);

        homePage.allowPermissionsIfNeeded().dismissWelcomeDialog();
        homePage.setOrientationDefault();

        resources = instrumentation.getTargetContext().getResources();
    }

    protected String getStringResource(int resId) {
        return resources.getString(resId);
    }
}
