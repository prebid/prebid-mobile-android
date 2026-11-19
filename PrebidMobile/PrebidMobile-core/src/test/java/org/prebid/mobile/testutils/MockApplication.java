/*
 *    Copyright 2018s Prebid.org, Inc.
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

import android.app.Application;
import android.os.AsyncTask;

/**
 * Application for all Robolectric tests (set in robolectric.properties).
 */
public class MockApplication extends Application {

    static {
        // On Android 5.0, which these tests run on, AsyncTask binds its handler to the thread that
        // loads the class. Android loads it on the main thread when the app starts, but Robolectric
        // doesn't. Without this, an SDK background thread can load it first, and every later
        // AsyncTask in the test run fails.
        AsyncTask.THREAD_POOL_EXECUTOR.getClass();
    }

}
