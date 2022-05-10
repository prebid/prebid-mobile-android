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

package org.prebid.mobile.rendering.mraid.methods.network;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.BaseNetworkTask.GetUrlResult;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.prebid.mobile.rendering.networking.BaseNetworkTask.REDIRECT_TASK;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 25)
public class GetOriginalUrlTaskTest {

    private boolean success;
    private GetUrlResult response;
    private MockWebServer server;
    private String msg;
    private BaseNetworkTask.GetUrlParams params;
    private Exception exception;

    private ResponseHandler baseResponseHandler = new ResponseHandler() {

        @Override
        public void onResponse(GetUrlResult response) {
            success = true;
            GetOriginalUrlTaskTest.this.response = response;
        }

        @Override
        public void onError(String msg, long responseTime) {
            success = false;
            GetOriginalUrlTaskTest.this.msg = msg;
        }

        @Override
        public void onErrorWithException(Exception e, long responseTime) {
            success = false;
            exception = e;
        }
    };

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        params = new BaseNetworkTask.GetUrlParams();
        params.name = "TESTFIRST";
        params.userAgent = "user-agent";
        HttpUrl baseUrl = server.url("/first");
        params.url = baseUrl.url().toString();
        params.requestType = "GET";
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void testSuccessDoInBackground() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("This is google!"));

        GetOriginalUrlTask baseNetworkTask = new GetOriginalUrlTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(true, success);
    }

    @Test
    public void test400ExceptionError() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(400).setBody("404 not found"));

        GetOriginalUrlTask baseNetworkTask = new GetOriginalUrlTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(exception.getLocalizedMessage().contains("Code 400"));
        assertEquals(false, success);
    }

    @Test
    public void test401ExceptionError() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(401).setBody("404 not found"));

        GetOriginalUrlTask baseNetworkTask = new GetOriginalUrlTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(exception.getLocalizedMessage().contains("Code 401"));
        assertEquals(false, success);
    }

    @Test
    public void test402ExceptionError() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(402).setBody("404 not found"));

        GetOriginalUrlTask baseNetworkTask = new GetOriginalUrlTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(exception.getLocalizedMessage().contains("Code 402"));
        assertEquals(false, success);
    }

    @Test
    public void test403ExceptionError() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(403).setBody("404 not found"));

        GetOriginalUrlTask baseNetworkTask = new GetOriginalUrlTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(exception.getLocalizedMessage().contains("Code 403"));
        assertEquals(false, success);
    }

    @Test
    public void test404ExceptionError() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(404).setBody("404 not found"));

        GetOriginalUrlTask baseNetworkTask = new GetOriginalUrlTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(exception.getLocalizedMessage().contains("Code 404"));
        assertEquals(false, success);
    }

    @Test
    public void test405ExceptionError() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(405).setBody("404 not found"));

        GetOriginalUrlTask baseNetworkTask = new GetOriginalUrlTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(exception.getLocalizedMessage().contains("Code 405"));
        assertEquals(false, success);
    }

    @Test
    public void testEmptyVast() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("<VAST version=\"2.0\"></VAST>"));

        GetOriginalUrlTask baseNetworkTask = new GetOriginalUrlTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals("Invalid VAST Response: less than 100 characters.", msg);
        assertEquals(false, success);
    }

    @Test
    public void test301Redirect() throws IOException {
        server.enqueue(new MockResponse()
                            .setResponseCode(301)
                            .addHeader("Location: " + server.url("/new-path"))
                            .setBody("This page has moved!"));
        server.enqueue(new MockResponse().setBody("This is the new location!"));

        GetOriginalUrlTask baseNetworkTask = new GetOriginalUrlTask(baseResponseHandler);

        params.name = REDIRECT_TASK;

        try {
            baseNetworkTask.execute(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(true, success);
    }

    @Test
    public void testCustomParserWith200Code() throws IOException {

        server.enqueue(new MockResponse().setResponseCode(200).setBody("this is a success response"));

        GetOriginalUrlTask baseNetworkTask = new GetOriginalUrlTask(null);

        HttpUrl baseUrl = server.url("/first");
        URL uUrl = new URL(baseUrl.url().toString());
        HttpURLConnection con = (HttpURLConnection) uUrl.openConnection();

        con.setInstanceFollowRedirects(false);

        con.setRequestProperty(ResourceUtils.USER_AGENT_HEADER, "Mozilla/5.0 (Linux; U; Android " + android.os.Build.VERSION.RELEASE + ";" + " ");
        con.setRequestProperty(ResourceUtils.ACCEPT_LANGUAGE_HEADER, Locale.getDefault().toString());
        con.setRequestProperty(ResourceUtils.ACCEPT_ENCODING_HEADER, "gzip");
        con.setRequestProperty(ResourceUtils.ACCEPT_HEADER, ResourceUtils.ACCEPT_HEADER_VALUE);
        con.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
        BaseNetworkTask.GetUrlResult res = baseNetworkTask.customParser(200, con);
        Assert.assertNull("should have content type as: null(for code:200), but got: " + res.contentType, res.contentType);
        Assert.assertFalse("should have JSRedirectURI[0]: 'www.MenloPark.com' but got: " + res.JSRedirectURI[0], "www.MenloPark.com".equals(res.JSRedirectURI[0]));
        assertTrue("should have JSRedirectURI[1]: 'text/html; charset=UTF-8' but got: " + res.JSRedirectURI[1], "text/html; charset=UTF-8".equals(res.JSRedirectURI[1]));
        assertTrue("should have JSRedirectURI[2]: 'quit' but got: " + res.JSRedirectURI[2], "quit".equals(res.JSRedirectURI[2]));
    }

    @Test
    public void isRedirectTest() throws Exception {
        Method methodIsRedirect = WhiteBox.method(GetOriginalUrlTask.class, "isRedirect", int.class);

        GetOriginalUrlTask baseNetworkTask = new GetOriginalUrlTask(null);
        boolean isRedirect = (boolean) methodIsRedirect.invoke(baseNetworkTask, 301);
        assertTrue("301 should be True, but got False", isRedirect);
        isRedirect = (boolean) methodIsRedirect.invoke(baseNetworkTask, 302);
        assertTrue("302 should be True, but got False", isRedirect);
        isRedirect = (boolean) methodIsRedirect.invoke(baseNetworkTask, 302);
        assertTrue("303 should be True, but got False", isRedirect);
        isRedirect = (boolean) methodIsRedirect.invoke(baseNetworkTask, 302);
        assertTrue("307 should be True, but got False", isRedirect);
        isRedirect = (boolean) methodIsRedirect.invoke(baseNetworkTask, 302);
        assertTrue("308 should be True, but got False", isRedirect);
        isRedirect = (boolean) methodIsRedirect.invoke(baseNetworkTask, 200);
        Assert.assertFalse("200 should be False, but got True", isRedirect);
    }


    @Test
    public void testCustomParserWith301Code() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(301).setBody("this is a success response"));
        GetOriginalUrlTask baseNetworkTask = new GetOriginalUrlTask(null);
        HttpUrl baseUrl = server.url("/first");
        URL uUrl = new URL(baseUrl.url().toString());
        HttpURLConnection con = (HttpURLConnection) uUrl.openConnection();
        con.setInstanceFollowRedirects(false);
        con.setRequestProperty(ResourceUtils.USER_AGENT_HEADER, "Mozilla/5.0 (Linux; U; Android " + android.os.Build.VERSION.RELEASE + ";" + " ");
        con.setRequestProperty(ResourceUtils.ACCEPT_LANGUAGE_HEADER, Locale.getDefault().toString());
        con.setRequestProperty(ResourceUtils.ACCEPT_ENCODING_HEADER, "gzip");
        con.setRequestProperty(ResourceUtils.ACCEPT_HEADER, ResourceUtils.ACCEPT_HEADER_VALUE);
        con.setRequestProperty("Location", "www.MenloPark.com");
        con.setRequestProperty("Content-Type", "text/html; charset=UTF-8");

        BaseNetworkTask.GetUrlResult res = baseNetworkTask.customParser(301, con);
        Assert.assertNull("should have content type as: null(for code:301), but got: " + res.contentType, res.contentType);
        assertTrue("should have JSRedirectURI[0]: 'www.MenloPark.com' but got: " + res.JSRedirectURI[0], "www.MenloPark.com".equals(res.JSRedirectURI[0]));
        assertTrue("should have JSRedirectURI[1]: 'text/html; charset=UTF-8' but got: " + res.JSRedirectURI[1], "text/html; charset=UTF-8".equals(res.JSRedirectURI[1]));
    }

    @Test
    public void testCustomParserWith308Code() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(308).setBody("this is a success response"));
        GetOriginalUrlTask baseNetworkTask = new GetOriginalUrlTask(null);
        HttpUrl baseUrl = server.url("/first");
        URL uUrl = new URL(baseUrl.url().toString());
        HttpURLConnection con = (HttpURLConnection) uUrl.openConnection();
        con.setInstanceFollowRedirects(false);
        con.setRequestProperty(ResourceUtils.USER_AGENT_HEADER, "Mozilla/5.0 (Linux; U; Android " + android.os.Build.VERSION.RELEASE + ";" + " ");
        con.setRequestProperty(ResourceUtils.ACCEPT_LANGUAGE_HEADER, Locale.getDefault().toString());
        con.setRequestProperty(ResourceUtils.ACCEPT_ENCODING_HEADER, "gzip");
        con.setRequestProperty(ResourceUtils.ACCEPT_HEADER, ResourceUtils.ACCEPT_HEADER_VALUE);
        con.setRequestProperty("Location", "www.MenloPark.com");
        con.setRequestProperty("Content-Type", "text/html; charset=UTF-8");

        BaseNetworkTask.GetUrlResult res = baseNetworkTask.customParser(308, con);
        Assert.assertNull("should have content type as: null(for code:301), but got: " + res.contentType, res.contentType);
        assertTrue("should have JSRedirectURI[0]: 'www.MenloPark.com' but got: " + res.JSRedirectURI[0], "www.MenloPark.com".equals(res.JSRedirectURI[0]));
        assertTrue("should have JSRedirectURI[1]: 'text/html; charset=UTF-8' but got: " + res.JSRedirectURI[1], "text/html; charset=UTF-8".equals(res.JSRedirectURI[1]));
    }

    @Test
    public void getRedirectionUrlWithTypeWithNoUrlParamTest() throws Exception {
        GetOriginalUrlTask getOriginalUrlNetworkTask = new GetOriginalUrlTask(null);
        params.url = "";
        String[] redirectObject = (String[]) WhiteBox.method(GetOriginalUrlTask.class,
                                                             "getRedirectionUrlWithType",
                                                             BaseNetworkTask.GetUrlParams.class)
                                                     .invoke(getOriginalUrlNetworkTask, params);
        Assert.assertEquals("A param object with empty url should return {'', null null}", redirectObject[0], "");
        Assert.assertEquals("A param object with empty url should return {'', null null}", redirectObject[1], null);
        Assert.assertEquals("A param object with empty url should return {'', null null}", redirectObject[2], null);
    }

    @Test
    public void getRedirectionUrlWithTypeWithRecognizedPrefixParamTest() throws Exception {
        Method methodGetRedirectionUrl = WhiteBox.method(GetOriginalUrlTask.class, "getRedirectionUrlWithType", BaseNetworkTask.GetUrlParams.class);

        GetOriginalUrlTask getOriginalUrlNetworkTask = new GetOriginalUrlTask(null);
        params.url = "tel://123456789";
        String[] redirectObject = (String[]) methodGetRedirectionUrl.invoke(getOriginalUrlNetworkTask, params);
        Assert.assertEquals("A url param object with tel: should return {'', null null}", redirectObject[0], "tel://123456789");
        Assert.assertEquals("A url param object with tel: url should return {'', null null}", redirectObject[1], null);
        Assert.assertEquals("A url param object with tel: url should return {'', null null}", redirectObject[2], null);
        params.url = "voicemail://123456789";
        redirectObject = (String[]) methodGetRedirectionUrl.invoke(getOriginalUrlNetworkTask, params);
        Assert.assertEquals("A url param object with voicemail: should return {'', null null}", redirectObject[0], "voicemail://123456789");
        Assert.assertEquals( "A url param object with voicemail: url should return {'', null null}", redirectObject[1], null);
        Assert.assertEquals( "A url param object with voicemail: url should return {'', null null}", redirectObject[2], null);
        params.url = "sms://123456789";
        redirectObject = (String[]) methodGetRedirectionUrl.invoke(getOriginalUrlNetworkTask, params);
        Assert.assertEquals("A url param object with sms: should return {'', null null}", redirectObject[0], "sms://123456789");
        Assert.assertEquals( "A url param object with sms: url should return {'', null null}", redirectObject[1], null);
        Assert.assertEquals( "A url param object with sms: url should return {'', null null}", redirectObject[2], null);
        params.url = "mailto://123456789";
        redirectObject = (String[]) methodGetRedirectionUrl.invoke(getOriginalUrlNetworkTask, params);
        Assert.assertEquals("A url param object with mailto: should return {'', null null}", redirectObject[0], "mailto://123456789");
        Assert.assertEquals( "A url param object with mailto: url should return {'', null null}", redirectObject[1], null);
        Assert.assertEquals( "A url param object with mailto: url should return {'', null null}", redirectObject[2], null);
        params.url = "geo://123456789";
        redirectObject = (String[]) methodGetRedirectionUrl.invoke(getOriginalUrlNetworkTask, params);
        Assert.assertEquals("A url param object with tel: should return {'', null null}", redirectObject[0], "geo://123456789");
        Assert.assertEquals( "A url param object with tel: url should return {'', null null}", redirectObject[1], null);
        Assert.assertEquals( "A url param object with tel: url should return {'', null null}", redirectObject[2], null);
        params.url = "google.streetview://123456789";
        redirectObject = (String[]) methodGetRedirectionUrl.invoke(getOriginalUrlNetworkTask, params);
        Assert.assertEquals("A url param object with geo: should return {'', null null}", redirectObject[0], "google.streetview://123456789");
        Assert.assertEquals( "A url param object with geo: url should return {'', null null}", redirectObject[1], null);
        Assert.assertEquals( "A url param object with geo: url should return {'', null null}", redirectObject[2], null);
        params.url = "market://123456789";
        redirectObject = (String[]) methodGetRedirectionUrl.invoke(getOriginalUrlNetworkTask, params);
        Assert.assertEquals("A url param object with market: should return {'', null null}", redirectObject[0], "market://123456789");
        Assert.assertEquals( "A url param object with market: url should return {'', null null}", redirectObject[1], null);
        Assert.assertEquals( "A url param object with market: url should return {'', null null}", redirectObject[2], null);
    }

    @Test
    public void getRedirectionUrlWithTypeWithRedirectUrlTest() {
        server.enqueue(new MockResponse()
                            .setResponseCode(301)
                            .addHeader("Location: " + server.url("/new-path"))
                            .setBody("This page has moved!"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("This is the new location"));
        GetOriginalUrlTask getOriginalUrlNetworkTask = new GetOriginalUrlTask(baseResponseHandler);
        params.name = REDIRECT_TASK;

        try {
            getOriginalUrlNetworkTask.execute(params);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(true, success);
    }

    @Test
    public void getRedirectionUrlWithTypeWithNoRedirectUrlTest() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("this is a success response"));
        GetOriginalUrlTask getOriginalUrlNetworkTask = new GetOriginalUrlTask(null);
        HttpUrl baseUrl = server.url("/first");
        params.url = baseUrl.url().toString();
        String[] redirectObject = (String[]) WhiteBox.method(GetOriginalUrlTask.class,
                                                             "getRedirectionUrlWithType",
                                                             BaseNetworkTask.GetUrlParams.class)
                                                     .invoke(getOriginalUrlNetworkTask, params);
        Assert.assertEquals("A param object with non-redirect url should return {non-redirect, application/json quit}", redirectObject[0], server.url("/first").toString());
        Assert.assertEquals("A param object with non-redirect url should return {non-redirect, application/json quit}", redirectObject[1], "application/json");
        Assert.assertEquals("A param object with non-redirect url should return {non-redirect, application/json quit}", redirectObject[2], "quit");
    }

    @Test
    public void getUrlTest() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("this is a success response"));
        GetOriginalUrlTask getOriginalUrlNetworkTask = new GetOriginalUrlTask(null);
        HttpUrl baseUrl = server.url("/first");
        params.url = baseUrl.url().toString();
        BaseNetworkTask.GetUrlParams[] params = {this.params};
        GetUrlResult result = (GetUrlResult) WhiteBox.method(GetOriginalUrlTask.class, "getUrl", BaseNetworkTask.GetUrlParams[].class)
                                                     .invoke(getOriginalUrlNetworkTask, (Object) new BaseNetworkTask.GetUrlParams[]{this.params});
        Assert.assertEquals("A successful response should yile a result object with status code 200", result.statusCode, 200);
    }

    @Test
    public void getUrlNullParamsTest() throws Exception {
        GetOriginalUrlTask getOriginalUrlNetworkTask = new GetOriginalUrlTask(null);
        params = null;
        GetUrlResult result = (GetUrlResult) WhiteBox.method(GetOriginalUrlTask.class, "getUrl", BaseNetworkTask.GetUrlParams.class).invoke(getOriginalUrlNetworkTask,
                params
        );
        assertNotNull("A null params object should generate a result object with Exception",result.getException());
    }

    @Test
    public void getUrlOriginalUrlShouldBeSetTest() throws Exception {
        server.enqueue(new MockResponse()
                            .setResponseCode(301)
                            .addHeader("Location: " + server.url("/new-path"))
                            .setBody("This page has moved!"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("This is the new location"));
        GetOriginalUrlTask getOriginalUrlNetworkTask = new GetOriginalUrlTask(null);
        HttpUrl baseUrl = server.url("/first");
        params.url = baseUrl.url().toString();
        GetUrlResult result = (GetUrlResult) WhiteBox.method(GetOriginalUrlTask.class, "getUrl", BaseNetworkTask.GetUrlParams[].class)
                                                     .invoke(getOriginalUrlNetworkTask, (Object) new BaseNetworkTask.GetUrlParams[]{params});
        Assert.assertNotNull("Original url should not be null", result.originalUrl);
    }

    @Test
    public void processRedirectsTest() {
        server.enqueue(new MockResponse()
                            .setResponseCode(301)
                            .addHeader("Location: " + server.url("/second"))
                            .setBody("This page has moved!"));
        server.enqueue(new MockResponse()
                            .setResponseCode(301)
                            .addHeader("Location: " + server.url("/third"))
                            .setBody("This page has moved!"));
        server.enqueue(new MockResponse()
                            .setResponseCode(301)
                            .addHeader("Location: " + server.url("/fourth"))
                            .setBody("This page has moved!"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("This is the new location"));
        GetOriginalUrlTask getOriginalUrlNetworkTask = new GetOriginalUrlTask(baseResponseHandler);
        params.name = REDIRECT_TASK;
        try {
            getOriginalUrlNetworkTask.execute(params);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals("Should iterate through max of 3 redirects after the original landing on the '4th' url ", true,
                success
        );
    }

    @Test
    public void processRedirectsMaxOf3RedirectsTest() throws Exception {
        server.enqueue(new MockResponse()
                            .setResponseCode(301)
                            .addHeader("Location: " + server.url("/second"))
                            .setBody("This page has moved!"));
        server.enqueue(new MockResponse()
                            .setResponseCode(301)
                            .addHeader("Location: " + server.url("/third"))
                            .setBody("This page has moved!"));
        server.enqueue(new MockResponse()
                            .setResponseCode(301)
                            .addHeader("Location: " + server.url("/fourth"))
                            .setBody("This page has moved!"));
        server.enqueue(new MockResponse()
                            .setResponseCode(301)
                            .addHeader("Location: " + server.url("/fifth"))
                            .setBody("This page has moved!"));

        server.enqueue(new MockResponse().setResponseCode(200).setBody("This is the new location"));

        GetOriginalUrlTask getOriginalUrlNetworkTask = new GetOriginalUrlTask(null);
        HttpUrl baseUrl = server.url("/first");
        params.url = baseUrl.url().toString();
        params.name = REDIRECT_TASK;
        WhiteBox.method(GetOriginalUrlTask.class, "processRedirects", BaseNetworkTask.GetUrlParams.class).invoke(getOriginalUrlNetworkTask,
                params
        );
        GetUrlResult result = (GetUrlResult) WhiteBox.getInternalState(getOriginalUrlNetworkTask, "result");
        Assert.assertNull("Should not have a valid result.response if over max redirects ", result.responseString);
    }

}
