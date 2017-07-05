package org.prebid.mobile.unittestutils;

import android.app.Activity;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import org.prebid.mobile.unittestutils.mockobjects.MockMainActivity;

import org.junit.After;
import org.junit.Before;
import org.robolectric.Robolectric;
import org.robolectric.shadows.httpclient.FakeHttp;
import org.robolectric.util.Scheduler;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static org.robolectric.Shadows.shadowOf;

public class BaseSetup {
    public MockWebServer server;
    public boolean successfulMockServerStarted = false;
    public Scheduler uiScheduler, bgScheduler;
    public Activity activity;

    /**
     * Convenience methods and strings
     */
    public static final String MOCK_SERVER_NOT_STARTED = "Mock server was not started successfully";
    public static final String JSON_EXCEPTION = "Error parsing json response";
    public static final String CUSTOM_KEY = "key";
    public static final String CUSTOM_VALUE = "value";

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(MockMainActivity.class).create().get();
//        activity = Robolectric.setupActivity(MockMainActivity.class);
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

    public void scheduleTimerToCheckForTasks() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (uiScheduler.areAnyRunnable() || bgScheduler.areAnyRunnable()) {
                    Lock.unpause();
                    this.cancel();
                }
            }
        }, 0, 100);
    }

    public void waitForTasks() {
        scheduleTimerToCheckForTasks();
        Lock.pause();
    }

}
