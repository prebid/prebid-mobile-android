package org.prebid.mobile.rendering.loading;

import com.apollo.test.utils.ResourceUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class FileDownloadTaskTest {
    private BaseNetworkTask.GetUrlParams mParams;
    private MockWebServer mServer;
    private String mPath;
    private String mError;
    private File mFile;

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

        FileDownloadTask baseNetworkTask = new FileDownloadTask(mListener, mFile);

        baseNetworkTask.execute(mParams);

        assertNotNull(mPath);
    }

    @Test(expected = NullPointerException.class)
    public void testNullFile() {
        FileDownloadTask task = new FileDownloadTask(mListener, null);
        task.execute(mParams);
    }

    @Test
    public void testWrongData() {
        mServer.enqueue(new MockResponse().setResponseCode(401).setBody("Not found"));
        FileDownloadTask task = new FileDownloadTask(mListener, mFile);
        task.execute(mParams);
        assertNotNull(mError);
    }
}