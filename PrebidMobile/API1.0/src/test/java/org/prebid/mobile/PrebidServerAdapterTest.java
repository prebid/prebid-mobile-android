package org.prebid.mobile;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
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
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK, manifest = Config.NONE)
public class PrebidServerAdapterTest extends BaseSetup {
    // todo add more mocks of potential Prebid Server responses
    @Test
    public void testNoBidResponse() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
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
    public void testListenerMapping() {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
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
            response.setBody("{}");
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
            verify(mockListener, never()).onDemandFailed(ResultCode.NO_BIDS, uuid);
        } else {
            assertTrue("Server failed to start, unable to test.", false);
        }
    }

    @Test
    public void testTargetingParamsInPostData() throws Exception {
        if (successfulMockServerStarted) {
            server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
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
            assertEquals(10, device.length());
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
