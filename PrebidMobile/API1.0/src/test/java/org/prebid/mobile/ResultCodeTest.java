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

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.mopub.mobileads.MoPubView;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.prebid.mobile.testutils.MockPrebidServerResponses;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowNetworkInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class ResultCodeTest extends BaseSetup {
    @Test
    public void testInvalidContext() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setPrebidServerAccountId("123456");
            PrebidMobile.setApplicationContext(null);
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus()));
            InterstitialAdUnit adUnit = new InterstitialAdUnit("123456");
            MoPubView testView = new MoPubView(activity);
            OnCompleteListener mockListener = mock(OnCompleteListener.class);
            adUnit.fetchDemand(testView, mockListener);
            verify(mockListener).onComplete(ResultCode.INVALID_CONTEXT);
        } else {
            assertTrue("Mock server not started", false);
        }
    }

    @Test
    public void testSuccessForMoPub() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            PrebidMobile.setPrebidServerAccountId("123456");
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus()));
            BannerAdUnit adUnit = new BannerAdUnit("123456", 300, 250);
            MoPubView testView = new MoPubView(activity);
            OnCompleteListener mockListener = mock(OnCompleteListener.class);
            adUnit.fetchDemand(testView, mockListener);
            DemandFetcher fetcher = (DemandFetcher) FieldUtils.readField(adUnit, "fetcher", true);
            PrebidMobile.timeoutMillis = Integer.MAX_VALUE;
            ShadowLooper fetcherLooper = shadowOf(fetcher.getHandler().getLooper());
            fetcherLooper.runOneTask();
            ShadowLooper demandLooper = shadowOf(fetcher.getDemandHandler().getLooper());
            demandLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.SUCCESS);
            assertEquals("hb_pb:0.50,hb_env:mobile-app,hb_pb_appnexus:0.50,hb_size:300x250,hb_bidder_appnexus:appnexus,hb_bidder:appnexus,hb_cache_id:df4aba04-5e69-44b8-8608-058ab21600b8,hb_env_appnexus:mobile-app,hb_size_appnexus:300x250,hb_cache_id_appnexus:df4aba04-5e69-44b8-8608-058ab21600b8,", testView.getKeywords());
        } else {
            assertTrue("Mock server not started", false);
        }
    }

    @Test
    public void testSuccessForDFP() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            PrebidMobile.setPrebidServerAccountId("123456");
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus()));
            BannerAdUnit adUnit = new BannerAdUnit("123456", 300, 250);
            PublisherAdRequest testRequest = new PublisherAdRequest.Builder().build();
            OnCompleteListener mockListener = mock(OnCompleteListener.class);
            adUnit.fetchDemand(testRequest, mockListener);
            DemandFetcher fetcher = (DemandFetcher) FieldUtils.readField(adUnit, "fetcher", true);
            PrebidMobile.timeoutMillis = Integer.MAX_VALUE;
            ShadowLooper fetcherLooper = shadowOf(fetcher.getHandler().getLooper());
            fetcherLooper.runOneTask();
            ShadowLooper demandLooper = shadowOf(fetcher.getDemandHandler().getLooper());
            demandLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.SUCCESS);
            Bundle bundle = testRequest.getCustomTargeting();
            assertEquals(10, bundle.size());
            assertTrue(bundle.containsKey("hb_pb"));
            assertEquals("0.50", bundle.get("hb_pb"));
            assertTrue(bundle.containsKey("hb_bidder"));
            assertEquals("appnexus", bundle.get("hb_bidder"));
            assertTrue(bundle.containsKey("hb_bidder_appnexus"));
            assertEquals("appnexus", bundle.get("hb_bidder_appnexus"));
            assertTrue(bundle.containsKey("hb_cache_id"));
            assertEquals("df4aba04-5e69-44b8-8608-058ab21600b8", bundle.get("hb_cache_id"));
            assertTrue(bundle.containsKey("hb_cache_id_appnexus"));
            assertEquals("df4aba04-5e69-44b8-8608-058ab21600b8", bundle.get("hb_cache_id_appnexus"));
            assertTrue(bundle.containsKey("hb_env"));
            assertEquals("mobile-app", bundle.get("hb_env"));
            assertTrue(bundle.containsKey("hb_env_appnexus"));
            assertEquals("mobile-app", bundle.get("hb_env_appnexus"));
            assertTrue(bundle.containsKey("hb_pb_appnexus"));
            assertEquals("0.50", bundle.get("hb_pb_appnexus"));
            assertTrue(bundle.containsKey("hb_size"));
            assertEquals("300x250", bundle.get("hb_size"));
            assertTrue(bundle.containsKey("hb_size_appnexus"));
            assertEquals("300x250", bundle.get("hb_size_appnexus"));
        } else {
            assertTrue("Mock server not started", false);
        }
    }

    @Test
    public void testSuccessRubiconForDFP() throws Exception {
        if (!successfulMockServerStarted) {
            fail("Mock server not started");
            return;
        }

        HttpUrl httpUrl = server.url("/");
        Host.CUSTOM.setHostUrl(httpUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("123456");
        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromRubicon()));
        BannerAdUnit adUnit = new BannerAdUnit("123456", 300, 250);
        PublisherAdRequest testRequest = new PublisherAdRequest.Builder().build();
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testRequest, mockListener);
        DemandFetcher fetcher = (DemandFetcher) FieldUtils.readField(adUnit, "fetcher", true);
        PrebidMobile.timeoutMillis = Integer.MAX_VALUE;
        ShadowLooper fetcherLooper = shadowOf(fetcher.getHandler().getLooper());
        fetcherLooper.runOneTask();
        ShadowLooper demandLooper = shadowOf(fetcher.getDemandHandler().getLooper());
        demandLooper.runOneTask();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        verify(mockListener).onComplete(ResultCode.SUCCESS);

        Bundle bundle = testRequest.getCustomTargeting();
        assertEquals(16, bundle.size());
        String hb_pb = "hb_pb";
        assertTrue(bundle.containsKey(hb_pb));
        assertEquals("1.20", bundle.get(hb_pb));
        String hb_pb_rubicon = "hb_pb_rubicon";
        assertTrue(bundle.containsKey(hb_pb_rubicon));
        assertEquals("1.20", bundle.get(hb_pb_rubicon));
        String hb_bidder = "hb_bidder";
        assertTrue(bundle.containsKey(hb_bidder));
        assertEquals("rubicon", bundle.get(hb_bidder));
        String hb_bidder_rubicon = "hb_bidder_rubicon";
        assertTrue(bundle.containsKey(hb_bidder_rubicon));
        assertEquals("rubicon", bundle.get(hb_bidder_rubicon));
        String hb_cache_id = "hb_cache_id";
        assertTrue(bundle.containsKey(hb_cache_id));
        assertEquals("a2f41588-4727-425c-9ef0-3b382debef1e", bundle.get(hb_cache_id));
        String hb_cache_id_rubicon = "hb_cache_id_rubicon";
        assertTrue(bundle.containsKey(hb_cache_id_rubicon));
        assertEquals("a2f41588-4727-425c-9ef0-3b382debef1e", bundle.get(hb_cache_id_rubicon));
        String hb_env = "hb_env";
        assertTrue(bundle.containsKey(hb_env));
        assertEquals("mobile-app", bundle.get(hb_env));
        String hb_env_rubicon = "hb_env_rubicon";
        assertTrue(bundle.containsKey(hb_env_rubicon));
        assertEquals("mobile-app", bundle.get(hb_env_rubicon));
        String hb_size = "hb_size";
        assertTrue(bundle.containsKey(hb_size));
        assertEquals("300x250", bundle.get(hb_size));
        String hb_size_rubicon = "hb_size_rubicon";
        assertTrue(bundle.containsKey(hb_size_rubicon));
        assertEquals("300x250", bundle.get(hb_size_rubicon));

        String hb_cache_hostpath = "hb_cache_hostpath";
        assertTrue(bundle.containsKey(hb_cache_hostpath));
        assertEquals("https://prebid-cache-europe.rubiconproject.com/cache", bundle.get(hb_cache_hostpath));
        String hb_cache_hostpath_rubicon = "hb_cache_hostpath_rubicon";
        assertTrue(bundle.containsKey(hb_cache_hostpath_rubicon));
        assertEquals("https://prebid-cache-europe.rubiconproject.com/cache", bundle.get(hb_cache_hostpath_rubicon));
        String hb_cache_path = "hb_cache_path";
        assertTrue(bundle.containsKey(hb_cache_path));
        assertEquals("/cache", bundle.get(hb_cache_path));
        String hb_cache_path_rubicon = "hb_cache_path_rubicon";
        assertTrue(bundle.containsKey(hb_cache_path_rubicon));
        assertEquals("/cache", bundle.get(hb_cache_path_rubicon));
        String hb_cache_host_rubicon = "hb_cache_host_rubicon";
        assertTrue(bundle.containsKey(hb_cache_host_rubicon));
        assertEquals("prebid-cache-europe.rubiconproject.com", bundle.get(hb_cache_host_rubicon));
        String hb_cache_host = "hb_cache_host";
        assertTrue(bundle.containsKey(hb_cache_host));
        assertEquals("prebid-cache-europe.rubiconproject.com", bundle.get(hb_cache_host));
    }

    @Test
    public void testNetworkError() {
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("123456");
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        ShadowNetworkInfo shadowOfActiveNetworkInfo = shadowOf(connectivityManager.getActiveNetworkInfo());
        shadowOfActiveNetworkInfo.setConnectionStatus(false);
        BannerAdUnit adUnit = new BannerAdUnit("123456", 300, 250);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, mockListener);
        verify(mockListener).onComplete(ResultCode.NETWORK_ERROR);
    }

    @Test
    public void testTimeOut() throws Exception {
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.timeoutMillis = 30;
        PrebidMobile.setPrebidServerAccountId("b7adad2c-e042-4126-8ca1-b3caac7d3e5c");
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(0, 250));
        RequestParams requestParams = new RequestParams("e2edc23f-0b3b-4203-81b5-7cc97132f418", AdType.BANNER, sizes, new ArrayList<String>());
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);

        @SuppressWarnings("unchecked")
        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
        PrebidServerAdapter.ServerConnector.TimeoutCountDownTimer timeoutCountDownTimer = (PrebidServerAdapter.ServerConnector.TimeoutCountDownTimer) FieldUtils.readDeclaredField(connector, "timeoutCountDownTimer", true);
        shadowOf(timeoutCountDownTimer).invokeFinish();

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verify(mockListener).onDemandFailed(ResultCode.TIMEOUT, uuid);
    }

    @Test
    public void testNoBids() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            PrebidMobile.setPrebidServerAccountId("123456");
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
            BannerAdUnit adUnit = new BannerAdUnit("123456", 300, 250);
            MoPubView testView = new MoPubView(activity);
            OnCompleteListener mockListener = mock(OnCompleteListener.class);
            adUnit.fetchDemand(testView, mockListener);
            DemandFetcher fetcher = (DemandFetcher) FieldUtils.readField(adUnit, "fetcher", true);
            PrebidMobile.timeoutMillis = Integer.MAX_VALUE;
            ShadowLooper fetcherLooper = shadowOf(fetcher.getHandler().getLooper());
            fetcherLooper.runOneTask();
            ShadowLooper demandLooper = shadowOf(fetcher.getDemandHandler().getLooper());
            demandLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.NO_BIDS);
            assertEquals(null, testView.getKeywords());
        } else {
            assertTrue("Mock server not started", false);
        }
    }

    @Test
    public void testNoBidsRubicon() throws Exception {
        if (!successfulMockServerStarted) {
            fail("Mock server not started");
        }

        HttpUrl httpUrl = server.url("/");
        Host.CUSTOM.setHostUrl(httpUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("123456");
        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBidFromRubicon()));
        BannerAdUnit adUnit = new BannerAdUnit("123456", 300, 250);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, mockListener);
        DemandFetcher fetcher = (DemandFetcher) FieldUtils.readField(adUnit, "fetcher", true);
        PrebidMobile.timeoutMillis = Integer.MAX_VALUE;
        ShadowLooper fetcherLooper = shadowOf(fetcher.getHandler().getLooper());
        fetcherLooper.runOneTask();
        ShadowLooper demandLooper = shadowOf(fetcher.getDemandHandler().getLooper());
        demandLooper.runOneTask();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verify(mockListener).onComplete(ResultCode.NO_BIDS);

        assertNull(testView.getKeywords());
    }

    @Test
    public void testEmptyAccountId() throws Exception {
        PrebidMobile.setPrebidServerAccountId("");
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, mockListener);
        verify(mockListener).onComplete(ResultCode.INVALID_ACCOUNT_ID);
    }

    @Test
    public void testEmptyConfigId() throws Exception {
        PrebidMobile.setPrebidServerAccountId("123456");
        BannerAdUnit adUnit = new BannerAdUnit("", 320, 50);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, mockListener);
        verify(mockListener).onComplete(ResultCode.INVALID_CONFIG_ID);
    }

    @Test
    public void testEmptyHostUrl() throws Exception {
        PrebidMobile.setPrebidServerAccountId("123456");
        Host.CUSTOM.setHostUrl("");
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, mockListener);
        verify(mockListener).onComplete(ResultCode.INVALID_HOST_URL);
    }

    @Test
    public void testSupportMultipleSizesForDFPBanner() throws Exception {
        PrebidMobile.setPrebidServerAccountId("123456");
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        adUnit.addAdditionalSize(300, 250);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        adUnit.fetchDemand(builder.build(), mockListener);
        verify(mockListener, never()).onComplete(ResultCode.INVALID_SIZE);
    }

    @Test
    public void testNoNegativeSizeForBanner() {
        PrebidMobile.setPrebidServerAccountId("123456");
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        adUnit.addAdditionalSize(-1, 250);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, mockListener);
        verify(mockListener).onComplete(ResultCode.INVALID_SIZE);
    }

    @Test
    public void testInvalidSizeForBanner() {
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setPrebidServerAccountId("b7adad2c-e042-4126-8ca1-b3caac7d3e5c");
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(0, 250));
        RequestParams requestParams = new RequestParams("e2edc23f-0b3b-4203-81b5-7cc97132f418", AdType.BANNER, sizes, new ArrayList<String>());
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verify(mockListener).onDemandFailed(ResultCode.INVALID_SIZE, uuid);
    }

    @Test
    public void testInvalidAdObject() {
        PrebidMobile.setPrebidServerAccountId("123456");
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        BannerAdUnit adUnit = new BannerAdUnit("123456", 300, 250);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(new Object(), mockListener);
        verify(mockListener).onComplete(ResultCode.INVALID_AD_OBJECT);
    }
}
