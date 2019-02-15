/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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
        shadowOf(activity).grantPermissions("android.permission.CHANGE_NETWORK_STATE");
        shadowOf(activity).grantPermissions("android.permission.MODIFY_PHONE_STATE");
        shadowOf(activity).grantPermissions("android.permission.ACCESS_NETWORK_STATE");
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
