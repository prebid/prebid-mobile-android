package org.prebid.mobile.renderingtestapp.uiAutomator.pages;

import android.os.Build;
import android.os.RemoteException;
import android.view.KeyEvent;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.prebid.mobile.renderingtestapp.uiAutomator.utils.WebViewLocator;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.web.assertion.WebViewAssertions.webContent;
import static androidx.test.espresso.web.matcher.DomMatchers.hasElementWithId;
import static androidx.test.espresso.web.matcher.DomMatchers.hasElementWithXpath;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static androidx.test.espresso.web.webdriver.DriverAtoms.findElement;
import static androidx.test.espresso.web.webdriver.DriverAtoms.webClick;
import static org.junit.Assert.assertTrue;

public class BasePage<T> {

    protected static final String TAG = "org.prebid.mobile.renderingtestapp";
    protected UiDevice device;
    private int ACTIVITY_TIMEOUT = 10000;

    protected BasePage(UiDevice device) {
        this.device = device;
    }

    @SuppressWarnings("unchecked")
    public T setOrientationLeft() throws RemoteException, InterruptedException {
        device.setOrientationLeft();
        waitForActionPerformed();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setOrientationRight() throws RemoteException, InterruptedException {
        device.setOrientationRight();
        waitForActionPerformed();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setOrientationDefault() throws RemoteException, InterruptedException {
        device.setOrientationNatural();
        waitForActionPerformed();
        return (T) this;
    }

    private void waitForActionPerformed() throws InterruptedException {
        synchronized (device) {
            device.wait(500);
        }
        device.waitForIdle();
    }

    @SuppressWarnings("unchecked")
    protected T clickOnElementByUiSelector(UiSelector locator, int timeout)
    throws UiObjectNotFoundException {
        UiObject object = device.findObject(locator);
        assertTrue(String.format("%s is not displayed", locator.toString()),
                   object.waitForExists(timeout));
        object.click();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    protected T webViewElementShouldExist(WebViewLocator locator, int timeout) {
        switch (locator.TYPE) {
            case ID:
                onWebView()
                    .withTimeout(timeout, TimeUnit.MILLISECONDS)
                    .check(webContent(hasElementWithId(locator.VALUE)));
                break;
            case XPATH:
                onWebView()
                    .withTimeout(timeout, TimeUnit.MILLISECONDS)
                    .check(webContent(hasElementWithXpath(locator.VALUE)));
                break;
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    protected T webViewElementShouldNotExist(WebViewLocator locator) {
        try {
            webViewElementShouldExist(locator, 500);
        }
        catch (AssertionError e) {
            return (T) this;
        }
        throw new AssertionError(String.format("WebView element should not exist: <%s>:<%s>",
                                               locator.TYPE, locator.VALUE));
    }

    @SuppressWarnings("unchecked")
    protected T clickOnWebViewElement(WebViewLocator locator, int timeout) {
        onWebView()
            .withTimeout(timeout, TimeUnit.MILLISECONDS)
            .withElement(findElement(locator.TYPE, locator.VALUE))
            .perform(webClick());
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T currentActivityShouldChange() throws InterruptedException {
        assertTrue("Current activity has not changed",
                   device.wait(Until.gone(By.pkg(TAG)), ACTIVITY_TIMEOUT));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T pressBackToApp() {
        while (device.findObject(By.pkg(TAG)) == null) {
            device.pressBack();
        }
        return (T) this;
    }

    public T goBackOnce() {
        device.pressBack();
        return (T) this;
    }

    public T switchBackToApp() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return pressBackToApp();
        }
        openRecentApps();
        device.wait(Until.findObject(getWebViewSelectorVersionDepends("Prebid Rendering Kotlin Demo")), 100).click();
        return (T) this;
    }

    private void openRecentApps() {
        device.pressKeyCode(KeyEvent.KEYCODE_APP_SWITCH);
    }

    protected static BySelector getWebViewSelectorVersionDepends(String text) {
        return getWebViewSelectorVersionDepends(text, false);
    }

    protected static BySelector getWebViewSelectorVersionDependsByContains(String text) {
        return getWebViewSelectorVersionDepends(text, true);
    }

    protected static BySelector getWebViewSelectorVersionDepends(String text,
                                                                 boolean matchUsingContains) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            if (matchUsingContains) {
                return By.descContains(text);
            }
            return By.desc(text);
        }

        if (matchUsingContains) {
            return By.textContains(text);
        }
        return By.text(text);
    }

    protected String getViewDescVersionDepends(UiObject2 uiObject2) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return uiObject2.getContentDescription();
        }
        return uiObject2.getText();
    }

    protected static boolean isAtLeastAPI25() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
    }
}
