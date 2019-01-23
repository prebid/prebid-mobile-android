package org.prebid.mobile;


import android.os.Bundle;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.mopub.mobileads.MoPubView;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.prebid.mobile.testutils.BaseSetup;
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
            DemandFetcher.timeoutMillis = 1;
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
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.TIME_OUT);
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
            PrebidMobile.setHost(Host.CUSTOM);
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
            PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
            PublisherAdRequest request = builder.build();
            DemandFetcher demandFetcher = new DemandFetcher(request);
            demandFetcher.enableTestMode();
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
    public void testDestroyAutoRefresh() throws Exception{
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setHost(Host.CUSTOM);
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
            PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
            PublisherAdRequest request = builder.build();
            DemandFetcher demandFetcher = new DemandFetcher(request);
            demandFetcher.enableTestMode();
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
            PrebidMobile.setHost(Host.CUSTOM);
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{\n" +
                    "  \"id\": \"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\n" +
                    "  \"seatbid\": [\n" +
                    "    {\n" +
                    "      \"bid\": [\n" +
                    "        {\n" +
                    "          \"id\": \"1644265211331914430\",\n" +
                    "          \"impid\": \"Banner_300x250\",\n" +
                    "          \"price\": 0.5,\n" +
                    "          \"adm\": \"<script src=\\\"hello world\\\">this is an mock ad</script>\",\n" +
                    "          \"adid\": \"113276871\",\n" +
                    "          \"adomain\": [\n" +
                    "            \"appnexus.com\"\n" +
                    "          ],\n" +
                    "          \"iurl\": \"https://nym1-ib.adnxs.com/cr?id=113276871\",\n" +
                    "          \"cid\": \"9325\",\n" +
                    "          \"crid\": \"113276871\",\n" +
                    "          \"w\": 300,\n" +
                    "          \"h\": 250,\n" +
                    "          \"ext\": {\n" +
                    "            \"prebid\": {\n" +
                    "              \"targeting\": {\n" +
                    "                \"hb_bidder\": \"appnexus\",\n" +
                    "                \"hb_bidder_appnexus\": \"appnexus\",\n" +
                    "                \"hb_cache_id\": \"df4aba04-5e69-44b8-8608-058ab21600b8\",\n" +
                    "                \"hb_cache_id_appnexus\": \"df4aba04-5e69-44b8-8608-058ab21600b8\",\n" +
                    "                \"hb_creative_loadtype\": \"html\",\n" +
                    "                \"hb_env\": \"mobile-app\",\n" +
                    "                \"hb_env_appnexus\": \"mobile-app\",\n" +
                    "                \"hb_pb\": \"0.50\",\n" +
                    "                \"hb_pb_appnexus\": \"0.50\",\n" +
                    "                \"hb_size\": \"300x250\",\n" +
                    "                \"hb_size_appnexus\": \"300x250\"\n" +
                    "              },\n" +
                    "              \"type\": \"banner\"\n" +
                    "            },\n" +
                    "            \"bidder\": {\n" +
                    "              \"appnexus\": {\n" +
                    "                \"brand_id\": 1,\n" +
                    "                \"auction_id\": 7888349588523321000,\n" +
                    "                \"bidder_id\": 2,\n" +
                    "                \"bid_ad_type\": 0\n" +
                    "              }\n" +
                    "            }\n" +
                    "          }\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"seat\": \"appnexus\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"ext\": {\n" +
                    "    \"responsetimemillis\": {\n" +
                    "      \"appnexus\": 213\n" +
                    "    }\n" +
                    "  }\n" +
                    "}"));
            PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
            PublisherAdRequest request = builder.build();
            DemandFetcher demandFetcher = new DemandFetcher(request);
            demandFetcher.enableTestMode();
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
            assertEquals(11, bundle.size());
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
            assertTrue(bundle.containsKey("hb_creative_loadtype"));
            assertEquals("html", bundle.get("hb_creative_loadtype"));
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
    public void testSingleRequestOneBidResponseForMoPubAdObject() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setHost(Host.CUSTOM);
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{\n" +
                    "  \"id\": \"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\n" +
                    "  \"seatbid\": [\n" +
                    "    {\n" +
                    "      \"bid\": [\n" +
                    "        {\n" +
                    "          \"id\": \"1644265211331914430\",\n" +
                    "          \"impid\": \"Banner_300x250\",\n" +
                    "          \"price\": 0.5,\n" +
                    "          \"adm\": \"<script src=\\\"hello world\\\">this is an mock ad</script>\",\n" +
                    "          \"adid\": \"113276871\",\n" +
                    "          \"adomain\": [\n" +
                    "            \"appnexus.com\"\n" +
                    "          ],\n" +
                    "          \"iurl\": \"https://nym1-ib.adnxs.com/cr?id=113276871\",\n" +
                    "          \"cid\": \"9325\",\n" +
                    "          \"crid\": \"113276871\",\n" +
                    "          \"w\": 300,\n" +
                    "          \"h\": 250,\n" +
                    "          \"ext\": {\n" +
                    "            \"prebid\": {\n" +
                    "              \"targeting\": {\n" +
                    "                \"hb_bidder\": \"appnexus\",\n" +
                    "                \"hb_bidder_appnexus\": \"appnexus\",\n" +
                    "                \"hb_cache_id\": \"df4aba04-5e69-44b8-8608-058ab21600b8\",\n" +
                    "                \"hb_cache_id_appnexus\": \"df4aba04-5e69-44b8-8608-058ab21600b8\",\n" +
                    "                \"hb_creative_loadtype\": \"html\",\n" +
                    "                \"hb_env\": \"mobile-app\",\n" +
                    "                \"hb_env_appnexus\": \"mobile-app\",\n" +
                    "                \"hb_pb\": \"0.50\",\n" +
                    "                \"hb_pb_appnexus\": \"0.50\",\n" +
                    "                \"hb_size\": \"300x250\",\n" +
                    "                \"hb_size_appnexus\": \"300x250\"\n" +
                    "              },\n" +
                    "              \"type\": \"banner\"\n" +
                    "            },\n" +
                    "            \"bidder\": {\n" +
                    "              \"appnexus\": {\n" +
                    "                \"brand_id\": 1,\n" +
                    "                \"auction_id\": 7888349588523321000,\n" +
                    "                \"bidder_id\": 2,\n" +
                    "                \"bid_ad_type\": 0\n" +
                    "              }\n" +
                    "            }\n" +
                    "          }\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"seat\": \"appnexus\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"ext\": {\n" +
                    "    \"responsetimemillis\": {\n" +
                    "      \"appnexus\": 213\n" +
                    "    }\n" +
                    "  }\n" +
                    "}"));
            MoPubView adView = new MoPubView(activity);
            adView.setAdUnitId("123456789");
            DemandFetcher demandFetcher = new DemandFetcher(adView);
            demandFetcher.enableTestMode();
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
            assertEquals("hb_pb:0.50,hb_env:mobile-app,hb_pb_appnexus:0.50,hb_size:300x250,hb_bidder_appnexus:appnexus,hb_bidder:appnexus,hb_cache_id:df4aba04-5e69-44b8-8608-058ab21600b8,hb_env_appnexus:mobile-app,hb_creative_loadtype:html,hb_size_appnexus:300x250,hb_cache_id_appnexus:df4aba04-5e69-44b8-8608-058ab21600b8,", adViewKeywords);
        } else {
            assertTrue("Mock server was not started", false);
        }
    }

    @Test
    public void testAutoRefreshForMoPubAdObject() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setHost(Host.CUSTOM);
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{\n" +
                    "  \"id\": \"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\n" +
                    "  \"seatbid\": [\n" +
                    "    {\n" +
                    "      \"bid\": [\n" +
                    "        {\n" +
                    "          \"id\": \"1644265211331914430\",\n" +
                    "          \"impid\": \"Banner_300x250\",\n" +
                    "          \"price\": 0.5,\n" +
                    "          \"adm\": \"<script src=\\\"hello world\\\">this is an mock ad</script>\",\n" +
                    "          \"adid\": \"113276871\",\n" +
                    "          \"adomain\": [\n" +
                    "            \"appnexus.com\"\n" +
                    "          ],\n" +
                    "          \"iurl\": \"https://nym1-ib.adnxs.com/cr?id=113276871\",\n" +
                    "          \"cid\": \"9325\",\n" +
                    "          \"crid\": \"113276871\",\n" +
                    "          \"w\": 300,\n" +
                    "          \"h\": 250,\n" +
                    "          \"ext\": {\n" +
                    "            \"prebid\": {\n" +
                    "              \"targeting\": {\n" +
                    "                \"hb_bidder\": \"appnexus\",\n" +
                    "                \"hb_bidder_appnexus\": \"appnexus\",\n" +
                    "                \"hb_cache_id\": \"df4aba04-5e69-44b8-8608-058ab21600b8\",\n" +
                    "                \"hb_cache_id_appnexus\": \"df4aba04-5e69-44b8-8608-058ab21600b8\",\n" +
                    "                \"hb_creative_loadtype\": \"html\",\n" +
                    "                \"hb_env\": \"mobile-app\",\n" +
                    "                \"hb_env_appnexus\": \"mobile-app\",\n" +
                    "                \"hb_pb\": \"0.50\",\n" +
                    "                \"hb_pb_appnexus\": \"0.50\",\n" +
                    "                \"hb_size\": \"300x250\",\n" +
                    "                \"hb_size_appnexus\": \"300x250\"\n" +
                    "              },\n" +
                    "              \"type\": \"banner\"\n" +
                    "            },\n" +
                    "            \"bidder\": {\n" +
                    "              \"appnexus\": {\n" +
                    "                \"brand_id\": 1,\n" +
                    "                \"auction_id\": 7888349588523321000,\n" +
                    "                \"bidder_id\": 2,\n" +
                    "                \"bid_ad_type\": 0\n" +
                    "              }\n" +
                    "            }\n" +
                    "          }\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"seat\": \"appnexus\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"ext\": {\n" +
                    "    \"responsetimemillis\": {\n" +
                    "      \"appnexus\": 213\n" +
                    "    }\n" +
                    "  }\n" +
                    "}"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
            MoPubView adView = new MoPubView(activity);
            adView.setAdUnitId("123456789");
            DemandFetcher demandFetcher = new DemandFetcher(adView);
            demandFetcher.enableTestMode();
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
            assertEquals("hb_pb:0.50,hb_env:mobile-app,hb_pb_appnexus:0.50,hb_size:300x250,hb_bidder_appnexus:appnexus,hb_bidder:appnexus,hb_cache_id:df4aba04-5e69-44b8-8608-058ab21600b8,hb_env_appnexus:mobile-app,hb_creative_loadtype:html,hb_size_appnexus:300x250,hb_cache_id_appnexus:df4aba04-5e69-44b8-8608-058ab21600b8,", adViewKeywords);
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
            PrebidMobile.setHost(Host.CUSTOM);
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{\n" +
                    "  \"id\": \"3dc76667-a500-4e01-a43b-368e36d6c7cc\",\n" +
                    "  \"seatbid\": [\n" +
                    "    {\n" +
                    "      \"bid\": [\n" +
                    "        {\n" +
                    "          \"id\": \"1644265211331914430\",\n" +
                    "          \"impid\": \"Banner_300x250\",\n" +
                    "          \"price\": 0.5,\n" +
                    "          \"adm\": \"<script src=\\\"hello world\\\">this is an mock ad</script>\",\n" +
                    "          \"adid\": \"113276871\",\n" +
                    "          \"adomain\": [\n" +
                    "            \"appnexus.com\"\n" +
                    "          ],\n" +
                    "          \"iurl\": \"https://nym1-ib.adnxs.com/cr?id=113276871\",\n" +
                    "          \"cid\": \"9325\",\n" +
                    "          \"crid\": \"113276871\",\n" +
                    "          \"w\": 300,\n" +
                    "          \"h\": 250,\n" +
                    "          \"ext\": {\n" +
                    "            \"prebid\": {\n" +
                    "              \"targeting\": {\n" +
                    "                \"hb_bidder\": \"appnexus\",\n" +
                    "                \"hb_bidder_appnexus\": \"appnexus\",\n" +
                    "                \"hb_cache_id\": \"df4aba04-5e69-44b8-8608-058ab21600b8\",\n" +
                    "                \"hb_cache_id_appnexus\": \"df4aba04-5e69-44b8-8608-058ab21600b8\",\n" +
                    "                \"hb_creative_loadtype\": \"html\",\n" +
                    "                \"hb_env\": \"mobile-app\",\n" +
                    "                \"hb_env_appnexus\": \"mobile-app\",\n" +
                    "                \"hb_pb\": \"0.50\",\n" +
                    "                \"hb_pb_appnexus\": \"0.50\",\n" +
                    "                \"hb_size\": \"300x250\",\n" +
                    "                \"hb_size_appnexus\": \"300x250\"\n" +
                    "              },\n" +
                    "              \"type\": \"banner\"\n" +
                    "            },\n" +
                    "            \"bidder\": {\n" +
                    "              \"appnexus\": {\n" +
                    "                \"brand_id\": 1,\n" +
                    "                \"auction_id\": 7888349588523321000,\n" +
                    "                \"bidder_id\": 2,\n" +
                    "                \"bid_ad_type\": 0\n" +
                    "              }\n" +
                    "            }\n" +
                    "          }\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"seat\": \"appnexus\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"ext\": {\n" +
                    "    \"responsetimemillis\": {\n" +
                    "      \"appnexus\": 213\n" +
                    "    }\n" +
                    "  }\n" +
                    "}"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
            PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
            PublisherAdRequest request = builder.build();
            DemandFetcher demandFetcher = new DemandFetcher(request);
            demandFetcher.enableTestMode();
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
            assertEquals(11, bundle.size());
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
            assertTrue(bundle.containsKey("hb_creative_loadtype"));
            assertEquals("html", bundle.get("hb_creative_loadtype"));
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
