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

package org.prebid.mobile.rendering.loading;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class FileDownloadTaskTest {
    private BaseNetworkTask.GetUrlParams params;
    private MockWebServer server;
    private String path;
    private String error;
    private File file;

    private FileDownloadListener listener = new FileDownloadListener() {
        @Override
        public void onFileDownloaded(String path) {
            FileDownloadTaskTest.this.path = path;
        }

        @Override
        public void onFileDownloadError(String error) {
            FileDownloadTaskTest.this.error = error;
        }
    };

    @Before
    public void setup() {
        file = new File("test");
        server = new MockWebServer();
        params = new BaseNetworkTask.GetUrlParams();
        path = null;
        error = null;
        params.name = BaseNetworkTask.DOWNLOAD_TASK;
        params.userAgent = "user-agent";
        HttpUrl baseUrl = server.url("/first");
        params.url = baseUrl.url().toString();
        params.requestType = "GET";
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
        file.delete();
    }

    @Test
    public void testSuccessDoInBackground() throws IOException {
        String body = ResourceUtils.convertResourceToString("mraid.js");
        server.enqueue(new MockResponse().setResponseCode(200).setBody(body));

        FileDownloadTask baseNetworkTask = new FileDownloadTask(listener, file);

        baseNetworkTask.execute(params);

        assertNotNull(path);
    }

    @Test(expected = NullPointerException.class)
    public void testNullFile() {
        FileDownloadTask task = new FileDownloadTask(listener, null);
        task.execute(params);
    }

    @Test
    public void testWrongData() {
        server.enqueue(new MockResponse().setResponseCode(401).setBody("Not found"));
        FileDownloadTask task = new FileDownloadTask(listener, file);
        task.execute(params);
        assertNotNull(error);
    }
}