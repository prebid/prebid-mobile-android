package org.prebid.mobile;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.tasksmanager.BackgroundThreadExecutor;
import org.prebid.mobile.tasksmanager.TasksManager;
import org.prebid.mobile.testutils.BaseSetup;
import org.prebid.mobile.testutils.MockPrebidServerResponses;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class AdUnitContentTest extends BaseSetup {

    @Test
    public void testAdUnitUsingContentUrl() throws Exception {
        final String expectedContentUrl = "http://www.something.com/somewhere/here";
        HttpUrl hostUrl = server.url("/");
        Host.CUSTOM.setHostUrl(hostUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        final CompletableFuture<Void> future = new CompletableFuture<>();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String postData = request.getBody().readUtf8();
                System.out.println("I am in dispatch");
                try {
                    JSONObject jsonObject = new JSONObject(postData);

                    JSONObject app = jsonObject.getJSONObject("app");
                    assertTrue(app.has("content"));
                    JSONObject content = app.getJSONObject("content");
                    assertTrue(content.has("url"));
                    assertEquals(expectedContentUrl, content.getString("url"));

                    future.complete(null);
                } catch (JSONException err) {
                    Log.d("Error", err.toString());
                    future.cancel(true);
                }
                return new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid());
            }
        });

        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        ContentObject contentObject = new ContentObject();
        contentObject.setUrl(expectedContentUrl);
        adUnit.addContent(contentObject);


        adUnit.fetchDemand(new OnCompleteListener2() {
            @Override
            public void onComplete(ResultCode resultCode, Map<String, String> unmodifiableMap) {
                System.out.println("I am in onComplete " + resultCode.toString());
                future.cancel(true);
            }
        });

        future.get(10, TimeUnit.SECONDS);
    }
}
