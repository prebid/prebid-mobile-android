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

package org.prebid.mobile;

import android.support.annotation.Nullable;

import com.mopub.mobileads.MoPubView;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.prebid.mobile.testutils.MockPrebidServerResponses;
import org.prebid.mobile.testutils.Utils;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK, manifest = Config.NONE)
public class PrebidServerAdapterTest extends BaseSetup {

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Override
    public void tearDown() {
        super.tearDown();

        TargetingParams.clearAccessControlList();
        TargetingParams.clearUserData();
        TargetingParams.clearContextData();
        TargetingParams.clearContextKeywords();
        TargetingParams.clearUserKeywords();
    }

    @Test
    public void testRegexMatch() {
        Pattern p = Pattern.compile("^Invalid request: Stored Request with ID=\".*\" not found.");
        String s = "Invalid request: Stored Request with ID=\"6ace8c7d-88c0-4623-8117-75bc3f0a2edd\" not found.";
        Matcher m = p.matcher(s);
        assertTrue(m.find());
        Pattern p2 = Pattern.compile("^Invalid request: Stored Imp with ID=\".*\" not found.");
        String s2 = "Invalid request: Stored Imp with ID=\"6ace8c7d-88c0-4623-8117-75bc3f0a2edd\" not found.";
        Matcher m2 = p2.matcher(s2);
        assertTrue(m2.find());
        Pattern p3 = Pattern.compile("^Invalid request: Request imp\\[\\d\\].banner.format\\[\\d\\] must define non-zero \"h\" and \"w\" properties.");
        String s3 = "Invalid request: Request imp[0].banner.format[0] must define non-zero \"h\" and \"w\" properties.";
        Matcher m3 = p3.matcher(s3);
        assertTrue(m3.find());
        Pattern p4 = Pattern.compile("^Invalid request: Unable to set interstitial size list");
        String s4 = "Invalid request: Unable to set interstitial size list for Imp id=PrebidMobile (No valid sizes between 0x10 and 1x25)";
        Matcher m4 = p4.matcher(s4);
        assertTrue(m4.find());

    }

