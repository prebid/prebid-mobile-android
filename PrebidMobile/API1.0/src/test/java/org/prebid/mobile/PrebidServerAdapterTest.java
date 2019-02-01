package org.prebid.mobile;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.prebid.mobile.testutils.MockPrebidServerResponses;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK, manifest = Config.NONE)
public class PrebidServerAdapterTest extends BaseSetup {
    @Test
    public void testUpdateTimeoutMillis() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()).throttleBody(40, 100, TimeUnit.MILLISECONDS));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setHost(Host.CUSTOM);
            assertEquals(10000, PrebidMobile.timeoutMillis);
            assertFalse(PrebidMobile.timeoutMillisUpdated);
            PrebidMobile.setAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(320, 50));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes, new ArrayList<String>());
            String uuid = UUID.randomUUID().toString();
            adapter.requestDemand(requestParams, mockListener, uuid);
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onDemandFailed(ResultCode.NO_BIDS, uuid);
            assertTrue("Actual Prebid Mobile timeout is " + PrebidMobile.timeoutMillis, PrebidMobile.timeoutMillis < 3000 && PrebidMobile.timeoutMillis > 2000);
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
            PrebidMobile.setHost(Host.CUSTOM);
            PrebidMobile.setAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(320, 50));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes, new ArrayList<String>());
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
    public void testSuccessfulBidResponse() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus()));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setHost(Host.CUSTOM);
            PrebidMobile.setAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes, new ArrayList<String>());
            String uuid = UUID.randomUUID().toString();
            adapter.requestDemand(requestParams, mockListener, uuid);
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            HashMap<String, String> bids = new HashMap<String, String>();
            bids.put("hb_bidder", "appnexus");
            bids.put("hb_bidder_appnexus", "appnexus");
            bids.put("hb_cache_id", "df4aba04-5e69-44b8-8608-058ab21600b8");
            bids.put("hb_cache_id_appnexus", "df4aba04-5e69-44b8-8608-058ab21600b8");
            bids.put("hb_creative_loadtype", "html");
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
    public void testSuccessfulBidResponseWithoutCacheId() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.invalidBidResponseWithoutCacheId()));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setHost(Host.CUSTOM);
            PrebidMobile.setAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes, new ArrayList<String>());
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
    public void testSuccessfulBidResponseWithoutCacheId2() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.validResponseAppNexusNoCacheIdAndRubiconHasCacheId()));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setHost(Host.CUSTOM);
            PrebidMobile.setAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes, new ArrayList<String>());
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
            verify(mockListener).onDemandReady(bids, uuid);
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
            PrebidMobile.setHost(Host.CUSTOM);
            PrebidMobile.setAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(300, 250));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes, new ArrayList<String>());
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
            PrebidMobile.setHost(Host.CUSTOM);
            PrebidMobile.setAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            DemandAdapter.DemandAdapterListener mockListener1 = mock(DemandAdapter.DemandAdapterListener.class);
            DemandAdapter.DemandAdapterListener mockListener2 = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(320, 50));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes, new ArrayList<String>());
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
            PrebidMobile.setHost(Host.CUSTOM);
            PrebidMobile.setAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            final DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            final PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(320, 50));
            final RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes, new ArrayList<String>());
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
    public void testTargetingParamsInPostData() throws Exception {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
            HttpUrl hostUrl = server.url("/");
            Host.CUSTOM.setHostUrl(hostUrl.toString());
            PrebidMobile.setHost(Host.CUSTOM);
            PrebidMobile.setAccountId("12345");
            PrebidMobile.setShareGeoLocation(true);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            TargetingParams.setYearOfBirth(1989);
            TargetingParams.setGender(TargetingParams.GENDER.FEMALE);
            TargetingParams.setBundleName("org.prebid.mobile");
            TargetingParams.setDomain("prebid.org");
            TargetingParams.setStoreUrl("store://app");
            TargetingParams.setSubjectToGDPR(true);
            TargetingParams.setGDPRConsentString("testGDPR");
            DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
            PrebidServerAdapter adapter = new PrebidServerAdapter();
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(320, 50));
            RequestParams requestParams = new RequestParams("67890", AdType.BANNER, sizes, new ArrayList<String>());
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
            assertEquals(PrebidServerSettings.language, device.getString("language"));
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
}
