package org.prebid.mobile.app;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webContent;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static androidx.test.espresso.web.matcher.DomMatchers.containingTextInBody;
import static androidx.test.espresso.web.model.Atoms.getCurrentUrl;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ExtraTests {

    @Rule
    public ActivityTestRule<TestActivity> m = new ActivityTestRule<>(TestActivity.class);
    @Rule
    public GrantPermissionRule mGrant = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    MockWebServer server;

    @Before
    public void setUp() {
        server = new MockWebServer();
        try {
            server.start();
        } catch (IOException e) {
            fail("Mock server start failed.");
        }
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
        server = null;
    }

    @Test
    public void testRubiconDemand() throws Exception {
        m.getActivity().setUpRunbiconDemandTest();
        Thread.sleep(10000);
        assertEquals(ResultCode.SUCCESS, m.getActivity().resultCode);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
    }

    @Test
    public void testAppNexusKeyValueTargeting() throws Exception {
        m.getActivity().setUpAppNexusKeyValueTargetingTest();
        Thread.sleep(10000);
        assertEquals(ResultCode.SUCCESS, m.getActivity().resultCode);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
    }

    @Test
    public void testAppNexusKeyValueTargeting2() throws Exception {
        m.getActivity().setUpAppNexusKeyValueTargetingTest2();
        Thread.sleep(10000);
        assertEquals(ResultCode.NO_BIDS, m.getActivity().resultCode);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("Hello, I'm not a Prebid ad.")));
    }

    @Test
    public void testEmptyInvalidPrebidServerAccountId() throws Exception {
        m.getActivity().setUpEmptyPrebidServerAccountID();
        Thread.sleep(1000);
        assertEquals(ResultCode.INVALID_ACCOUNT_ID, m.getActivity().resultCode);
    }

    @Test
    public void testAppNexusInvalidPrebidServerAccountId() throws Exception {
        m.getActivity().setUpAppNexusInvalidPrebidServerAccountID();
        Thread.sleep(10000);
        assertEquals(ResultCode.INVALID_ACCOUNT_ID, m.getActivity().resultCode);
    }

    @Test
    public void testAppNexusAgeTargeting1() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/")) {
                    String postData = request.getBody().readUtf8();
                    assertTrue("Post data should not contain yob: " + postData, !postData.contains("yob"));
                    return getAppNexusDemand(postData);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        m.getActivity().setUpAppNexusAgeTargeting1(server.url("/").toString());
        Thread.sleep(10000);
        assertEquals(ResultCode.NO_BIDS, m.getActivity().resultCode);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("Hello, I'm not a Prebid ad.")));
    }

    @Test
    public void testAppNexusAgeTargeting2() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/")) {
                    String postData = request.getBody().readUtf8();
                    assertTrue("Post data should not contain yob: " + postData, !postData.contains("yob"));
                    return getAppNexusDemand(postData);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        m.getActivity().setUpAppNexusAgeTargeting2(server.url("/").toString());
        Thread.sleep(10000);
        assertEquals(ResultCode.NO_BIDS, m.getActivity().resultCode);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("Hello, I'm not a Prebid ad.")));
    }

    @Test
    public void testAppNexusAgeTargeting3() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/")) {
                    String postData = request.getBody().readUtf8();
                    assertTrue("Post data does not contain yob 2018: " + postData, postData.contains("2018"));
                    return getAppNexusDemand(postData);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        m.getActivity().setUpAppNexusAgeTargeting3(server.url("/").toString());
        Thread.sleep(10000);
        assertEquals(ResultCode.NO_BIDS, m.getActivity().resultCode);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("Hello, I'm not a Prebid ad.")));
    }

    @Test
    public void testAppNexusAgeTargeting4() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/")) {
                    String postData = request.getBody().readUtf8();
                    assertTrue("Post data does not contain yob 1989: " + postData, postData.contains("1989"));
                    return getAppNexusDemand(postData);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        m.getActivity().setUpAppNexusAgeTargeting4(server.url("/").toString());
        Thread.sleep(10000);
        assertEquals(ResultCode.SUCCESS, m.getActivity().resultCode);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
    }

    // This test should be run with a real device located in New York City, New York, USA
    @Test
    public void testLocationTargeting() {
        if (!TestUtil.isEmulator()) {
            try {
                server.setDispatcher(new Dispatcher() {
                    int lastfix = -1;

                    @Override
                    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                        String postData = request.getBody().readUtf8();
                        if (request.getPath().equals("/sharerealgeo")) {
                            assertTrue(postData.contains("geo"));
                            try {
                                JSONObject data = new JSONObject(postData);
                                lastfix = data.getJSONObject("device").getJSONObject("geo").getInt("lastfix");
                            } catch (JSONException e) {
                            }
                            return getAppNexusDemand(postData);
                        } else if (request.getPath().equals("/sharefakegeo")) {
                            assertTrue(!postData.contains("geo"));
                            // add a shanghai location to the request
                            try {
                                JSONObject data = new JSONObject(postData);
                                JSONObject device = data.getJSONObject("device");
                                JSONObject geo = new JSONObject();
                                geo.put("lat", 31.2304);
                                geo.put("lon", 121.4737);
                                geo.put("accuracy", 19);
                                if (lastfix > 0) {
                                    geo.put("lastfix", lastfix);
                                }
                                device.put("geo", geo);
                                data.put("device", device);
                                return getAppNexusDemand(data.toString());
                            } catch (JSONException e) {
                            }
                        }
                        return new MockResponse().setResponseCode(404);
                    }
                });
                // Global Settings
                PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
                PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
                // Share location
                PrebidMobile.setShareGeoLocation(true);
                Host.CUSTOM.setHostUrl(server.url("/sharerealgeo").toString());
                PrebidMobile.setPrebidServerHost(Host.CUSTOM);
                m.getActivity().setUpLocationTargeting();
                Thread.sleep(10000);
                assertEquals(ResultCode.SUCCESS, m.getActivity().resultCode);
                onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
                // Do not share location
                PrebidMobile.setShareGeoLocation(false);
                Host.CUSTOM.setHostUrl(server.url("/sharefakegeo").toString());
                PrebidMobile.setPrebidServerHost(Host.CUSTOM);
                m.getActivity().setUpLocationTargeting();
                Thread.sleep(10000);
                assertEquals(ResultCode.NO_BIDS, m.getActivity().resultCode);
                onWebView().check(webContent(containingTextInBody("Hello, I'm not a Prebid ad.")));
            } catch (InterruptedException e) {
                fail("Mock server take request interruption exception.");
            }
        }
    }

    private MockResponse getAppNexusDemand(String data) {
        try {
            URL url = new URL(Host.APPNEXUS.getHostUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);

            // Add post data
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            wr.write(data);
            wr.flush();

            // Start the connection
            conn.connect();
            // Read request response
            int httpResult = conn.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                StringBuilder builder = new StringBuilder();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                is.close();
                String result = builder.toString();

                return new MockResponse().setResponseCode(200).setBody(result);
            } else if (httpResult >= HttpURLConnection.HTTP_BAD_REQUEST) {
                StringBuilder builder = new StringBuilder();
                InputStream is = conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                is.close();
                String result = builder.toString();
                return new MockResponse().setResponseCode(httpResult).setBody(result);
            }
        } catch (Exception e) {
        }
        return new MockResponse().setResponseCode(500);
    }

}
