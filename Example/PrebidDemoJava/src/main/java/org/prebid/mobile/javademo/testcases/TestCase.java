package org.prebid.mobile.javademo.testcases;

import androidx.annotation.StringRes;

public class TestCase {

    private final int titleStringRes;
    private final AdFormat adFormat;
    private final IntegrationKind integrationKind;
    private final Class<?> activity;

    public TestCase(
        @StringRes int titleStringRes,
        AdFormat adFormat,
        IntegrationKind integrationKind,
        Class<?> activity
    ) {
        this.titleStringRes = titleStringRes;
        this.adFormat = adFormat;
        this.integrationKind = integrationKind;
        this.activity = activity;
    }

    public int getTitleStringRes() {
        return titleStringRes;
    }

    public AdFormat getAdFormat() {
        return adFormat;
    }

    public IntegrationKind getIntegrationKind() {
        return integrationKind;
    }

    public Class<?> getActivity() {
        return activity;
    }
}
