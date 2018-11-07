package org.prebid.mobile.testutils;

import android.app.Activity;

import org.junit.After;
import org.junit.Before;
import org.robolectric.Robolectric;
import org.robolectric.shadows.httpclient.FakeHttp;
import org.robolectric.util.Scheduler;

import java.io.IOException;

import okhttp3.mockwebserver.MockWebServer;

import static org.robolectric.Shadows.shadowOf;

public class BaseSetup {
    public static final int testSDK = 21;

    protected MockWebServer server;
    protected boolean successfulMockServerStarted = false;
    protected Scheduler uiScheduler, bgScheduler;
    protected Activity activity;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(MockMainActivity.class).create().get();
        shadowOf(activity).grantPermissions("android.permission.INTERNET");
        server = new MockWebServer();
        try {
            server.start();
            successfulMockServerStarted = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        FakeHttp.getFakeHttpLayer().interceptHttpRequests(true);
        FakeHttp.getFakeHttpLayer().interceptResponseContent(true);
        bgScheduler = Robolectric.getBackgroundThreadScheduler();
        uiScheduler = Robolectric.getForegroundThreadScheduler();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        bgScheduler.pause();
        uiScheduler.pause();
    }

    @After
    public void tearDown() {
        try {
            server.shutdown();
            activity.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
