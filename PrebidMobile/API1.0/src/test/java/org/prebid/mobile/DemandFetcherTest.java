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


import android.os.Bundle;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.mopub.mobileads.MoPubView;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.prebid.mobile.testutils.BaseSetup;
import org.prebid.mobile.testutils.MockPrebidServerResponses;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.util.ArrayList;
import java.util.HashSet;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class DemandFetcherTest extends BaseSetup {

    @Test
    public void testBaseConditions() throws Exception {
        if (successfulMockServerStarted) {
            PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
            PublisherAdRequest request = builder.build();
            DemandFetcher demandFetcher = new DemandFetcher(request);
            demandFetcher.setPeriodMillis(0);
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            ArrayList<String> keywords = new ArrayList<>();
            RequestParams requestParams = new RequestParams("12345", AdType.BANNER, sizes, keywords);
            demandFetcher.setRequestParams(requestParams);
            assertEquals(DemandFetcher.STATE.STOPPED, FieldUtils.readField(demandFetcher, "state", true));
            demandFetcher.start();
            assertEquals(DemandFetcher.STATE.RUNNING, FieldUtils.readField(demandFetcher, "state", true));
            ShadowLooper fetcherLooper = Shadows.shadowOf(demandFetcher.getHandler().getLooper());
            fetcherLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            demandFetcher.destroy();
            assertEquals(DemandFetcher.STATE.DESTROYED, FieldUtils.readField(demandFetcher, "state", true));
        } else {
            assertTrue("Mock server was not started", false);
        }
    }

    @Test
    public void testSingleRequestNoBidsResponse() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
            PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
            PublisherAdRequest request = builder.build();
            DemandFetcher demandFetcher = new DemandFetcher(request);
            PrebidMobile.timeoutMillis = Integer.MAX_VALUE;
            demandFetcher.setPeriodMillis(0);
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            ArrayList<String> keywords = new ArrayList<>();
            RequestParams requestParams = new RequestParams("12345", AdType.BANNER, sizes, keywords);
            demandFetcher.setRequestParams(requestParams);
            OnCompleteListener mockListener = mock(OnCompleteListener.class);
            demandFetcher.setListener(mockListener);
            assertEquals(DemandFetcher.STATE.STOPPED, FieldUtils.readField(demandFetcher, "state", true));
            demandFetcher.start();
            assertEquals(DemandFetcher.STATE.RUNNING, FieldUtils.readField(demandFetcher, "state", true));
            ShadowLooper fetcherLooper = Shadows.shadowOf(demandFetcher.getHandler().getLooper());
            fetcherLooper.runOneTask();
            ShadowLooper demandLooper = Shadows.shadowOf(demandFetcher.getDemandHandler().getLooper());
            demandLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.NO_BIDS);
            assertEquals(DemandFetcher.STATE.DESTROYED, FieldUtils.readField(demandFetcher, "state", true));
        } else {
            assertTrue("Mock server was not started", false);
        }
    }

    @Test
    public void testDestroyAutoRefresh() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));

            PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
            PublisherAdRequest request = builder.build();
            DemandFetcher demandFetcher = new DemandFetcher(request);
            PrebidMobile.timeoutMillis = Integer.MAX_VALUE;
            demandFetcher.setPeriodMillis(30);
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            ArrayList<String> keywords = new ArrayList<>();
            RequestParams requestParams = new RequestParams("12345", AdType.BANNER, sizes, keywords);
            demandFetcher.setRequestParams(requestParams);
            OnCompleteListener mockListener = mock(OnCompleteListener.class);
            demandFetcher.setListener(mockListener);
            assertEquals(DemandFetcher.STATE.STOPPED, FieldUtils.readField(demandFetcher, "state", true));
            demandFetcher.start();
            assertEquals(DemandFetcher.STATE.RUNNING, FieldUtils.readField(demandFetcher, "state", true));
            ShadowLooper fetcherLooper = Shadows.shadowOf(demandFetcher.getHandler().getLooper());
            fetcherLooper.runOneTask();
            ShadowLooper demandLooper = Shadows.shadowOf(demandFetcher.getDemandHandler().getLooper());
            demandLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            assertEquals(DemandFetcher.STATE.RUNNING, FieldUtils.readField(demandFetcher, "state", true));
            demandFetcher.destroy();
            assertTrue(!Robolectric.getForegroundThreadScheduler().areAnyRunnable());
            assertTrue(!Robolectric.getBackgroundThreadScheduler().areAnyRunnable());
            assertEquals(DemandFetcher.STATE.DESTROYED, FieldUtils.readField(demandFetcher, "state", true));
            verify(mockListener, Mockito.times(1)).onComplete(ResultCode.NO_BIDS);
        } else {
            assertTrue("Mock server was not started", false);
        }
    }

    @Test
    public void testSingleRequestOneBidResponseForDFPAdObject() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus()));
            PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
            PublisherAdRequest request = builder.build();
            DemandFetcher demandFetcher = new DemandFetcher(request);
            PrebidMobile.timeoutMillis = Integer.MAX_VALUE;
            demandFetcher.setPeriodMillis(0);
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            ArrayList<String> keywords = new ArrayList<>();
            RequestParams requestParams = new RequestParams("12345", AdType.BANNER, sizes, keywords);
            demandFetcher.setRequestParams(requestParams);
            OnCompleteListener mockListener = mock(OnCompleteListener.class);
            demandFetcher.setListener(mockListener);
            assertEquals(DemandFetcher.STATE.STOPPED, FieldUtils.readField(demandFetcher, "state", true));
            demandFetcher.start();
            assertEquals(DemandFetcher.STATE.RUNNING, FieldUtils.readField(demandFetcher, "state", true));
            ShadowLooper fetcherLooper = Shadows.shadowOf(demandFetcher.getHandler().getLooper());
            fetcherLooper.runOneTask();
            ShadowLooper demandLooper = Shadows.shadowOf(demandFetcher.getDemandHandler().getLooper());
            demandLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.SUCCESS);
            assertEquals(DemandFetcher.STATE.DESTROYED, FieldUtils.readField(demandFetcher, "state", true));
            Bundle bundle = request.getCustomTargeting();
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
            assertTrue("Mock server was not started", false);
        }
    }

    @Test
    public void testSingleRequestOneBidRubiconResponseForDFPAdObject() throws Exception {
        if (!successfulMockServerStarted) {
            fail("Mock server was not started");
        }

        HttpUrl httpUrl = server.url("/");
        Host.CUSTOM.setHostUrl(httpUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromRubicon()));
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        PublisherAdRequest request = builder.build();
        DemandFetcher demandFetcher = new DemandFetcher(request);
        PrebidMobile.timeoutMillis = Integer.MAX_VALUE;
        demandFetcher.setPeriodMillis(0);
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(300, 250));
        ArrayList<String> keywords = new ArrayList<>();
        RequestParams requestParams = new RequestParams("12345", AdType.BANNER, sizes, keywords);
        demandFetcher.setRequestParams(requestParams);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        demandFetcher.setListener(mockListener);
        assertEquals(DemandFetcher.STATE.STOPPED, FieldUtils.readField(demandFetcher, "state", true));
        demandFetcher.start();
        assertEquals(DemandFetcher.STATE.RUNNING, FieldUtils.readField(demandFetcher, "state", true));
        ShadowLooper fetcherLooper = Shadows.shadowOf(demandFetcher.getHandler().getLooper());
        fetcherLooper.runOneTask();
        ShadowLooper demandLooper = Shadows.shadowOf(demandFetcher.getDemandHandler().getLooper());
        demandLooper.runOneTask();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verify(mockListener).onComplete(ResultCode.SUCCESS);
        assertEquals(DemandFetcher.STATE.DESTROYED, FieldUtils.readField(demandFetcher, "state", true));

        Bundle bundle = request.getCustomTargeting();
        Assert.assertEquals(16, bundle.size());
        String hb_pb = "hb_pb";
        Assert.assertTrue(bundle.containsKey(hb_pb));
        Assert.assertEquals("1.20", bundle.get(hb_pb));
        String hb_pb_rubicon = "hb_pb_rubicon";
        Assert.assertTrue(bundle.containsKey(hb_pb_rubicon));
        Assert.assertEquals("1.20", bundle.get(hb_pb_rubicon));
        String hb_bidder = "hb_bidder";
        Assert.assertTrue(bundle.containsKey(hb_bidder));
        Assert.assertEquals("rubicon", bundle.get(hb_bidder));
        String hb_bidder_rubicon = "hb_bidder_rubicon";
        Assert.assertTrue(bundle.containsKey(hb_bidder_rubicon));
        Assert.assertEquals("rubicon", bundle.get(hb_bidder_rubicon));
        String hb_cache_id = "hb_cache_id";
        Assert.assertTrue(bundle.containsKey(hb_cache_id));
        Assert.assertEquals("a2f41588-4727-425c-9ef0-3b382debef1e", bundle.get(hb_cache_id));
        String hb_cache_id_rubicon = "hb_cache_id_rubicon";
        Assert.assertTrue(bundle.containsKey(hb_cache_id_rubicon));
        Assert.assertEquals("a2f41588-4727-425c-9ef0-3b382debef1e", bundle.get(hb_cache_id_rubicon));
        String hb_env = "hb_env";
        Assert.assertTrue(bundle.containsKey(hb_env));
        Assert.assertEquals("mobile-app", bundle.get(hb_env));
        String hb_env_rubicon = "hb_env_rubicon";
        Assert.assertTrue(bundle.containsKey(hb_env_rubicon));
        Assert.assertEquals("mobile-app", bundle.get(hb_env_rubicon));
        String hb_size = "hb_size";
        Assert.assertTrue(bundle.containsKey(hb_size));
        Assert.assertEquals("300x250", bundle.get(hb_size));
        String hb_size_rubicon = "hb_size_rubicon";
        Assert.assertTrue(bundle.containsKey(hb_size_rubicon));
        Assert.assertEquals("300x250", bundle.get(hb_size_rubicon));

        String hb_cache_hostpath = "hb_cache_hostpath";
        Assert.assertTrue(bundle.containsKey(hb_cache_hostpath));
        Assert.assertEquals("https://prebid-cache-europe.rubiconproject.com/cache", bundle.get(hb_cache_hostpath));
        String hb_cache_hostpath_rubicon = "hb_cache_hostpath_rubicon";
        Assert.assertTrue(bundle.containsKey(hb_cache_hostpath_rubicon));
        Assert.assertEquals("https://prebid-cache-europe.rubiconproject.com/cache", bundle.get(hb_cache_hostpath_rubicon));
        String hb_cache_path = "hb_cache_path";
        Assert.assertTrue(bundle.containsKey(hb_cache_path));
        Assert.assertEquals("/cache", bundle.get(hb_cache_path));
        String hb_cache_path_rubicon = "hb_cache_path_rubicon";
        Assert.assertTrue(bundle.containsKey(hb_cache_path_rubicon));
        Assert.assertEquals("/cache", bundle.get(hb_cache_path_rubicon));
        String hb_cache_host = "hb_cache_host";
        Assert.assertTrue(bundle.containsKey(hb_cache_host));
        Assert.assertEquals("prebid-cache-europe.rubiconproject.com", bundle.get(hb_cache_host));
        String hb_cache_host_rubicon = "hb_cache_host_rubicon";
        Assert.assertTrue(bundle.containsKey(hb_cache_host_rubicon));
        Assert.assertEquals("prebid-cache-europe.rubiconproject.com", bundle.get(hb_cache_host_rubicon));
    }

    @Test
    public void testSingleRequestOneBidResponseForMoPubAdObject() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus()));
            MoPubView adView = new MoPubView(activity);
            adView.setAdUnitId("123456789");
            DemandFetcher demandFetcher = new DemandFetcher(adView);
            PrebidMobile.timeoutMillis = Integer.MAX_VALUE;
            demandFetcher.setPeriodMillis(0);
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            ArrayList<String> keywords = new ArrayList<>();
            RequestParams requestParams = new RequestParams("12345", AdType.BANNER, sizes, keywords);
            demandFetcher.setRequestParams(requestParams);
            OnCompleteListener mockListener = mock(OnCompleteListener.class);
            demandFetcher.setListener(mockListener);
            assertEquals(DemandFetcher.STATE.STOPPED, FieldUtils.readField(demandFetcher, "state", true));
            demandFetcher.start();
            assertEquals(DemandFetcher.STATE.RUNNING, FieldUtils.readField(demandFetcher, "state", true));
            ShadowLooper fetcherLooper = Shadows.shadowOf(demandFetcher.getHandler().getLooper());
            fetcherLooper.runOneTask();
            ShadowLooper demandLooper = Shadows.shadowOf(demandFetcher.getDemandHandler().getLooper());
            demandLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.SUCCESS);
            assertEquals(DemandFetcher.STATE.DESTROYED, FieldUtils.readField(demandFetcher, "state", true));
            String adViewKeywords = adView.getKeywords();
            assertEquals("hb_pb:0.50,hb_env:mobile-app,hb_pb_appnexus:0.50,hb_size:300x250,hb_bidder_appnexus:appnexus,hb_bidder:appnexus,hb_cache_id:df4aba04-5e69-44b8-8608-058ab21600b8,hb_env_appnexus:mobile-app,hb_size_appnexus:300x250,hb_cache_id_appnexus:df4aba04-5e69-44b8-8608-058ab21600b8,", adViewKeywords);
        } else {
            assertTrue("Mock server was not started", false);
        }
    }

    @Test
    public void testSingleRequestOneBidRubiconResponseForMoPubAdObject() throws Exception {
        if (!successfulMockServerStarted) {
            fail("Mock server was not started");
        }

        HttpUrl httpUrl = server.url("/");
        Host.CUSTOM.setHostUrl(httpUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromRubicon()));
        MoPubView adView = new MoPubView(activity);
        adView.setAdUnitId("123456789");
        DemandFetcher demandFetcher = new DemandFetcher(adView);
        PrebidMobile.timeoutMillis = Integer.MAX_VALUE;
        demandFetcher.setPeriodMillis(0);
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(300, 250));
        ArrayList<String> keywords = new ArrayList<>();
        RequestParams requestParams = new RequestParams("12345", AdType.BANNER, sizes, keywords);
        demandFetcher.setRequestParams(requestParams);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        demandFetcher.setListener(mockListener);
        assertEquals(DemandFetcher.STATE.STOPPED, FieldUtils.readField(demandFetcher, "state", true));
        demandFetcher.start();
        assertEquals(DemandFetcher.STATE.RUNNING, FieldUtils.readField(demandFetcher, "state", true));
        ShadowLooper fetcherLooper = Shadows.shadowOf(demandFetcher.getHandler().getLooper());
        fetcherLooper.runOneTask();
        ShadowLooper demandLooper = Shadows.shadowOf(demandFetcher.getDemandHandler().getLooper());
        demandLooper.runOneTask();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        verify(mockListener).onComplete(ResultCode.SUCCESS);
        assertEquals(DemandFetcher.STATE.DESTROYED, FieldUtils.readField(demandFetcher, "state", true));
        String adViewKeywords = adView.getKeywords();
        assertEquals("hb_env:mobile-app,hb_cache_hostpath:https://prebid-cache-europe.rubiconproject.com/cache,hb_size_rubicon:300x250,hb_cache_id:a2f41588-4727-425c-9ef0-3b382debef1e,hb_cache_path_rubicon:/cache,hb_cache_host_rubicon:prebid-cache-europe.rubiconproject.com,hb_pb:1.20,hb_pb_rubicon:1.20,hb_cache_id_rubicon:a2f41588-4727-425c-9ef0-3b382debef1e,hb_cache_path:/cache,hb_size:300x250,hb_cache_hostpath_rubicon:https://prebid-cache-europe.rubiconproject.com/cache,hb_env_rubicon:mobile-app,hb_bidder:rubicon,hb_bidder_rubicon:rubicon,hb_cache_host:prebid-cache-europe.rubiconproject.com,", adViewKeywords);
    }

    @Test
    public void testAutoRefreshForMoPubAdObject() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus()));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
            MoPubView adView = new MoPubView(activity);
            adView.setAdUnitId("123456789");
            DemandFetcher demandFetcher = new DemandFetcher(adView);
            PrebidMobile.timeoutMillis = Integer.MAX_VALUE;
            demandFetcher.setPeriodMillis(2000);
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            ArrayList<String> keywords = new ArrayList<>();
            RequestParams requestParams = new RequestParams("12345", AdType.BANNER, sizes, keywords);
            demandFetcher.setRequestParams(requestParams);
            OnCompleteListener mockListener = mock(OnCompleteListener.class);
            demandFetcher.setListener(mockListener);
            assertEquals(DemandFetcher.STATE.STOPPED, FieldUtils.readField(demandFetcher, "state", true));
            demandFetcher.start();
            assertEquals(DemandFetcher.STATE.RUNNING, FieldUtils.readField(demandFetcher, "state", true));
            ShadowLooper fetcherLooper = Shadows.shadowOf(demandFetcher.getHandler().getLooper());
            fetcherLooper.runOneTask();
            ShadowLooper demandLooper = Shadows.shadowOf(demandFetcher.getDemandHandler().getLooper());
            demandLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.SUCCESS);
            assertNotSame(DemandFetcher.STATE.DESTROYED, FieldUtils.readField(demandFetcher, "state", true));
            String adViewKeywords = adView.getKeywords();
            assertEquals("hb_pb:0.50,hb_env:mobile-app,hb_pb_appnexus:0.50,hb_size:300x250,hb_bidder_appnexus:appnexus,hb_bidder:appnexus,hb_cache_id:df4aba04-5e69-44b8-8608-058ab21600b8,hb_env_appnexus:mobile-app,hb_size_appnexus:300x250,hb_cache_id_appnexus:df4aba04-5e69-44b8-8608-058ab21600b8,", adViewKeywords);
            fetcherLooper.runOneTask();
            demandLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.NO_BIDS);
            assertNotSame(DemandFetcher.STATE.DESTROYED, FieldUtils.readField(demandFetcher, "state", true));
            adViewKeywords = adView.getKeywords();
            assertEquals("", adViewKeywords);
        } else {
            assertTrue("Mock server was not started", false);
        }
    }

    @Test
    public void testAutoRefreshForDFPAdObject() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus()));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
            PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
            PublisherAdRequest request = builder.build();
            DemandFetcher demandFetcher = new DemandFetcher(request);
            PrebidMobile.timeoutMillis = Integer.MAX_VALUE;
            demandFetcher.setPeriodMillis(2000);
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            ArrayList<String> keywords = new ArrayList<>();
            RequestParams requestParams = new RequestParams("12345", AdType.BANNER, sizes, keywords);
            demandFetcher.setRequestParams(requestParams);
            OnCompleteListener mockListener = mock(OnCompleteListener.class);
            demandFetcher.setListener(mockListener);
            assertEquals(DemandFetcher.STATE.STOPPED, FieldUtils.readField(demandFetcher, "state", true));
            demandFetcher.start();
            assertEquals(DemandFetcher.STATE.RUNNING, FieldUtils.readField(demandFetcher, "state", true));
            ShadowLooper fetcherLooper = Shadows.shadowOf(demandFetcher.getHandler().getLooper());
            fetcherLooper.runOneTask();
            ShadowLooper demandLooper = Shadows.shadowOf(demandFetcher.getDemandHandler().getLooper());
            demandLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.SUCCESS);
            assertEquals(DemandFetcher.STATE.RUNNING, FieldUtils.readField(demandFetcher, "state", true));
            Bundle bundle = request.getCustomTargeting();
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
            fetcherLooper.runOneTask();
            demandLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.NO_BIDS);
            assertEquals(DemandFetcher.STATE.RUNNING, FieldUtils.readField(demandFetcher, "state", true));
            bundle = request.getCustomTargeting();
            assertEquals(0, bundle.size());
        } else {
            assertTrue("Mock server was not started", false);
        }
    }
}
