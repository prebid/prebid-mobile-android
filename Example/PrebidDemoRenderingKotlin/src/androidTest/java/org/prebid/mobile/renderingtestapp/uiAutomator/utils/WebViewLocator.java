package org.prebid.mobile.renderingtestapp.uiAutomator.utils;

import androidx.test.espresso.web.webdriver.Locator;

public class WebViewLocator {

    final public Locator TYPE;
    final public String VALUE;

    public WebViewLocator(Locator type, String value) {
        this.TYPE = type;
        this.VALUE = value;
    }
}
