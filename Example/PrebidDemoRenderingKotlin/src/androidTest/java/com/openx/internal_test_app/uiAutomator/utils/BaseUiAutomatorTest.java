package com.openx.internal_test_app.uiAutomator.utils;

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

import com.openx.internal_test_app.mock.MockServerManager;
import com.openx.internal_test_app.uiAutomator.pages.HomePage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(AndroidJUnit4ClassRunner.class)
public class BaseUiAutomatorTest {

    public static final String INTERNAL_APP_PACKAGE = "com.openx.internal_test_app";
    public static final int TIMEOUT = 5000;
    protected MockServerManager mMockServerManager = new MockServerManager();
    protected UiDevice device;
    protected HomePage homePage;
    protected Resources resources;

    @Rule
    public RetryRule retryRule = new RetryRule(5);

    @Before
    public void setup() throws RemoteException, InterruptedException {
        mMockServerManager.clearLogs();

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
