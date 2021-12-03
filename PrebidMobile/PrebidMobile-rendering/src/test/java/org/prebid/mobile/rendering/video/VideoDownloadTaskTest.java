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

package org.prebid.mobile.rendering.video;

import android.app.Activity;
import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.prebid.mobile.rendering.loading.FileDownloadListener;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class VideoDownloadTaskTest {
    private BaseNetworkTask.GetUrlParams mParams;
    private MockWebServer mServer;
    private String mPath;
    private String mError;
    private File mFile;
    private Context mContext;

    private FileDownloadListener mListener = new FileDownloadListener() {
        @Override
        public void onFileDownloaded(String path) {
            mPath = path;
        }

        @Override
        public void onFileDownloadError(String error) {
            mError = error;
        }
    };

    @Before
    public void setup() {
        mFile = new File("test");
        mServer = new MockWebServer();
        mParams = new BaseNetworkTask.GetUrlParams();
        mPath = null;
        mError = null;
        mParams.name = BaseNetworkTask.DOWNLOAD_TASK;
        mParams.userAgent = "user-agent";
        HttpUrl baseUrl = mServer.url("/first");
        mParams.url = baseUrl.url().toString();
        mParams.requestType = "GET";
        mContext = Robolectric.buildActivity(Activity.class).create().get();
    }

    @After
    public void tearDown() throws IOException {
        mServer.shutdown();
        mFile.delete();
    }

    @Test
    public void testSuccessDoInBackground() throws IOException {
        String body = ResourceUtils.convertResourceToString("mraid.js");
        mServer.enqueue(new MockResponse().setResponseCode(200).setBody(body));

        VideoDownloadTask baseNetworkTask = new VideoDownloadTask(mContext.getApplicationContext(), mFile, mListener, Mockito.mock(AdConfiguration.class));

        baseNetworkTask.execute(mParams);

        assertNotNull(mPath);
    }

    @Test(expected = NullPointerException.class)
    public void testNullFile() {
        VideoDownloadTask task = new VideoDownloadTask(mContext.getApplicationContext(), null, mListener, Mockito.mock(AdConfiguration.class));
        task.execute(mParams);
    }

    @Test
    public void testWrongData() {
        mServer.enqueue(new MockResponse().setResponseCode(401).setBody("Not found"));
        VideoDownloadTask task = new VideoDownloadTask(mContext.getApplicationContext(), mFile, mListener, Mockito.mock(AdConfiguration.class));
        task.execute(mParams);
        assertNotNull(mError);
    }
}