    @Test
    public void testInvalidPrebidServerAccountIdForAppNexusHostedPrebidServer() {
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-ffffffffffff");
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(320, 50));
        RequestParams requestParams = new RequestParams("6ace8c7d-88c0-4623-8117-75bc3f0a2e45", AdType.BANNER, sizes);
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verify(mockListener).onDemandFailed(ResultCode.INVALID_ACCOUNT_ID, uuid);
    }

    @Test
    public void testInvalidPrebidServerAccountIdForRubiconHostedPrebidServer() {
        if (!successfulMockServerStarted) {
            fail("Mock server was not started");
        }
        server.enqueue(new MockResponse().setResponseCode(400).setBody(MockPrebidServerResponses.invalidAccountIdFromRubicon()));
        HttpUrl hostUrl = server.url("/");
        Host.CUSTOM.setHostUrl(hostUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);

        PrebidMobile.setPrebidServerAccountId("1001_INVALID_ACCOUNT_ID");
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(300, 250));
        RequestParams requestParams = new RequestParams("1001-1", AdType.BANNER, sizes);
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        verify(mockListener).onDemandFailed(ResultCode.INVALID_ACCOUNT_ID, uuid);

    }

    @Test
    public void testInvalidPrebidServerConfigIdForAppNexusHostedPrebidServer() {
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(320, 50));
        RequestParams requestParams = new RequestParams("6ace8c7d-88c0-4623-8117-ffffffffffff", AdType.BANNER, sizes);
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verify(mockListener).onDemandFailed(ResultCode.INVALID_CONFIG_ID, uuid);
    }

    @Test
    public void testInvalidPrebidServerConfigIdForRubiconHostedPrebidServer() {

        if (!successfulMockServerStarted) {
            fail("Mock server was not started");
        }
        server.enqueue(new MockResponse().setResponseCode(400).setBody(MockPrebidServerResponses.invalidConfigIdFromRubicon()));
        HttpUrl hostUrl = server.url("/");
        Host.CUSTOM.setHostUrl(hostUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);

        PrebidMobile.setPrebidServerAccountId("1001");
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(300, 250));
        RequestParams requestParams = new RequestParams("1001-1_INVALID_CONFIG_ID", AdType.BANNER, sizes);
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        verify(mockListener).onDemandFailed(ResultCode.INVALID_CONFIG_ID, uuid);
    }

    @Test
    public void testInvalidPrebidServerIdSyntaxForAppNexusHostedPrebidServer() {
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888d");
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(320, 50));
        RequestParams requestParams = new RequestParams("6ace8c7d-88c0-4623-8117-75bc3f0a2e45", AdType.BANNER, sizes);
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verify(mockListener).onDemandFailed(ResultCode.PREBID_SERVER_ERROR, uuid);
    }

    @Test
    public void testInvalidPrebidServerIdSyntaxForAppNexusHostedPrebidServer2() {
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(320, 50));
        RequestParams requestParams = new RequestParams("6ace8c7d-88c0-4623-8117-75bc3f0a2e", AdType.BANNER, sizes);
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verify(mockListener).onDemandFailed(ResultCode.PREBID_SERVER_ERROR, uuid);
    }

    @Test
    public void testUpdateTimeoutMillis() {
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        assertEquals(2000, PrebidMobile.getTimeoutMillis());
        assertFalse(PrebidMobile.timeoutMillisUpdated);
        PrebidMobile.setPrebidServerAccountId("b7adad2c-e042-4126-8ca1-b3caac7d3e5c");
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(300, 250));
        RequestParams requestParams = new RequestParams("e2edc23f-0b3b-4203-81b5-7cc97132f418", AdType.BANNER, sizes);
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verify(mockListener).onDemandFailed(ResultCode.NO_BIDS, uuid);
        assertTrue("Actual Prebid Mobile timeout is " + PrebidMobile.getTimeoutMillis(), PrebidMobile.getTimeoutMillis() <= 2000 && PrebidMobile.getTimeoutMillis() > 700);
        assertTrue(PrebidMobile.timeoutMillisUpdated);
    }

    @Test
    public void testUpdateTimeoutMillis2() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBidResponseNoTmax()));
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBidResponseTmaxTooLarge()));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setPrebidServerAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
            String uuid = UUID.randomUUID().toString();
            adapter.requestDemand(requestParams, mockListener, uuid);
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            assertEquals("Actual Prebid Mobile timeout is " + PrebidMobile.getTimeoutMillis(), 2000, PrebidMobile.getTimeoutMillis());
            assertTrue(!PrebidMobile.timeoutMillisUpdated);
            adapter.requestDemand(requestParams, mockListener, uuid);
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            assertEquals("Actual Prebid Mobile timeout is " + PrebidMobile.getTimeoutMillis(), 2000, PrebidMobile.getTimeoutMillis());
            assertTrue(PrebidMobile.timeoutMillisUpdated);
        } else {
            assertTrue("Server failed to start, unable to test.", false);
        }
    }

    @Test
    public void testNoBidResponse() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setPrebidServerAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(320, 50));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
            String uuid = UUID.randomUUID().toString();
            adapter.requestDemand(requestParams, mockListener, uuid);
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onDemandFailed(ResultCode.NO_BIDS, uuid);
        } else {
            assertTrue("Server failed to start, unable to test.", false);
        }
    }

    @Test
    public void testNoBidRubiconResponse() {
        if (!successfulMockServerStarted) {
            fail("Server failed to start, unable to test.");
        }

        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBidFromRubicon()));
        HttpUrl hostUrl = server.url("/");
        Host.CUSTOM.setHostUrl(hostUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setPrebidServerAccountId("12345");
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(320, 50));
        RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verify(mockListener).onDemandFailed(ResultCode.NO_BIDS, uuid);

    }

    @Test
    public void testSuccessfulBidResponse() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus()));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setPrebidServerAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
            String uuid = UUID.randomUUID().toString();
            adapter.requestDemand(requestParams, mockListener, uuid);
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            HashMap<String, String> bids = new HashMap<String, String>();
            bids.put("hb_bidder", "appnexus");
            bids.put("hb_bidder_appnexus", "appnexus");
            bids.put("hb_cache_id", "df4aba04-5e69-44b8-8608-058ab21600b8");
            bids.put("hb_cache_id_appnexus", "df4aba04-5e69-44b8-8608-058ab21600b8");
            bids.put("hb_env", "mobile-app");
            bids.put("hb_env_appnexus", "mobile-app");
            bids.put("hb_pb", "0.50");
            bids.put("hb_pb_appnexus", "0.50");
            bids.put("hb_size", "300x250");
            bids.put("hb_size_appnexus", "300x250");
            verify(mockListener).onDemandReady(bids, uuid);
        } else {
            assertTrue("Server failed to start, unable to test.", false);
        }
    }

    @Test
    public void testSuccessfulBidRubiconResponse() {
        if (!successfulMockServerStarted) {
            fail("Server failed to start, unable to test.");
        }

        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromRubicon()));
        HttpUrl hostUrl = server.url("/");
        Host.CUSTOM.setHostUrl(hostUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setPrebidServerAccountId("12345");
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(300, 250));
        RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        HashMap<String, String> bids = new HashMap<String, String>();
        bids.put("hb_bidder", "rubicon");
        bids.put("hb_bidder_rubicon", "rubicon");
        bids.put("hb_cache_id", "a2f41588-4727-425c-9ef0-3b382debef1e");
        bids.put("hb_cache_id_rubicon", "a2f41588-4727-425c-9ef0-3b382debef1e");
        bids.put("hb_env", "mobile-app");
        bids.put("hb_env_rubicon", "mobile-app");
        bids.put("hb_pb", "1.20");
        bids.put("hb_pb_rubicon", "1.20");
        bids.put("hb_size", "300x250");
        bids.put("hb_size_rubicon", "300x250");

        bids.put("hb_cache_hostpath", "https://prebid-cache-europe.rubiconproject.com/cache");
        bids.put("hb_cache_hostpath_rubicon", "https://prebid-cache-europe.rubiconproject.com/cache");
        bids.put("hb_cache_path", "/cache");
        bids.put("hb_cache_path_rubicon", "/cache");
        bids.put("hb_cache_host", "prebid-cache-europe.rubiconproject.com");
        bids.put("hb_cache_host_rubicon", "prebid-cache-europe.rubiconproject.com");
        verify(mockListener).onDemandReady(bids, uuid);

    }

    @Test
    public void testSuccessfulBidResponseWithoutCacheId() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.invalidBidResponseWithoutCacheId()));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setPrebidServerAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
            String uuid = UUID.randomUUID().toString();
            adapter.requestDemand(requestParams, mockListener, uuid);
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onDemandFailed(ResultCode.NO_BIDS, uuid);
        } else {
            assertTrue("Server failed to start, unable to test.", false);
        }
    }

    @Test
    public void testSuccessfulBidRubiconResponseWithoutCacheId() {
        if (!successfulMockServerStarted) {
            fail("Server failed to start, unable to test.");
        }

        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.invalidBidRubiconResponseWithoutCacheId()));
        HttpUrl hostUrl = server.url("/");
        Host.CUSTOM.setHostUrl(hostUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setPrebidServerAccountId("12345");
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(300, 250));
        RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verify(mockListener).onDemandFailed(ResultCode.NO_BIDS, uuid);
    }

    @Test
    public void testSuccessfulBidResponseTwoBidsOnTheSameSeat() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.validBidResponseTwoBidsOnTheSameSeat()));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setPrebidServerAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
            String uuid = UUID.randomUUID().toString();
            adapter.requestDemand(requestParams, mockListener, uuid);
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            HashMap<String, String> bids = new HashMap<String, String>();
            bids.put("hb_bidder", "appnexus");
            bids.put("hb_bidder_appnexus", "appnexus");
            bids.put("hb_cache_id", "fdc4a3b1-ecdd-4c0a-b043-7ed66dca0553");
            bids.put("hb_cache_id_appnexus", "fdc4a3b1-ecdd-4c0a-b043-7ed66dca0553");
            bids.put("hb_env", "mobile-app");
            bids.put("hb_env_appnexus", "mobile-app");
            bids.put("hb_pb", "0.08");
            bids.put("hb_pb_appnexus", "0.08");
            bids.put("hb_size", "300x250");
            bids.put("hb_size_appnexus", "300x250");
            verify(mockListener).onDemandReady(bids, uuid);
        } else {
            assertTrue("Server failed to start, unable to test.", false);
        }
    }

    @Test
    public void testSuccessfulBidResponseWithoutCacheId2() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.validResponseAppNexusNoCacheIdAndRubiconHasCacheId()));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setPrebidServerAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
            String uuid = UUID.randomUUID().toString();
            adapter.requestDemand(requestParams, mockListener, uuid);
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            HashMap<String, String> bids = new HashMap<String, String>();
            bids.put("hb_bidder", "rubicon");
            bids.put("hb_bidder_rubicon", "rubicon");
            bids.put("hb_cache_id", "bd8d6eeb-8ad1-402c-a1f8-09565bb0bda7");
            bids.put("hb_cache_id_rubicon", "bd8d6eeb-8ad1-402c-a1f8-09565bb0bda7");
            bids.put("hb_env", "mobile-app");
            bids.put("hb_env_rubicon", "mobile-app");
            bids.put("hb_pb", "1.20");
            bids.put("hb_pb_rubicon", "1.20");
            bids.put("hb_size", "300x250");
            bids.put("hb_size_rubicon", "300x250");
            verify(mockListener).onDemandReady(bids, uuid);
        } else {
            assertTrue("Server failed to start, unable to test.", false);
        }
    }

    @Test
    public void testSuccessfulBidResponseWithoutCacheId3() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.invalidBidResponseTopBidNoCacheId()));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setPrebidServerAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
            String uuid = UUID.randomUUID().toString();
            adapter.requestDemand(requestParams, mockListener, uuid);
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onDemandFailed(ResultCode.NO_BIDS, uuid);
        } else {
            assertTrue("Server failed to start, unable to test.", false);
        }
    }

    @Test
    public void testMergingBidsFromDifferentSeats() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexusOneBidFromRubicon()));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setPrebidServerAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
            String uuid = UUID.randomUUID().toString();
            adapter.requestDemand(requestParams, mockListener, uuid);
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            HashMap<String, String> bids = new HashMap<String, String>();
            bids.put("hb_bidder", "rubicon");
            bids.put("hb_bidder_rubicon", "rubicon");
            bids.put("hb_cache_id", "bd8d6eeb-8ad1-402c-a1f8-09565bb0bda7");
            bids.put("hb_cache_id_rubicon", "bd8d6eeb-8ad1-402c-a1f8-09565bb0bda7");
            bids.put("hb_creative_loadtype", "html");
            bids.put("hb_env", "mobile-app");
            bids.put("hb_env_rubicon", "mobile-app");
            bids.put("hb_pb", "1.20");
            bids.put("hb_pb_rubicon", "1.20");
            bids.put("hb_size", "300x250");
            bids.put("hb_size_rubicon", "300x250");
            bids.put("hb_bidder_appnexus", "appnexus");
            bids.put("hb_cache_id_appnexus", "f5b7ff9f-4311-459d-a5ac-5d4d3d034e47");
            bids.put("hb_creative_loadtype", "html");
            bids.put("hb_env_appnexus", "mobile-app");
            bids.put("hb_pb_appnexus", "0.50");
            bids.put("hb_size_appnexus", "300x250");
            verify(mockListener).onDemandReady(bids, uuid);
        } else {
            assertTrue("Server failed to start, unable to test.", false);
        }
    }

    @Test
    public void testListenerMapping() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setPrebidServerAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener1 = mock(DemandAdapter.DemandAdapterListener.class);
            DemandAdapter.DemandAdapterListener mockListener2 = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(320, 50));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
            String uuid1 = UUID.randomUUID().toString();
            String uuid2 = UUID.randomUUID().toString();
            adapter.requestDemand(requestParams, mockListener1, uuid1);
            adapter.requestDemand(requestParams, mockListener2, uuid2);
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener1).onDemandFailed(ResultCode.NO_BIDS, uuid1);
            verify(mockListener1, never()).onDemandFailed(ResultCode.NO_BIDS, uuid2);
            verify(mockListener2).onDemandFailed(ResultCode.NO_BIDS, uuid2);
            verify(mockListener2, never()).onDemandFailed(ResultCode.NO_BIDS, uuid1);
        } else {
            assertTrue("Server failed to start, unable to test.", false);
        }
    }

    @Test
    public void testStopRequest() throws Exception {
        if (successfulMockServerStarted) {
            MockResponse response = new MockResponse();
            response.setBody(MockPrebidServerResponses.noBid());
            response.setBodyDelay(1, TimeUnit.SECONDS);
            server.enqueue(response);
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setPrebidServerAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            final DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            final PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(320, 50));
            final RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
            final String uuid = UUID.randomUUID().toString();
            adapter.requestDemand(requestParams, mockListener, uuid);
            bgScheduler.runOneTask();
            adapter.stopRequest(uuid);
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener, never()).onDemandFailed(ResultCode.NO_BIDS, uuid);
        } else {
            assertTrue("Server failed to start, unable to test.", false);
        }
    }

    @Test
    public void testAdUnitKeyValuesInPostData() throws Exception {
        if (!successfulMockServerStarted) {
            fail("Mock server failed to start, unable to test.");
        }
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                if (request.getPath().equals("/withKeywords")) {
                    String postData = request.getBody().readUtf8();
                    errorCollector.checkThat("Post data does not contain key values: " + postData, postData, containsString("value2,value1"));
                } else if (request.getPath().equals("/clearKeywords")) {
                    String postData = request.getBody().readUtf8();
                    errorCollector.checkThat("Post data should not contain key values: " + postData, postData, not(containsString("value2,value1")));
                }
                return new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid());
            }
        });
        HttpUrl httpUrl = server.url("/withKeywords");
        Host.CUSTOM.setHostUrl(httpUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("123456");
        BannerAdUnit adUnit = new BannerAdUnit("123456", 300, 250);
        adUnit.addUserKeyword("key1", "value1");
        adUnit.addUserKeyword("key1", "value2");
        adUnit.addUserKeyword("key2", "value1");
        adUnit.addUserKeyword("key2", "value2");
        adUnit.addUserKeyword("key3", "value1");
        adUnit.addUserKeyword("key3", "value2");
        adUnit.addUserKeyword("key4", "value1");
        adUnit.addUserKeyword("key4", "value2");
        adUnit.addUserKeyword("key5", "value1");
        adUnit.addUserKeyword("key5", "value2");
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, mockListener);
        DemandFetcher fetcher = (DemandFetcher) FieldUtils.readField(adUnit, "fetcher", true);
        PrebidMobile.setTimeoutMillis(Integer.MAX_VALUE);
        ShadowLooper fetcherLooper = shadowOf(fetcher.getHandler().getLooper());
        fetcherLooper.runOneTask();
        ShadowLooper demandLooper = shadowOf(fetcher.getDemandHandler().getLooper());
        demandLooper.runOneTask();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        Host.CUSTOM.setHostUrl(server.url("/clearKeywords").toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        adUnit.clearUserKeywords();
        OnCompleteListener mockListenerNoKV = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, mockListenerNoKV);
        fetcher = (DemandFetcher) FieldUtils.readField(adUnit, "fetcher", true);
        PrebidMobile.setTimeoutMillis(Integer.MAX_VALUE);
        fetcherLooper = shadowOf(fetcher.getHandler().getLooper());
        fetcherLooper.runOneTask();
        demandLooper = shadowOf(fetcher.getDemandHandler().getLooper());
        demandLooper.runOneTask();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verify(mockListener, times(1)).onComplete(ResultCode.NO_BIDS);
        verify(mockListenerNoKV, times(1)).onComplete(ResultCode.NO_BIDS);
    }

    @Test
    public void testTargetingParamsInPostData() throws Exception {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setPrebidServerAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            TargetingParams.setYearOfBirth(1989);
            TargetingParams.setGender(TargetingParams.GENDER.FEMALE);
            TargetingParams.setBundleName("org.prebid.mobile");
            TargetingParams.setDomain("prebid.org");
            TargetingParams.setStoreUrl("store://app");
            TargetingParams.setSubjectToGDPR(true);
            TargetingParams.setSubjectToCOPPA(true);
            TargetingParams.setGDPRConsentString("testGDPR");
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(320, 50));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
            String uuid = UUID.randomUUID().toString();
            adapter.requestDemand(requestParams, mockListener, uuid);
            @SuppressWarnings("unchecked")
            ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
            PrebidServerAdapter.ServerConnector connector = connectors.get(0);
            assertEquals(uuid, connector.getAuctionId());
            JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
            assertEquals(8, postData.length());
            assertTrue(postData.has("id"));
            assertTrue(postData.has("source"));
            assertTrue(postData.has("imp"));
            assertTrue(postData.has("device"));
            assertTrue(postData.has("app"));
            assertTrue(postData.has("user"));
            assertTrue(postData.has("regs"));
            assertTrue(postData.has("ext"));
            JSONObject imp = postData.getJSONArray("imp").getJSONObject(0);
            assertEquals("PrebidMobile", imp.getString("id"));
            assertEquals(1, imp.getInt("secure"));
            assertEquals(1, imp.getJSONObject("banner").getJSONArray("format").length());
            assertEquals(320, imp.getJSONObject("banner").getJSONArray("format").getJSONObject(0).getInt("w"));
            assertEquals(50, imp.getJSONObject("banner").getJSONArray("format").getJSONObject(0).getInt("h"));
            assertEquals(67890, imp.getJSONObject("ext").getJSONObject("prebid").getJSONObject("storedrequest").getInt("id"));
            JSONObject device = postData.getJSONObject("device");
            assertEquals(PrebidServerSettings.deviceMake, device.getString("make"));
            assertEquals(PrebidServerSettings.deviceModel, device.getString("model"));
            assertEquals(0, device.getInt("lmt"));
            assertEquals(PrebidServerSettings.os, device.getString("os"));
            assertEquals(Locale.getDefault().getLanguage(), device.getString("language"));
            assertEquals(String.valueOf(BaseSetup.testSDK), device.getString("osv"));
            assertEquals(320, device.getInt("w"));
            assertEquals(0, device.getInt("h"));
            assertEquals(1, device.getInt("pxratio"));
            assertEquals(2, device.getInt("connectiontype"));
            JSONObject app = postData.getJSONObject("app");
            assertEquals("org.prebid.mobile", app.getString("bundle"));
            assertEquals("prebid.org", app.getString("domain"));
            assertEquals("store://app", app.getString("storeurl"));
            assertEquals("12345", app.getJSONObject("publisher").getString("id"));
            assertEquals("prebid-mobile", app.getJSONObject("ext").getJSONObject("prebid").getString("source"));
            assertEquals(PrebidServerSettings.sdk_version, app.getJSONObject("ext").getJSONObject("prebid").getString("version"));
            JSONObject user = postData.getJSONObject("user");
            assertEquals(3, user.length());
            assertEquals(1989, user.getInt("yob"));
            assertEquals("F", user.getString("gender"));
            assertEquals("testGDPR", user.getJSONObject("ext").getString("consent"));
            JSONObject regs = postData.getJSONObject("regs");
            assertEquals(1, regs.getInt("coppa"));
            assertEquals(1, regs.getJSONObject("ext").getInt("gdpr"));
            JSONObject ext = postData.getJSONObject("ext");
            assertTrue(ext.getJSONObject("prebid").has("cache"));
            assertTrue(ext.getJSONObject("prebid").getJSONObject("cache").has("bids"));
            assertEquals(0, ext.getJSONObject("prebid").getJSONObject("cache").getJSONObject("bids").length());
            assertEquals("12345", ext.getJSONObject("prebid").getJSONObject("storedrequest").getString("id"));
            assertTrue(ext.getJSONObject("prebid").has("targeting"));
        } else {
            assertTrue("Server failed to start, unable to test.", false);
        }
    }

    @Test
    public void testRubiconDefaultError() {
        if (!successfulMockServerStarted) {
            fail("Server failed to start, unable to test.");
        }

        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.htmlUnreachableFromRubicon()));
        HttpUrl hostUrl = server.url("/");
        Host.CUSTOM.setHostUrl(hostUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setPrebidServerAccountId("12345");
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(320, 50));
        RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes);
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verify(mockListener).onDemandFailed(ResultCode.PREBID_SERVER_ERROR, uuid);

    }

    @Test
    public void testPostDataWithCOPPA() throws Exception {

        //given
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        TargetingParams.setSubjectToCOPPA(true);
        int coppa = 0;

        //when
        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null);
        try {
            coppa = postData.getJSONObject("regs").getInt("coppa");
        } catch (Exception ex) {
        }

        //then
        assertEquals(1, coppa);
    }

    @Test
    public void testPostDataWithoutCOPPA() throws Exception {

        //given
        TargetingParams.setSubjectToCOPPA(false);
        int coppa = 0;

        //when
        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null);
        try {
            coppa = postData.getJSONObject("regs").getInt("coppa");
        } catch (Exception ex) {
        }

        //then
        assertEquals(0, coppa);
    }

    @Test
    public void testPostDataWithStoredResponses() throws Exception {

        if (!successfulMockServerStarted) {
            fail("Server failed to start, unable to test.");
        }

        //given
        PrebidMobile.setStoredAuctionResponse("111122223333");
        PrebidMobile.addStoredBidResponse("appnexus", "221144");
        PrebidMobile.addStoredBidResponse("rubicon", "221155");

        //when
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(500, 700));

        RequestParams requestParams = new RequestParams("67890", AdType.INTERSTITIAL, sizes);
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        @SuppressWarnings("unchecked")
        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
        PrebidServerAdapter.ServerConnector connector = connectors.get(0);

        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
        JSONObject prebid = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("prebid");
        JSONObject storedauctionresponse = prebid.getJSONObject("storedauctionresponse");
        JSONArray storedbidresponse = prebid.getJSONArray("storedbidresponse");
        JSONObject storedbidresponse1 = storedbidresponse.getJSONObject(0);
        JSONObject storedbidresponse2 = storedbidresponse.getJSONObject(1);

        //then
        assertEquals("111122223333", storedauctionresponse.getString("id"));

        Assert.assertEquals(2, storedbidresponse.length());
        assertEquals("appnexus", storedbidresponse1.getString("bidder"));
        assertEquals("221144", storedbidresponse1.getString("id"));
        assertEquals("rubicon", storedbidresponse2.getString("bidder"));
        assertEquals("221155", storedbidresponse2.getString("id"));

    }

    @Test
    public void testPostDataWithAdvancedInterstitial() throws Exception {

        //given
        AdSize minSizePerc = new AdSize(50, 70);

        JSONObject extInterstitial = null;
        JSONObject banner = null;
        int instl = 0;

        //when
        JSONObject postData = getPostDataHelper(AdType.INTERSTITIAL, null, null, minSizePerc);

        try {
            extInterstitial = postData.getJSONObject("device").getJSONObject("ext").getJSONObject("prebid").getJSONObject("interstitial");
        } catch (Exception ex) {
            fail("extInterstitial parsing fail");
        }

        try {
            banner = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("banner").getJSONArray("format").getJSONObject(0);
        } catch (Exception ex) {
            fail("banner parsing fail");
        }

        try {
            instl = postData.getJSONArray("imp").getJSONObject(0).getInt("instl");
        } catch (Exception ex) {
            fail("instl parsing fail");
        }

        //then
        Assert.assertNotNull(extInterstitial);
        assertTrue(extInterstitial.getInt("minwidthperc") == 50 && extInterstitial.getInt("minheightperc") == 70);

        Assert.assertNotNull(banner);
        assertTrue(banner.has("w"));
        assertTrue(banner.has("h"));

        assertEquals(1, instl);
    }

    @Test
    public void testPostDataWithoutAdvancedInterstitial() throws Exception {

        //given
        JSONObject extInterstitial = null;
        int instl = 0;

        //when
        JSONObject postData = getPostDataHelper(AdType.INTERSTITIAL, null, null, null);

        try {
            instl = postData.getJSONArray("imp").getJSONObject(0).getInt("instl");
        } catch (Exception ex) {
        }

        try {
            extInterstitial = postData.getJSONObject("device").getJSONObject("ext").getJSONObject("prebid").getJSONObject("interstitial");
        } catch (Exception ex) {
        }

        //then
        assertEquals(1, instl);
        assertNull(extInterstitial);
    }

    @Test
    public void testPostDataBannerWithAdvancedInterstitial() throws Exception {

        //given
        AdSize minSizePerc = new AdSize(50, 70);
        JSONObject extInterstitial = null;
        int instl = 0;

        //when
        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, minSizePerc);

        try {
            extInterstitial = postData.getJSONObject("device").getJSONObject("ext").getJSONObject("prebid").getJSONObject("interstitial");
        } catch (Exception ex) {
        }

        try {
            instl = postData.getJSONArray("imp").getJSONObject(0).getInt("instl");
        } catch (Exception ex) {
        }

        //then
        assertNull(extInterstitial);
        assertEquals(0, instl);
    }

    @Test
    public void testPostDataWithGlobalUserKeyword() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        //given
        TargetingParams.addUserKeyword("value10");

        //when
        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null);

        String keywords = null;
        try {
            keywords = postData.getJSONObject("user").optString("keywords", null);
        } catch (Exception ex) {
            fail("keywords parsing fail");
        }

        //then
        assertNotNull(keywords);
        assertEquals("value10", keywords);
    }

    @Test
    public void testPostDataWithGlobalContextKeyword() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        //given
        TargetingParams.addContextKeyword("value10");

        //when
        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null);

        String keywords = null;
        try {
            keywords = postData.getJSONObject("app").optString("keywords", null);
        } catch (Exception ex) {
            fail("keywords parsing fail");
        }

        //then
        assertNotNull(keywords);
        assertEquals("value10", keywords);

    }

    @Test
    public void testPostDataWithAccessControlList() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, JSONException {

        //given
        TargetingParams.addBidderToAccessControlList(TargetingParams.BIDDER_NAME_RUBICON_PROJECT);

        //when
        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null);

        JSONArray biddersJsonArray = null;
        try {
            biddersJsonArray = postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("data").getJSONArray("bidders");
        } catch (Exception ex) {
            fail("keywords parsing fail");
        }

        //then
        assertNotNull(biddersJsonArray);
        assertEquals(1, biddersJsonArray.length());
        assertEquals(TargetingParams.BIDDER_NAME_RUBICON_PROJECT, biddersJsonArray.get(0).toString());

    }

    @Test
    public void testPostDataWithGlobalUserData() throws Exception {

        //given
        TargetingParams.addUserData("key1", "value10");
        TargetingParams.addUserData("key2", "value20");
        TargetingParams.addUserData("key2", "value21");

        //when
        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null);

        JSONObject dataJsonObject = null;
        try {
            dataJsonObject = postData.getJSONObject("user").getJSONObject("ext").getJSONObject("data");
        } catch (Exception ex) {
            fail("keywords parsing fail");
        }

        List<String> listKey1 = Utils.getList(dataJsonObject.getJSONArray("key1"), new Utils.ParseCallable<String>() {
            @Override
            public String call(JSONArray jsonArray, int index) throws JSONException {
                return jsonArray.getString(index);
            }
        });

        List<String> listKey2 = Utils.getList(dataJsonObject.getJSONArray("key2"), new Utils.ParseCallable<String>() {
            @Override
            public String call(JSONArray jsonArray, int index) throws JSONException {
                return jsonArray.getString(index);
            }
        });

        //then
        assertNotNull(dataJsonObject);
        assertEquals(2, dataJsonObject.length());

        assertEquals(listKey1.size(), 1);
        assertThat(listKey1, containsInAnyOrder("value10"));

        assertEquals(listKey2.size(), 2);
        assertThat(listKey2, containsInAnyOrder("value20", "value21"));
    }

    @Test
    public void testPostDataWithGlobalContextData() throws Exception {

        //given
        TargetingParams.addContextData("key1", "value10");
        TargetingParams.addContextData("key2", "value20");
        TargetingParams.addContextData("key2", "value21");

        //when
        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null);

        JSONObject dataJsonObject = null;
        try {
            dataJsonObject = postData.getJSONObject("app").getJSONObject("ext").getJSONObject("data");
        } catch (Exception ex) {
            fail("keywords parsing fail");
        }

        List<String> listKey1 = Utils.getList(dataJsonObject.getJSONArray("key1"), new Utils.ParseCallable<String>() {
            @Override
            public String call(JSONArray jsonArray, int index) throws JSONException {
                return jsonArray.getString(index);
            }
        });

        List<String> listKey2 = Utils.getList(dataJsonObject.getJSONArray("key2"), new Utils.ParseCallable<String>() {
            @Override
            public String call(JSONArray jsonArray, int index) throws JSONException {
                return jsonArray.getString(index);
            }
        });

        //then
        assertNotNull(dataJsonObject);
        assertEquals(2, dataJsonObject.length());

        assertEquals(listKey1.size(), 1);
        assertThat(listKey1, containsInAnyOrder("value10"));

        assertEquals(listKey2.size(), 2);
        assertThat(listKey2, containsInAnyOrder("value20", "value21"));
    }

    @Test
    public void testPostDataWithAdunitContextKeyword() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        //given
        HashSet<String> contextKeywordsSet = new HashSet<>(1);
        contextKeywordsSet.add("value10");

        //when
        JSONObject postData = getPostDataHelper(AdType.BANNER, null, contextKeywordsSet, null);

        String keywords = null;

        try {
            keywords = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("context").getString("keywords");
        } catch (Exception ex) {
            fail("keywords parsing fail");
        }

        //then
        assertNotNull(keywords);
        assertEquals("value10", keywords);
    }

    @Test
    public void testPostDataWithAdunitContextData() throws Exception {

        //given
        Map<String, Set<String>> contextDataDictionary = new HashMap<>(2);
        contextDataDictionary.put("key1", new HashSet<>(Arrays.asList("value10")));
        contextDataDictionary.put("key2", new HashSet<>(Arrays.asList("value20", "value21")));

        //when
        JSONObject postData = getPostDataHelper(AdType.BANNER, contextDataDictionary, null, null);

        JSONObject dataJsonObject = null;
        try {
            dataJsonObject = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("context").getJSONObject("data");
        } catch (Exception ex) {
            fail("keywords parsing fail");
        }

        List<String> listKey1 = Utils.getList(dataJsonObject.getJSONArray("key1"), new Utils.ParseCallable<String>() {
            @Override
            public String call(JSONArray jsonArray, int index) throws JSONException {
                return jsonArray.getString(index);
            }
        });

        List<String> listKey2 = Utils.getList(dataJsonObject.getJSONArray("key2"), new Utils.ParseCallable<String>() {
            @Override
            public String call(JSONArray jsonArray, int index) throws JSONException {
                return jsonArray.getString(index);
            }
        });

        //then
        assertNotNull(dataJsonObject);
        assertEquals(2, dataJsonObject.length());

        assertEquals(listKey1.size(), 1);
        assertThat(listKey1, containsInAnyOrder("value10"));

        assertEquals(listKey2.size(), 2);
        assertThat(listKey2, containsInAnyOrder("value20", "value21"));

    }

    private JSONObject getPostDataHelper(AdType adType, @Nullable Map<String, Set<String>> contextDataDictionary, @Nullable Set<String> contextKeywordsSet, @Nullable AdSize minSizePerc) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (!successfulMockServerStarted) {
            fail("Server failed to start, unable to test.");
        }

        PrebidMobile.setApplicationContext(activity.getApplicationContext());

        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));

        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(300, 250));

        RequestParams requestParams = new RequestParams("67890", adType, sizes, contextDataDictionary, contextKeywordsSet, minSizePerc);
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        @SuppressWarnings("unchecked")
        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
        PrebidServerAdapter.ServerConnector connector = connectors.get(0);

        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
        return postData;

    }
}
