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

// TODO:Unification: Enable or delete
//@RunWith(RobolectricTestRunner.class)
//@Config(sdk = BaseSetup.testSDK, manifest = Config.NONE)
//public class PrebidServerAdapterTest extends BaseSetup {
//
//    @Rule
//    public ErrorCollector errorCollector = new ErrorCollector();
//
//    @Override
//    public void setup() {
//        super.setup();
//        ((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).startThread();
//    }
//
//    @Override
//    public void tearDown() {
//        super.tearDown();
//
//        ((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).shutdown();
//        TargetingParams.clearStoredExternalUserIds();
//        PrebidMobile.setExternalUserIds(null);
//
//        TargetingParams.clearAccessControlList();
//        TargetingParams.clearUserData();
//        TargetingParams.clearContextData();
//        TargetingParams.clearContextKeywords();
//        TargetingParams.clearUserKeywords();
//    }
//
//    @Test
//    public void testRegexMatch() {
//        Pattern p = Pattern.compile("^Invalid request: Stored Request with ID=\".*\" not found.");
//        String s = "Invalid request: Stored Request with ID=\"6ace8c7d-88c0-4623-8117-75bc3f0a2edd\" not found.";
//        Matcher m = p.matcher(s);
//        assertTrue(m.find());
//        Pattern p2 = Pattern.compile("^Invalid request: Stored Imp with ID=\".*\" not found.");
//        String s2 = "Invalid request: Stored Imp with ID=\"6ace8c7d-88c0-4623-8117-75bc3f0a2edd\" not found.";
//        Matcher m2 = p2.matcher(s2);
//        assertTrue(m2.find());
//        Pattern p3 = Pattern.compile("^Invalid request: Request imp\\[\\d\\].banner.format\\[\\d\\] must define non-zero \"h\" and \"w\" properties.");
//        String s3 = "Invalid request: Request imp[0].banner.format[0] must define non-zero \"h\" and \"w\" properties.";
//        Matcher m3 = p3.matcher(s3);
//        assertTrue(m3.find());
//        Pattern p4 = Pattern.compile("^Invalid request: Unable to set interstitial size list");
//        String s4 = "Invalid request: Unable to set interstitial size list for Imp id=PrebidMobile (No valid sizes between 0x10 and 1x25)";
//        Matcher m4 = p4.matcher(s4);
//        assertTrue(m4.find());
//
//    }
//
//    //    @Test
//    // TODO: Activate this test after setting up test server
//    public void testInvalidPrebidServerAccountIdForAppNexusHostedPrebidServer() {
//        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
//        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-ffffffffffff");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("6ace8c7d-88c0-4623-8117-75bc3f0a2e45");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener).onDemandFailed(ResultCode.INVALID_ACCOUNT_ID, uuid);
//    }
//
//    @Test
//    public void testInvalidPrebidServerAccountIdForRubiconHostedPrebidServer() {
//        server.enqueue(new MockResponse().setResponseCode(400).setBody(MockPrebidServerResponses.invalidAccountIdFromRubicon()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//
//        PrebidMobile.setPrebidServerAccountId("1001_INVALID_ACCOUNT_ID");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("1001-1");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//
//        verify(mockListener).onDemandFailed(ResultCode.INVALID_ACCOUNT_ID, uuid);
//    }
//
//    //    @Test
//    // TODO: Activate this test after setting up test server
//    public void testInvalidPrebidServerConfigIdForAppNexusHostedPrebidServer() {
//        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
//        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("6ace8c7d-88c0-4623-8117-ffffffffffff");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener).onDemandFailed(ResultCode.INVALID_CONFIG_ID, uuid);
//    }
//
//    @Test
//    public void testInvalidPrebidServerConfigIdForRubiconHostedPrebidServer() {
//        server.enqueue(new MockResponse().setResponseCode(400).setBody(MockPrebidServerResponses.invalidConfigIdFromRubicon()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//
//        PrebidMobile.setPrebidServerAccountId("1001");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("1001-1_INVALID_CONFIG_ID");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener).onDemandFailed(ResultCode.INVALID_CONFIG_ID, uuid);
//    }
//
//    //    @Test
//    // TODO: Activate this test after setting up test server
//    public void testInvalidPrebidServerIdSyntaxForAppNexusHostedPrebidServer() {
//        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
//        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888d"); // invalid account id
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("6ace8c7d-88c0-4623-8117-75bc3f0a2e45");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener).onDemandFailed(ResultCode.INVALID_ACCOUNT_ID, uuid);
//    }
//
//    //    @Test
//    // TODO: Activate this test after setting up test server
//    public void testInvalidPrebidServerIdSyntaxForAppNexusHostedPrebidServer2() {
//        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
//        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("6ace8c7d-88c0-4623-8117-75bc3f0a2e");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener).onDemandFailed(ResultCode.INVALID_CONFIG_ID, uuid);
//    }
//
//    //    @Test
//    // TODO: Activate this test after setting up test server
//    public void testUpdateTimeoutMillis() {
//        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
//        assertEquals(2000, PrebidMobile.getTimeoutMillis());
//        assertFalse(PrebidMobile.timeoutMillisUpdated);
//        PrebidMobile.setPrebidServerAccountId("b7adad2c-e042-4126-8ca1-b3caac7d3e5c");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("e2edc23f-0b3b-4203-81b5-7cc97132f418");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener).onDemandFailed(ResultCode.NO_BIDS, uuid);
//        assertTrue("Actual Prebid Mobile timeout is " + PrebidMobile.getTimeoutMillis(), PrebidMobile.getTimeoutMillis() <= 2000 && PrebidMobile.getTimeoutMillis() > 700);
//        assertTrue(PrebidMobile.timeoutMillisUpdated);
//    }
//
//    @Test
//    public void testUpdateTimeoutMillis2() {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBidResponseNoTmax()));
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBidResponseTmaxTooLarge()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        assertEquals("Actual Prebid Mobile timeout is " + PrebidMobile.getTimeoutMillis(), 2000, PrebidMobile.getTimeoutMillis());
//        assertTrue(!PrebidMobile.timeoutMillisUpdated);
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        assertEquals("Actual Prebid Mobile timeout is " + PrebidMobile.getTimeoutMillis(), 2000, PrebidMobile.getTimeoutMillis());
//        assertTrue(PrebidMobile.timeoutMillisUpdated);
//    }
//
//    @Test
//    public void testNoBidResponse() {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener).onDemandFailed(ResultCode.NO_BIDS, uuid);
//    }
//
//    @Test
//    public void testBlankResponseReturnsNoBid() {
//        server.enqueue(new MockResponse().setResponseCode(204).setBody(""));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener).onDemandFailed(ResultCode.NO_BIDS, uuid);
//    }
//
//    @Test
//    public void testNoBidRubiconResponse() {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBidFromRubicon()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener).onDemandFailed(ResultCode.NO_BIDS, uuid);
//
//    }
//
//    @Test
//    public void testSuccessfulBidResponse() {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        HashMap<String, String> bids = new HashMap<String, String>();
//        bids.put("hb_bidder", "appnexus");
//        bids.put("hb_bidder_appnexus", "appnexus");
//        bids.put("hb_cache_id", "df4aba04-5e69-44b8-8608-058ab21600b8");
//        bids.put("hb_cache_id_appnexus", "df4aba04-5e69-44b8-8608-058ab21600b8");
//        bids.put("hb_env", "mobile-app");
//        bids.put("hb_env_appnexus", "mobile-app");
//        bids.put("hb_pb", "0.50");
//        bids.put("hb_pb_appnexus", "0.50");
//        bids.put("hb_size", "300x250");
//        bids.put("hb_size_appnexus", "300x250");
//        verify(mockListener).onDemandReady(bids, uuid);
//    }
//
//    @Test
//    public void testSuccessfulBidRubiconResponse() {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromRubicon()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        HashMap<String, String> bids = new HashMap<String, String>();
//        bids.put("hb_bidder", "rubicon");
//        bids.put("hb_bidder_rubicon", "rubicon");
//        bids.put("hb_cache_id", "a2f41588-4727-425c-9ef0-3b382debef1e");
//        bids.put("hb_cache_id_rubicon", "a2f41588-4727-425c-9ef0-3b382debef1e");
//        bids.put("hb_env", "mobile-app");
//        bids.put("hb_env_rubicon", "mobile-app");
//        bids.put("hb_pb", "1.20");
//        bids.put("hb_pb_rubicon", "1.20");
//        bids.put("hb_size", "300x250");
//        bids.put("hb_size_rubicon", "300x250");
//
//        bids.put("hb_cache_hostpath", "https://prebid-cache-europe.rubiconproject.com/cache");
//        bids.put("hb_cache_hostpath_rubicon", "https://prebid-cache-europe.rubiconproject.com/cache");
//        bids.put("hb_cache_path", "/cache");
//        bids.put("hb_cache_path_rubicon", "/cache");
//        bids.put("hb_cache_host", "prebid-cache-europe.rubiconproject.com");
//        bids.put("hb_cache_host_rubicon", "prebid-cache-europe.rubiconproject.com");
//        verify(mockListener).onDemandReady(bids, uuid);
//
//    }
//
//    @Test
//    public void testSuccessfulBidResponseWithoutCacheId() {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.invalidBidResponseWithoutCacheId()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener).onDemandFailed(ResultCode.NO_BIDS, uuid);
//    }
//
//    @Test
//    public void testSuccessfulBidRubiconResponseWithoutCacheId() {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.invalidBidRubiconResponseWithoutCacheId()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener).onDemandFailed(ResultCode.NO_BIDS, uuid);
//    }
//
//    @Test
//    public void testSuccessfulBidResponseTwoBidsOnTheSameSeat() {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.validBidResponseTwoBidsOnTheSameSeat()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        HashMap<String, String> bids = new HashMap<String, String>();
//        bids.put("hb_bidder", "appnexus");
//        bids.put("hb_bidder_appnexus", "appnexus");
//        bids.put("hb_cache_id", "fdc4a3b1-ecdd-4c0a-b043-7ed66dca0553");
//        bids.put("hb_cache_id_appnexus", "fdc4a3b1-ecdd-4c0a-b043-7ed66dca0553");
//        bids.put("hb_env", "mobile-app");
//        bids.put("hb_env_appnexus", "mobile-app");
//        bids.put("hb_pb", "0.08");
//        bids.put("hb_pb_appnexus", "0.08");
//        bids.put("hb_size", "300x250");
//        bids.put("hb_size_appnexus", "300x250");
//        verify(mockListener).onDemandReady(bids, uuid);
//    }
//
//    @Test
//    public void testSuccessfulBidResponseWithoutCacheId2() {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.validResponseAppNexusNoCacheIdAndRubiconHasCacheId()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        HashMap<String, String> bids = new HashMap<String, String>();
//        bids.put("hb_bidder", "rubicon");
//        bids.put("hb_bidder_rubicon", "rubicon");
//        bids.put("hb_cache_id", "bd8d6eeb-8ad1-402c-a1f8-09565bb0bda7");
//        bids.put("hb_cache_id_rubicon", "bd8d6eeb-8ad1-402c-a1f8-09565bb0bda7");
//        bids.put("hb_env", "mobile-app");
//        bids.put("hb_env_rubicon", "mobile-app");
//        bids.put("hb_pb", "1.20");
//        bids.put("hb_pb_rubicon", "1.20");
//        bids.put("hb_size", "300x250");
//        bids.put("hb_size_rubicon", "300x250");
//        verify(mockListener).onDemandReady(bids, uuid);
//    }
//
//    @Test
//    public void testSuccessfulBidResponseWithoutCacheId3() {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.invalidBidResponseTopBidNoCacheId()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener).onDemandFailed(ResultCode.NO_BIDS, uuid);
//    }
//
//    @Test
//    public void testMergingBidsFromDifferentSeats() {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexusOneBidFromRubicon()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        HashMap<String, String> bids = new HashMap<String, String>();
//        bids.put("hb_bidder", "rubicon");
//        bids.put("hb_bidder_rubicon", "rubicon");
//        bids.put("hb_cache_id", "bd8d6eeb-8ad1-402c-a1f8-09565bb0bda7");
//        bids.put("hb_cache_id_rubicon", "bd8d6eeb-8ad1-402c-a1f8-09565bb0bda7");
//        bids.put("hb_creative_loadtype", "html");
//        bids.put("hb_env", "mobile-app");
//        bids.put("hb_env_rubicon", "mobile-app");
//        bids.put("hb_pb", "1.20");
//        bids.put("hb_pb_rubicon", "1.20");
//        bids.put("hb_size", "300x250");
//        bids.put("hb_size_rubicon", "300x250");
//        bids.put("hb_bidder_appnexus", "appnexus");
//        bids.put("hb_cache_id_appnexus", "f5b7ff9f-4311-459d-a5ac-5d4d3d034e47");
//        bids.put("hb_creative_loadtype", "html");
//        bids.put("hb_env_appnexus", "mobile-app");
//        bids.put("hb_pb_appnexus", "0.50");
//        bids.put("hb_size_appnexus", "300x250");
//        verify(mockListener).onDemandReady(bids, uuid);
//    }
//
//    @Test
//    public void testListenerMapping() {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener1 = mock(DemandAdapter.DemandAdapterListener.class);
//        DemandAdapter.DemandAdapterListener mockListener2 = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid1 = UUID.randomUUID().toString();
//        String uuid2 = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener1, uuid1);
//        adapter.requestDemand(configuration, mockListener2, uuid2);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener1).onDemandFailed(ResultCode.NO_BIDS, uuid1);
//        verify(mockListener1, never()).onDemandFailed(ResultCode.NO_BIDS, uuid2);
//        verify(mockListener2).onDemandFailed(ResultCode.NO_BIDS, uuid2);
//        verify(mockListener2, never()).onDemandFailed(ResultCode.NO_BIDS, uuid1);
//    }
//
//    @Test
//    public void testStopRequest() throws Exception {
//        MockResponse response = new MockResponse();
//        response.setBody(MockPrebidServerResponses.noBid());
//        response.setBodyDelay(1, TimeUnit.SECONDS);
//        server.enqueue(response);
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        final DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        final PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        final AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        final String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        bgScheduler.runOneTask();
//        adapter.stopRequest(uuid);
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener, never()).onDemandFailed(ResultCode.NO_BIDS, uuid);
//    }
//
//    @Test
//    public void testUserKeywordInPostData() throws Exception {
//        server.setDispatcher(new Dispatcher() {
//            @Override
//            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
//                if (request.getPath().equals("/withKeywords")) {
//                    String postData = request.getBody().readUtf8();
//                    errorCollector.checkThat("Post data does not contain key values: " + postData, postData, containsString("value2,value1"));
//                } else if (request.getPath().equals("/clearKeywords")) {
//                    String postData = request.getBody().readUtf8();
//                    errorCollector.checkThat("Post data should not contain key values: " + postData, postData, not(containsString("value2,value1")));
//                }
//                return new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid());
//            }
//        });
//        HttpUrl httpUrl = server.url("/withKeywords");
//        Host.CUSTOM.setHostUrl(httpUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        PrebidMobile.setPrebidServerAccountId("123456");
//        BannerAdUnit adUnit = new BannerAdUnit("123456", 300, 250);
//        TargetingParams.addUserKeyword("value1");
//        TargetingParams.addUserKeyword("value2");
//        TargetingParams.addUserKeyword("value1");
//        MoPubView testView = new MoPubView(activity);
//        OnCompleteListener mockListener = mock(OnCompleteListener.class);
//        adUnit.fetchDemand(testView, mockListener);
//        DemandFetcher fetcher = (DemandFetcher) FieldUtils.readField(adUnit, "fetcher", true);
//        PrebidMobile.setTimeoutMillis(Integer.MAX_VALUE);
//        ShadowLooper fetcherLooper = shadowOf(fetcher.getHandler().getLooper());
//        fetcherLooper.runOneTask();
//        ShadowLooper demandLooper = shadowOf(fetcher.getDemandHandler().getLooper());
//        demandLooper.runOneTask();
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        Host.CUSTOM.setHostUrl(server.url("/clearKeywords").toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        TargetingParams.clearUserKeywords();
//        OnCompleteListener mockListenerNoKV = mock(OnCompleteListener.class);
//        adUnit.fetchDemand(testView, mockListenerNoKV);
//        fetcher = (DemandFetcher) FieldUtils.readField(adUnit, "fetcher", true);
//        PrebidMobile.setTimeoutMillis(Integer.MAX_VALUE);
//        fetcherLooper = shadowOf(fetcher.getHandler().getLooper());
//        fetcherLooper.runOneTask();
//        demandLooper = shadowOf(fetcher.getDemandHandler().getLooper());
//        demandLooper.runOneTask();
//
//        bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener, times(1)).onComplete(ResultCode.NO_BIDS);
//        verify(mockListenerNoKV, times(1)).onComplete(ResultCode.NO_BIDS);
//    }
//
//    @Test
//    public void testTargetingParamsInPostData() throws Exception {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        TargetingParams.setYearOfBirth(1989);
//        TargetingParams.setGender(TargetingParams.GENDER.FEMALE);
//        TargetingParams.setBundleName("org.prebid.mobile");
//        TargetingParams.setDomain("prebid.org");
//        TargetingParams.setStoreUrl("store://app");
//        TargetingParams.setSubjectToGDPR(true);
//        TargetingParams.setSubjectToCOPPA(true);
//        TargetingParams.setGDPRConsentString("testGDPR");
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//        assertEquals(uuid, connector.getAuctionId());
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//        assertEquals(8, postData.length());
//        assertTrue(postData.has("id"));
//        assertTrue(postData.has("source"));
//        assertTrue(postData.has("imp"));
//        assertTrue(postData.has("device"));
//        assertTrue(postData.has("app"));
//        assertTrue(postData.has("user"));
//        assertTrue(postData.has("regs"));
//        assertTrue(postData.has("ext"));
//        JSONObject imp = postData.getJSONArray("imp").getJSONObject(0);
//        assertEquals("PrebidMobile", imp.getString("id"));
//        assertEquals(1, imp.getInt("secure"));
//        assertEquals(1, imp.getJSONObject("banner").getJSONArray("format").length());
//        assertEquals(320, imp.getJSONObject("banner").getJSONArray("format").getJSONObject(0).getInt("w"));
//        assertEquals(50, imp.getJSONObject("banner").getJSONArray("format").getJSONObject(0).getInt("h"));
//        assertEquals(67890, imp.getJSONObject("ext").getJSONObject("prebid").getJSONObject("storedrequest").getInt("id"));
//        JSONObject device = postData.getJSONObject("device");
//        assertEquals(PrebidServerSettings.deviceMake, device.getString("make"));
//        assertEquals(PrebidServerSettings.deviceModel, device.getString("model"));
//        assertEquals(0, device.getInt("lmt"));
//        assertEquals(PrebidServerSettings.os, device.getString("os"));
//        assertEquals(Locale.getDefault().getLanguage(), device.getString("language"));
//        assertEquals(String.valueOf(BaseSetup.testSDK), device.getString("osv"));
//        assertEquals(320, device.getInt("w"));
//        assertEquals(470, device.getInt("h"));
//        assertEquals(1, device.getInt("pxratio"));
//        assertEquals(2, device.getInt("connectiontype"));
//        JSONObject app = postData.getJSONObject("app");
//        assertEquals("org.prebid.mobile", app.getString("bundle"));
//        assertEquals("prebid.org", app.getString("domain"));
//        assertEquals("store://app", app.getString("storeurl"));
//        assertEquals("12345", app.getJSONObject("publisher").getString("id"));
//        assertEquals("prebid-mobile", app.getJSONObject("ext").getJSONObject("prebid").getString("source"));
//        assertEquals(PrebidServerSettings.sdk_version, app.getJSONObject("ext").getJSONObject("prebid").getString("version"));
//        JSONObject user = postData.getJSONObject("user");
//        assertEquals(3, user.length());
//        assertEquals(1989, user.getInt("yob"));
//        assertEquals("F", user.getString("gender"));
//        assertEquals("testGDPR", user.getJSONObject("ext").getString("consent"));
//        JSONObject regs = postData.getJSONObject("regs");
//        assertEquals(1, regs.getInt("coppa"));
//        assertEquals(1, regs.getJSONObject("ext").getInt("gdpr"));
//        JSONObject ext = postData.getJSONObject("ext");
//        assertTrue(ext.getJSONObject("prebid").has("cache"));
//        assertTrue(ext.getJSONObject("prebid").getJSONObject("cache").has("bids"));
//        assertEquals(0, ext.getJSONObject("prebid").getJSONObject("cache").getJSONObject("bids").length());
//        assertEquals("12345", ext.getJSONObject("prebid").getJSONObject("storedrequest").getString("id"));
//        assertTrue(ext.getJSONObject("prebid").has("targeting"));
//    }
//
//    @Test
//    public void testContentUrlInPostData() throws Exception {
//        String expectedContentUrl = "http://www.something.com/item";
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//
//        // set content somewhere here
//
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        ContentObject contentObject = new ContentObject();
//        contentObject.setUrl(expectedContentUrl);
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        configuration.setAppContent(contentObject);
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject app = postData.getJSONObject("app");
//        assertTrue(app.has("content"));
//        JSONObject content = app.getJSONObject("content");
//        assertTrue(content.has("url"));
//        assertEquals(expectedContentUrl, content.getString("url"));
//    }
//
//    @Test
//    public void testExternalUserIdsInPostData() throws Exception {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//
//        // User Id from External Third Party Sources
//        ArrayList<ExternalUserId> externalUserIdArray = new ArrayList<>();
//        externalUserIdArray.add(new ExternalUserId("adserver.org", "111111111111", null, new HashMap() {{
//            put("rtiPartner", "TDID");
//        }}));
//        externalUserIdArray.add(new ExternalUserId("netid.de", "999888777", null, null));
//        externalUserIdArray.add(new ExternalUserId("criteo.com", "_fl7bV96WjZsbiUyQnJlQ3g4ckh5a1N", null, null));
//        externalUserIdArray.add(new ExternalUserId("liveramp.com", "AjfowMv4ZHZQJFM8TpiUnYEyA81Vdgg", null, null));
//        externalUserIdArray.add(new ExternalUserId("sharedid.org", "111111111111", 1, new HashMap() {{
//            put("third", "01ERJWE5FS4RAZKG6SKQ3ZYSKV");
//        }}));
//        PrebidMobile.setExternalUserIds(externalUserIdArray);
//
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//        assertEquals(uuid, connector.getAuctionId());
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject user = postData.getJSONObject("user");
//        JSONObject ext = user.getJSONObject("ext");
//        JSONArray eids = ext.getJSONArray("eids");
//
//        JSONObject adserverObj = eids.getJSONObject(0);
//        assertEquals("adserver.org", adserverObj.get("source"));
//        assertEquals("111111111111", adserverObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//        assertEquals("TDID", adserverObj.getJSONArray("uids").getJSONObject(0).getJSONObject("ext").getString("rtiPartner"));
//
//        JSONObject netidObj = eids.getJSONObject(1);
//        assertEquals("netid.de", netidObj.get("source"));
//        assertEquals("999888777", netidObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//
//        JSONObject criteoObj = eids.getJSONObject(2);
//        assertEquals("criteo.com", criteoObj.get("source"));
//        assertEquals("_fl7bV96WjZsbiUyQnJlQ3g4ckh5a1N", criteoObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//
//        JSONObject liverampObj = eids.getJSONObject(3);
//        assertEquals("liveramp.com", liverampObj.get("source"));
//        assertEquals("AjfowMv4ZHZQJFM8TpiUnYEyA81Vdgg", liverampObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//
//        JSONObject sharedidObj = eids.getJSONObject(4);
//        assertEquals("sharedid.org", sharedidObj.get("source"));
//        assertEquals("111111111111", sharedidObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//        assertEquals("1", sharedidObj.getJSONArray("uids").getJSONObject(0).getString("atype"));
//        assertEquals("01ERJWE5FS4RAZKG6SKQ3ZYSKV", sharedidObj.getJSONArray("uids").getJSONObject(0).getJSONObject("ext").getString("third"));
//    }
//
//    @Test
//    public void testExternalUserIdsInPostDataWithEmptySource() throws Exception {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//
//        // User Id from External Third Party Sources
//        ArrayList<ExternalUserId> externalUserIdArray = new ArrayList<>();
//        externalUserIdArray.add(new ExternalUserId("", "999888777", null, null));
//        PrebidMobile.setExternalUserIds(externalUserIdArray);
//
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//        assertEquals(uuid, connector.getAuctionId());
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject user = postData.getJSONObject("user");
//        assertFalse(user.has("ext"));
//
//    }
//
//    @Test
//    public void testExternalUserIdsInPostDataWithEmptyIdentifier() throws Exception {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//
//        // User Id from External Third Party Sources
//        ArrayList<ExternalUserId> externalUserIdArray = new ArrayList<>();
//        externalUserIdArray.add(new ExternalUserId("netid.de", "", null, null));
//        PrebidMobile.setExternalUserIds(externalUserIdArray);
//
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//        assertEquals(uuid, connector.getAuctionId());
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject user = postData.getJSONObject("user");
//        assertFalse(user.has("ext"));
//
//    }
//
//    @Test
//    public void testStoredExternalUserIdsInPostData() throws Exception {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//
//        // User Id from External Third Party Sources
//        TargetingParams.storeExternalUserId(new ExternalUserId("adserver.org", "111111111111", null, new HashMap() {{
//            put("rtiPartner", "TDID");
//        }}));
//        TargetingParams.storeExternalUserId(new ExternalUserId("netid.de", "999888777", null, null));
//        TargetingParams.storeExternalUserId(new ExternalUserId("criteo.com", "_fl7bV96WjZsbiUyQnJlQ3g4ckh5a1N", null, null));
//        TargetingParams.storeExternalUserId(new ExternalUserId("liveramp.com", "AjfowMv4ZHZQJFM8TpiUnYEyA81Vdgg", null, null));
//        TargetingParams.storeExternalUserId(new ExternalUserId("sharedid.org", "111111111111", 1, new HashMap() {{
//            put("third", "01ERJWE5FS4RAZKG6SKQ3ZYSKV");
//        }}));
//
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//        assertEquals(uuid, connector.getAuctionId());
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject user = postData.getJSONObject("user");
//        JSONObject ext = user.getJSONObject("ext");
//        JSONArray eids = ext.getJSONArray("eids");
//
//        JSONObject adserverObj = eids.getJSONObject(0);
//        assertEquals("adserver.org", adserverObj.get("source"));
//        assertEquals("111111111111", adserverObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//        assertEquals("TDID", adserverObj.getJSONArray("uids").getJSONObject(0).getJSONObject("ext").getString("rtiPartner"));
//
//        JSONObject netidObj = eids.getJSONObject(1);
//        assertEquals("netid.de", netidObj.get("source"));
//        assertEquals("999888777", netidObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//
//        JSONObject criteoObj = eids.getJSONObject(2);
//        assertEquals("criteo.com", criteoObj.get("source"));
//        assertEquals("_fl7bV96WjZsbiUyQnJlQ3g4ckh5a1N", criteoObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//
//        JSONObject liverampObj = eids.getJSONObject(3);
//        assertEquals("liveramp.com", liverampObj.get("source"));
//        assertEquals("AjfowMv4ZHZQJFM8TpiUnYEyA81Vdgg", liverampObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//
//        JSONObject sharedidObj = eids.getJSONObject(4);
//        assertEquals("sharedid.org", sharedidObj.get("source"));
//        assertEquals("111111111111", sharedidObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//        assertEquals("1", sharedidObj.getJSONArray("uids").getJSONObject(0).getString("atype"));
//        assertEquals("01ERJWE5FS4RAZKG6SKQ3ZYSKV", sharedidObj.getJSONArray("uids").getJSONObject(0).getJSONObject("ext").getString("third"));
//    }
//
//    @Test
//    public void testClearStoredExternalUserIdsInPostData() throws Exception {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//
//        // User Id from External Third Party Sources
//        TargetingParams.storeExternalUserId(new ExternalUserId("adserver.org", "111111111111", null, new HashMap() {{
//            put("rtiPartner", "TDID");
//        }}));
//        TargetingParams.storeExternalUserId(new ExternalUserId("netid.de", "999888777", null, null));
//        TargetingParams.storeExternalUserId(new ExternalUserId("criteo.com", "_fl7bV96WjZsbiUyQnJlQ3g4ckh5a1N", null, null));
//        TargetingParams.storeExternalUserId(new ExternalUserId("liveramp.com", "AjfowMv4ZHZQJFM8TpiUnYEyA81Vdgg", null, null));
//        TargetingParams.storeExternalUserId(new ExternalUserId("sharedid.org", "111111111111", 1, new HashMap() {{
//            put("third", "01ERJWE5FS4RAZKG6SKQ3ZYSKV");
//        }}));
//
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//        assertEquals(uuid, connector.getAuctionId());
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject user = postData.getJSONObject("user");
//        JSONObject ext = user.getJSONObject("ext");
//        JSONArray eids = ext.getJSONArray("eids");
//
//        JSONObject adserverObj = eids.getJSONObject(0);
//        assertEquals("adserver.org", adserverObj.get("source"));
//        assertEquals("111111111111", adserverObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//        assertEquals("TDID", adserverObj.getJSONArray("uids").getJSONObject(0).getJSONObject("ext").getString("rtiPartner"));
//
//        JSONObject netidObj = eids.getJSONObject(1);
//        assertEquals("netid.de", netidObj.get("source"));
//        assertEquals("999888777", netidObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//
//        JSONObject criteoObj = eids.getJSONObject(2);
//        assertEquals("criteo.com", criteoObj.get("source"));
//        assertEquals("_fl7bV96WjZsbiUyQnJlQ3g4ckh5a1N", criteoObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//
//        JSONObject liverampObj = eids.getJSONObject(3);
//        assertEquals("liveramp.com", liverampObj.get("source"));
//        assertEquals("AjfowMv4ZHZQJFM8TpiUnYEyA81Vdgg", liverampObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//
//        JSONObject sharedidObj = eids.getJSONObject(4);
//        assertEquals("sharedid.org", sharedidObj.get("source"));
//        assertEquals("111111111111", sharedidObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//        assertEquals("1", sharedidObj.getJSONArray("uids").getJSONObject(0).getString("atype"));
//        assertEquals("01ERJWE5FS4RAZKG6SKQ3ZYSKV", sharedidObj.getJSONArray("uids").getJSONObject(0).getJSONObject("ext").getString("third"));
//
//        TargetingParams.clearStoredExternalUserIds();
//
//        postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        user = postData.getJSONObject("user");
//
//        assertFalse(user.has("ext"));
//    }
//
//    @Test
//    public void testBothExternalUserArrayAndStoredExternalUserIdsInPostData() throws Exception {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//
//        // User Id from External Third Party Sources
//        ArrayList<ExternalUserId> externalUserIdArray = new ArrayList<>();
//        externalUserIdArray.add(new ExternalUserId("adserver.org", "111111111111", null, new HashMap() {{
//            put("rtiPartner", "TDID");
//        }}));
//        externalUserIdArray.add(new ExternalUserId("netid.de", "999888777", null, null));
//        PrebidMobile.setExternalUserIds(externalUserIdArray);
//
//        TargetingParams.storeExternalUserId(new ExternalUserId("criteo.com", "_fl7bV96WjZsbiUyQnJlQ3g4ckh5a1N", null, null));
//        TargetingParams.storeExternalUserId(new ExternalUserId("liveramp.com", "AjfowMv4ZHZQJFM8TpiUnYEyA81Vdgg", null, null));
//        TargetingParams.storeExternalUserId(new ExternalUserId("sharedid.org", "111111111111", 1, new HashMap() {{
//            put("third", "01ERJWE5FS4RAZKG6SKQ3ZYSKV");
//        }}));
//
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//        assertEquals(uuid, connector.getAuctionId());
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject user = postData.getJSONObject("user");
//        JSONObject ext = user.getJSONObject("ext");
//        JSONArray eids = ext.getJSONArray("eids");
//
//        JSONObject adserverObj = eids.getJSONObject(0);
//        assertEquals("adserver.org", adserverObj.get("source"));
//        assertEquals("111111111111", adserverObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//        assertEquals("TDID", adserverObj.getJSONArray("uids").getJSONObject(0).getJSONObject("ext").getString("rtiPartner"));
//
//        JSONObject netidObj = eids.getJSONObject(1);
//        assertEquals("netid.de", netidObj.get("source"));
//        assertEquals("999888777", netidObj.getJSONArray("uids").getJSONObject(0).getString("id"));
//    }
//
//    @Test
//    public void testStoredExternalUserIdsInPostDataWithEmptySource() throws Exception {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//
//        // User Id from External Third Party Sources
//        TargetingParams.storeExternalUserId(new ExternalUserId("", "999888777", null, null));
//
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//        assertEquals(uuid, connector.getAuctionId());
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject user = postData.getJSONObject("user");
//        assertFalse(user.has("ext"));
//
//    }
//
//    @Test
//    public void testStoredExternalUserIdsInPostDataWithEmptyIdentifier() throws Exception {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//
//        // User Id from External Third Party Sources
//        TargetingParams.storeExternalUserId(new ExternalUserId("netid.de", "", null, null));
//
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//        assertEquals(uuid, connector.getAuctionId());
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject user = postData.getJSONObject("user");
//        assertFalse(user.has("ext"));
//
//    }
//
//    @Test
//    public void testStoredExternalUserIdsInPostDataWithEmptyAType() throws Exception {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//
//        // User Id from External Third Party Sources
//        TargetingParams.storeExternalUserId(new ExternalUserId("netid.de", "999888777", null, new HashMap() {{
//            put("rtiPartner", "TDID");
//        }}));
//
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//        assertEquals(uuid, connector.getAuctionId());
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject user = postData.getJSONObject("user");
//        assertTrue(user.has("ext"));
//        assertFalse(user.getJSONObject("ext").getJSONArray("eids").getJSONObject(0).getJSONArray("uids").getJSONObject(0).has("atype"));
//        assertTrue(user.getJSONObject("ext").getJSONArray("eids").getJSONObject(0).getJSONArray("uids").getJSONObject(0).has("ext"));
//    }
//
//    @Test
//    public void testStoredExternalUserIdsInPostDataWithEmptyExt() throws Exception {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//
//        // User Id from External Third Party Sources
//        TargetingParams.storeExternalUserId(new ExternalUserId("sharedid.org", "111111111111", 1, null));
//
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//        assertEquals(uuid, connector.getAuctionId());
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject user = postData.getJSONObject("user");
//        assertTrue(user.has("ext"));
//        assertTrue(user.getJSONObject("ext").getJSONArray("eids").getJSONObject(0).getJSONArray("uids").getJSONObject(0).has("atype"));
//        assertFalse(user.getJSONObject("ext").getJSONArray("eids").getJSONObject(0).getJSONArray("uids").getJSONObject(0).has("ext"));
//    }
//
//    @Test
//    public void testConnectionHasSetCustomHeaders() throws Exception {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setShareGeoLocation(true);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        HashMap<String, String> customHeaders = new HashMap<>();
//        customHeaders.put("test-key", "test-value");
//        PrebidMobile.setCustomHeaders(customHeaders);
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//
//        RecordedRequest recordedRequest = server.takeRequest();
//        Headers headers = recordedRequest.getHeaders();
//        assertEquals("test-value", headers.get("test-key"));
//    }
//
//    @Test
//    public void testRubiconDefaultError() {
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.htmlUnreachableFromRubicon()));
//        HttpUrl hostUrl = server.url("/");
//        Host.CUSTOM.setHostUrl(hostUrl.toString());
//        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
//        PrebidMobile.setPrebidServerAccountId("12345");
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(320, 50));
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//
//        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
//        bgLooper.runToEndOfTasks();
//
//        Robolectric.flushBackgroundThreadScheduler();
//        Robolectric.flushForegroundThreadScheduler();
//        verify(mockListener).onDemandFailed(ResultCode.PREBID_SERVER_ERROR, uuid);
//    }
//
//    @Test
//    public void testPostDataGdprSubjectTrue() throws Exception {
//
//        //given
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        TargetingParams.setSubjectToGDPR(true);
//
//        Integer gdprSubject = null;
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//        try {
//            gdprSubject = postData.getJSONObject("regs").getJSONObject("ext").getInt("gdpr");
//        } catch (Exception ex) {
//            fail("parsing error");
//        }
//
//        //then
//        assertEquals(Integer.valueOf(1), gdprSubject);
//    }
//
//    @Test
//    public void testPostDataGdprSubjectFalse() throws Exception {
//
//        //given
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        TargetingParams.setSubjectToGDPR(false);
//
//        Object gdprSubject = null;
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//        try {
//            gdprSubject = postData.opt("regs");
//        } catch (Exception ex) {
//            fail("parsing error");
//        }
//
//        //then
//        assertNull(gdprSubject);
//    }
//
//    @Test
//    public void testPostDataGdprSubjectUndefined() throws Exception {
//
//        //given
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        TargetingParams.setSubjectToGDPR(null);
//
//        Object gdprSubject = null;
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//        try {
//            gdprSubject = postData.optJSONObject("regs");
//        } catch (Exception ex) {
//            fail("parsing error");
//        }
//
//        //then
//        assertNull(gdprSubject);
//    }
//
//    //GDPR Consent
//    @Test
//    public void testPostDataGdprConsent() throws Exception {
//
//        //given
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        TargetingParams.setSubjectToGDPR(true);
//        TargetingParams.setGDPRConsentString("BOEFEAyOEFEAyAHABDENAI4AAAB9vABAASA");
//
//        Integer gdprSubject = null;
//        String gdprConsent = null;
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//        try {
//            gdprSubject = postData.getJSONObject("regs").getJSONObject("ext").getInt("gdpr");
//            gdprConsent = postData.getJSONObject("user").getJSONObject("ext").getString("consent");
//        } catch (Exception ex) {
//            fail("parsing error");
//        }
//
//        //then
//        assertEquals(Integer.valueOf(1), gdprSubject);
//        assertEquals("BOEFEAyOEFEAyAHABDENAI4AAAB9vABAASA", gdprConsent);
//    }
//
//    @Test
//    public void testPostDataGdprConsentAndGdprSubjectFalse() throws Exception {
//
//        //given
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        TargetingParams.setSubjectToGDPR(false);
//        TargetingParams.setGDPRConsentString("BOEFEAyOEFEAyAHABDENAI4AAAB9vABAASA");
//
//        Object gdprSubject = null;
//        Object gdprConsent = null;
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//        try {
//            gdprSubject = postData.opt("regs");
//            gdprConsent = postData.getJSONObject("user").opt("ext");
//        } catch (Exception ex) {
//            fail("parsing error");
//        }
//
//        //then
//        assertNull(gdprSubject);
//        assertNull(gdprConsent);
//    }
//
//    //COPPA
//    @Test
//    public void testPostDataCoppaTrue() throws Exception {
//
//        //given
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        TargetingParams.setSubjectToCOPPA(true);
//        int coppa = 0;
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//        try {
//            coppa = postData.getJSONObject("regs").getInt("coppa");
//        } catch (Exception ex) {
//            fail("parsing error");
//        }
//
//        //then
//        assertEquals(1, coppa);
//    }
//
//    @Test
//    public void testPostDataCoppaFalse() throws Exception {
//
//        //given
//        TargetingParams.setSubjectToCOPPA(false);
//        Object coppa = null;
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//        try {
//            coppa = postData.opt("regs");
//        } catch (Exception ex) {
//            fail("parsing error");
//        }
//
//        //then
//        assertNull(coppa);
//    }
//
//    //CCPA
//    @Test
//    public void testPostDataCcpa() throws Exception {
//
//        //given
//
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
//        SharedPreferences.Editor editor = pref.edit();
//        editor.putString("IABUSPrivacy_String", "1YN");
//        editor.apply();
//
//        String ccpa = null;
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//        try {
//            ccpa = postData.getJSONObject("regs").getJSONObject("ext").getString("us_privacy");
//        } catch (Exception ex) {
//            fail("parsing error");
//        }
//
//        //then
//        assertEquals("1YN", ccpa);
//    }
//
//    @Test
//    public void testPostDataCcpaEmptyValue() throws Exception {
//
//        //given
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
//        SharedPreferences.Editor editor = pref.edit();
//        editor.putString("IABUSPrivacy_String", "");
//        editor.apply();
//
//        Object ccpa = null;
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//        try {
//            ccpa = postData.opt("regs");
//        } catch (Exception ex) {
//            fail("parsing error");
//        }
//
//        //then
//        assertNull(ccpa);
//
//    }
//
//    @Test
//    public void testPostDataCcpaUndefined() throws Exception {
//
//        //given
//
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
//        SharedPreferences.Editor editor = pref.edit();
//        editor.remove("IABUSPrivacy_String");
//        editor.apply();
//
//        Object ccpa = null;
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//        try {
//            ccpa = postData.opt("regs");
//        } catch (Exception ex) {
//            fail("parsing error");
//        }
//
//        //then
//        assertNull(ccpa);
//    }
//
//    @Test
//    public void testPostDataWithStoredResponses() throws Exception {
//        //given
//        PrebidMobile.setStoredAuctionResponse("111122223333");
//        PrebidMobile.addStoredBidResponse("appnexus", "221144");
//        PrebidMobile.addStoredBidResponse("rubicon", "221155");
//
//        //when
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(500, 700));
//
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.INTERSTITIAL);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//        JSONObject prebid = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("prebid");
//        JSONObject storedauctionresponse = prebid.getJSONObject("storedauctionresponse");
//        JSONArray storedbidresponse = prebid.getJSONArray("storedbidresponse");
//        JSONObject storedbidresponse1 = storedbidresponse.getJSONObject(0);
//        JSONObject storedbidresponse2 = storedbidresponse.getJSONObject(1);
//
//        //then
//        assertEquals("111122223333", storedauctionresponse.getString("id"));
//
//        Assert.assertEquals(2, storedbidresponse.length());
//        assertEquals("appnexus", storedbidresponse1.getString("bidder"));
//        assertEquals("221144", storedbidresponse1.getString("id"));
//        assertEquals("rubicon", storedbidresponse2.getString("bidder"));
//        assertEquals("221155", storedbidresponse2.getString("id"));
//
//    }
//
//    @Test
//    public void testPostDataWithOmidNameAndVersion() throws Exception {
//
//        //given
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//
//        String partnerName = "PartnerName";
//        String partnerVersion = "1.0";
//
//        TargetingParams.setOmidPartnerName(partnerName);
//        TargetingParams.setOmidPartnerVersion(partnerVersion);
//
//        String omidPartnerName = null;
//        String omidPartnerVersion = null;
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//        try {
//            JSONObject ext = postData.getJSONObject("source").getJSONObject("ext");
//            omidPartnerName = ext.getString("omidpn");
//            omidPartnerVersion = ext.getString("omidpv");
//
//        } catch (Exception ex) {
//            fail("parsing error");
//        }
//
//        //then
//        assertEquals(partnerName, omidPartnerName);
//        assertEquals(partnerVersion, omidPartnerVersion);
//    }
//
//    @Test
//    public void testPostDataWithoutOmidNameAndVersion() throws Exception {
//
//        //given
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//
//        TargetingParams.setOmidPartnerName(null);
//        TargetingParams.setOmidPartnerVersion(null);
//
//        JSONObject ext = null;
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//        try {
//            ext = (JSONObject) postData.getJSONObject("source").opt("ext");
//
//        } catch (Exception ex) {
//            fail("parsing error");
//        }
//
//        //then
//        assertNull(ext);
//    }
//
//    @Test
//    public void testPbsDebug() throws Exception {
//        pbsDebugHelper(true, 1);
//        pbsDebugHelper(false, null);
//
//    }
//
//    private void pbsDebugHelper(boolean pbsDebug, @Nullable Integer expectedTest) throws Exception {
//        //given
//        PrebidMobile.setPbsDebug(pbsDebug);
//
//        //when
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.INTERSTITIAL);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//        Integer test = (Integer) postData.opt("test");
//
//        //then
//        assertEquals(expectedTest, test);
//    }
//
//    @Test
//    public void testBannerBaseAdUnit() throws Exception {
//        PrebidMobile.setPrebidServerAccountId("12345");
//
//        //given
//        BannerAdUnit.Parameters parameters = new BannerAdUnit.Parameters();
//
//        parameters.setApi(Arrays.asList(Signals.Api.VPAID_1, Signals.Api.VPAID_2));
//
//        //when
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(500, 700));
//
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.BANNER);
//        configuration.setBannerParameters(parameters);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject video = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("banner");
//
//        //then
//        List<Integer> apiList = Util.convertJSONArray(video.getJSONArray("api"));
//        assertEquals(2, apiList.size());
//        assertTrue(apiList.contains(1) && apiList.contains(2));
//
//    }
//
//    @Test
//    public void testVideoAdUnit() throws Exception {
//        //given
//        PrebidMobile.setPrebidServerAccountId("12345");
//
//        //when
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(500, 700));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.VIDEO);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject video = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("video");
//
//        JSONObject cache = postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("cache");
//        JSONObject vastxml = cache.getJSONObject("vastxml");
//
//        //then
//        assertEquals(500, video.getInt("w"));
//        assertEquals(700, video.getInt("h"));
//        assertEquals(1, video.getInt("linearity"));
//
//        assertNotNull(vastxml);
//
//    }
//
//    @Test
//    public void testVideoInterstitialAdUnit() throws Exception {
//        //given
//        PrebidMobile.setPrebidServerAccountId("12345");
//
//        //when
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(500, 700));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.VIDEO_INTERSTITIAL);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject video = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("video");
//
//        JSONObject cache = postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("cache");
//        JSONObject vastxml = cache.getJSONObject("vastxml");
//
//        int instl = postData.getJSONArray("imp").getJSONObject(0).getInt("instl");
//
//        //then
//        assertEquals(5, video.getInt("placement"));
//        assertEquals(1, video.getInt("linearity"));
//        assertEquals(1, instl);
//        assertNotNull(vastxml);
//    }
//
//    @Test
//    public void testRewardedVideoAdUnit() throws Exception {
//        //given
//        PrebidMobile.setPrebidServerAccountId("12345");
//
//        //when
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(500, 700));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.REWARDED_VIDEO);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject video = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("video");
//
//        JSONObject cache = postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("cache");
//        JSONObject vastxml = cache.getJSONObject("vastxml");
//
//        int instl = postData.getJSONArray("imp").getJSONObject(0).getInt("instl");
//
//        int isRewarded = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("prebid").getInt("is_rewarded_inventory");
//
//        //then
//        assertEquals(5, video.getInt("placement"));
//        assertEquals(1, video.getInt("linearity"));
//        assertEquals(1, instl);
//        assertEquals(1, isRewarded);
//        assertNotNull(vastxml);
//    }
//
//    @Test
//    public void testVideoBaseAdUnit() throws Exception {
//        PrebidMobile.setPrebidServerAccountId("12345");
//
//        //given
//        VideoAdUnit.Parameters parameters = new VideoAdUnit.Parameters();
//
//        parameters.setApi(Arrays.asList(Signals.Api.VPAID_1, Signals.Api.VPAID_2));
//        parameters.setMaxBitrate(1500);
//        parameters.setMinBitrate(300);
//        parameters.setMaxDuration(30);
//        parameters.setMinDuration(5);
//        parameters.setMimes(Arrays.asList("video/x-flv", "video/mp4"));
//        parameters.setPlaybackMethod(Arrays.asList(Signals.PlaybackMethod.AutoPlaySoundOn, Signals.PlaybackMethod.ClickToPlay));
//        parameters.setProtocols(Arrays.asList(Signals.Protocols.VAST_2_0, Signals.Protocols.VAST_3_0));
//        parameters.setStartDelay(Signals.StartDelay.PreRoll);
//
//        //when
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(500, 700));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(AdType.VIDEO);
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//        configuration.setVideoParameters(parameters);
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//
//        JSONObject video = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("video");
//
//        //then
//        List<Integer> apiList = Util.convertJSONArray(video.getJSONArray("api"));
//        assertEquals(2, apiList.size());
//        assertTrue(apiList.contains(1) && apiList.contains(2));
//
//        int maxBitrate = video.getInt("maxbitrate");
//        assertEquals(1500, maxBitrate);
//
//        int minBitrate = video.getInt("minbitrate");
//        assertEquals(300, minBitrate);
//
//        int maxDuration = video.getInt("maxduration");
//        assertEquals(30, maxDuration);
//
//        int minDuration = video.getInt("minduration");
//        assertEquals(5, minDuration);
//
//        List<String> mimeList = Util.convertJSONArray(video.getJSONArray("mimes"));
//
//        assertEquals(2, mimeList.size());
//        assertTrue(mimeList.contains("video/x-flv") && mimeList.contains("video/mp4"));
//
//        List<Integer> playbackList = Util.convertJSONArray(video.getJSONArray("playbackmethod"));
//        assertEquals(2, playbackList.size());
//        assertTrue(playbackList.contains(1) && playbackList.contains(3));
//
//        List<Integer> protocolList = Util.convertJSONArray(video.getJSONArray("protocols"));
//        assertEquals(2, protocolList.size());
//        assertTrue(protocolList.contains(2) && protocolList.contains(3));
//
//        int startDelay = video.getInt("startdelay");
//        assertEquals(0, startDelay);
//
//    }
//
//    @Test
//    public void testPostDataWithAdvancedInterstitial() throws Exception {
//
//        //given
//        AdSize minSizePerc = new AdSize(50, 70);
//
//        JSONObject extInterstitial = null;
//        JSONObject banner = null;
//        int instl = 0;
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.INTERSTITIAL, null, null, minSizePerc, null, null, null);
//
//        try {
//            extInterstitial = postData.getJSONObject("device").getJSONObject("ext").getJSONObject("prebid").getJSONObject("interstitial");
//        } catch (Exception ex) {
//            fail("extInterstitial parsing fail");
//        }
//
//        try {
//            banner = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("banner").getJSONArray("format").getJSONObject(0);
//        } catch (Exception ex) {
//            fail("banner parsing fail");
//        }
//
//        try {
//            instl = postData.getJSONArray("imp").getJSONObject(0).getInt("instl");
//        } catch (Exception ex) {
//            fail("instl parsing fail");
//        }
//
//        //then
//        Assert.assertNotNull(extInterstitial);
//        assertTrue(extInterstitial.getInt("minwidthperc") == 50 && extInterstitial.getInt("minheightperc") == 70);
//
//        Assert.assertNotNull(banner);
//        assertTrue(banner.has("w"));
//        assertTrue(banner.has("h"));
//
//        assertEquals(1, instl);
//    }
//
//    @Test
//    public void testPostDataWithoutAdvancedInterstitial() throws Exception {
//
//        //given
//        JSONObject extInterstitial = null;
//        int instl = 0;
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.INTERSTITIAL, null, null, null, null, null, null);
//
//        try {
//            instl = postData.getJSONArray("imp").getJSONObject(0).getInt("instl");
//        } catch (Exception ex) {
//        }
//
//        try {
//            extInterstitial = postData.getJSONObject("device").getJSONObject("ext").getJSONObject("prebid").getJSONObject("interstitial");
//        } catch (Exception ex) {
//        }
//
//        //then
//        assertEquals(1, instl);
//        assertNull(extInterstitial);
//    }
//
//    @Test
//    public void testPostDataBannerWithAdvancedInterstitial() throws Exception {
//
//        //given
//        AdSize minSizePerc = new AdSize(50, 70);
//        JSONObject extInterstitial = null;
//        int instl = 0;
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, minSizePerc, null, null, null);
//
//        try {
//            extInterstitial = postData.getJSONObject("device").getJSONObject("ext").getJSONObject("prebid").getJSONObject("interstitial");
//        } catch (Exception ex) {
//        }
//
//        try {
//            instl = postData.getJSONArray("imp").getJSONObject(0).getInt("instl");
//        } catch (Exception ex) {
//        }
//
//        //then
//        assertNull(extInterstitial);
//        assertEquals(0, instl);
//    }
//
//    @Test
//    public void testPostDataWithGlobalUserKeyword() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//
//        //given
//        TargetingParams.addUserKeyword("value10");
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//
//        String keywords = null;
//        try {
//            keywords = postData.getJSONObject("user").optString("keywords", null);
//        } catch (Exception ex) {
//            fail("keywords parsing fail");
//        }
//
//        //then
//        assertNotNull(keywords);
//        assertEquals("value10", keywords);
//    }
//
//    @Test
//    public void testPostDataWithGlobalContextKeyword() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//
//        //given
//        TargetingParams.addContextKeyword("value10");
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//
//        String keywords = null;
//        try {
//            keywords = postData.getJSONObject("app").optString("keywords", null);
//        } catch (Exception ex) {
//            fail("keywords parsing fail");
//        }
//
//        //then
//        assertNotNull(keywords);
//        assertEquals("value10", keywords);
//
//    }
//
//    @Test
//    public void testPostDataWithAccessControlList() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, JSONException {
//
//        //given
//        TargetingParams.addBidderToAccessControlList(TargetingParams.BIDDER_NAME_RUBICON_PROJECT);
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//
//        JSONArray biddersJsonArray = null;
//        try {
//            biddersJsonArray = postData.getJSONObject("ext").getJSONObject("prebid").getJSONObject("data").getJSONArray("bidders");
//        } catch (Exception ex) {
//            fail("keywords parsing fail");
//        }
//
//        //then
//        assertNotNull(biddersJsonArray);
//        assertEquals(1, biddersJsonArray.length());
//        assertEquals(TargetingParams.BIDDER_NAME_RUBICON_PROJECT, biddersJsonArray.get(0).toString());
//
//    }
//
//    @Test
//    public void testPostDataWithGlobalUserData() throws Exception {
//
//        //given
//        TargetingParams.addUserData("key1", "value10");
//        TargetingParams.addUserData("key2", "value20");
//        TargetingParams.addUserData("key2", "value21");
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//
//        JSONObject dataJsonObject = null;
//        try {
//            dataJsonObject = postData.getJSONObject("user").getJSONObject("ext").getJSONObject("data");
//        } catch (Exception ex) {
//            fail("keywords parsing fail");
//        }
//
//        List<String> listKey1 = Util.convertJSONArray(dataJsonObject.getJSONArray("key1"));
//
//        List<String> listKey2 = Util.convertJSONArray(dataJsonObject.getJSONArray("key2"));
//
//        //then
//        assertNotNull(dataJsonObject);
//        assertEquals(2, dataJsonObject.length());
//
//        assertEquals(listKey1.size(), 1);
//        assertThat(listKey1, containsInAnyOrder("value10"));
//
//        assertEquals(listKey2.size(), 2);
//        assertThat(listKey2, containsInAnyOrder("value20", "value21"));
//    }
//
//
//    @Test
//    public void testPostDataWithGlobalContextData() throws Exception {
//
//        //given
//        TargetingParams.addContextData("key1", "value10");
//        TargetingParams.addContextData("key2", "value20");
//        TargetingParams.addContextData("key2", "value21");
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, null, null, null);
//
//        JSONObject dataJsonObject = null;
//        try {
//            dataJsonObject = postData.getJSONObject("app").getJSONObject("ext").getJSONObject("data");
//        } catch (Exception ex) {
//            fail("keywords parsing fail");
//        }
//
//        List<String> listKey1 = Util.convertJSONArray(dataJsonObject.getJSONArray("key1"));
//
//        List<String> listKey2 = Util.convertJSONArray(dataJsonObject.getJSONArray("key2"));
//
//        //then
//        assertNotNull(dataJsonObject);
//        assertEquals(2, dataJsonObject.length());
//
//        assertEquals(listKey1.size(), 1);
//        assertThat(listKey1, containsInAnyOrder("value10"));
//
//        assertEquals(listKey2.size(), 2);
//        assertThat(listKey2, containsInAnyOrder("value20", "value21"));
//    }
//
//    @Test
//    public void testPostDataWithAdunitContextKeyword() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//
//        //given
//        HashSet<String> contextKeywordsSet = new HashSet<>(1);
//        contextKeywordsSet.add("value10");
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, contextKeywordsSet, null, null, null, null);
//
//        String keywords = null;
//
//        try {
//            keywords = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("context").getString("keywords");
//        } catch (Exception ex) {
//            fail("keywords parsing fail");
//        }
//
//        //then
//        assertNotNull(keywords);
//        assertEquals("value10", keywords);
//    }
//
//    @Test
//    public void testPostDataWithAdUnitContextData() throws Exception {
//
//        //given
//        Map<String, Set<String>> contextDataDictionary = new HashMap<>(2);
//        contextDataDictionary.put("key1", new HashSet<>(Arrays.asList("value10")));
//        contextDataDictionary.put("key2", new HashSet<>(Arrays.asList("value20", "value21")));
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, contextDataDictionary, null, null, null, null, null);
//
//        JSONObject dataJsonObject = null;
//        try {
//            dataJsonObject = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("context").getJSONObject("data");
//        } catch (Exception ex) {
//            fail("keywords parsing fail");
//        }
//
//        List<String> listKey1 = Util.convertJSONArray(dataJsonObject.getJSONArray("key1"));
//
//        List<String> listKey2 = Util.convertJSONArray(dataJsonObject.getJSONArray("key2"));
//
//        //then
//        assertNotNull(dataJsonObject);
//        assertEquals(2, dataJsonObject.length());
//
//        assertEquals(listKey1.size(), 1);
//        assertThat(listKey1, containsInAnyOrder("value10"));
//
//        assertEquals(listKey2.size(), 2);
//        assertThat(listKey2, containsInAnyOrder("value20", "value21"));
//
//    }
//
//    @Test
//    public void testNativeAdUnitInPostData() throws Exception {
//        server.setDispatcher(new Dispatcher() {
//            @Override
//            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
//                String postData = request.getBody().readUtf8();
//                try {
//                    JSONObject jsonObject = new JSONObject(postData);
//                    JSONArray jsonArrayImp = jsonObject.getJSONArray("imp");
//                    JSONObject jsonObjectImp = (JSONObject) jsonArrayImp.get(0);
//                    JSONObject nativeObject = (JSONObject) jsonObjectImp.get("native");
//                    JSONObject nativeRequest = new JSONObject((String) nativeObject.get("request"));
//                    JSONArray eventtrackersArray = nativeRequest.getJSONArray("eventtrackers");
//                    JSONObject eventtracker = (JSONObject) eventtrackersArray.get(0);
//                    JSONArray eventtrackerMethodsArray = eventtracker.getJSONArray("methods");
//                    JSONObject ext = (JSONObject) nativeRequest.get("ext");
//                    JSONArray assets = nativeRequest.getJSONArray("assets");
//                    JSONObject asset1 = (JSONObject) assets.get(0);
//                    JSONObject assetTitle = (JSONObject) asset1.get("title");
//                    JSONObject assetImage = (JSONObject) asset1.get("img");
//                    JSONObject asset2 = (JSONObject) assets.get(1);
//                    JSONObject assetImage2 = (JSONObject) asset2.get("img");
//                    JSONObject assetData = (JSONObject) asset2.get("data");
//
//                    assertEquals(2, nativeRequest.getInt("context"));
//                    assertEquals(20, nativeRequest.getInt("contextsubtype"));
//                    assertEquals(4, nativeRequest.getInt("plcmttype"));
//                    assertEquals(10, nativeRequest.getInt("plcmtcnt"));
//                    assertEquals(12, nativeRequest.getInt("seq"));
//                    assertEquals(1, nativeRequest.getInt("aurlsupport"));
//                    assertEquals(1, nativeRequest.getInt("durlsupport"));
//                    assertEquals(1, eventtracker.getInt("event"));
//                    assertEquals(1, eventtrackerMethodsArray.get(0));
//                    assertEquals(2, eventtrackerMethodsArray.get(1));
//                    assertEquals(1, nativeRequest.getInt("privacy"));
//                    assertEquals("value", ext.get("key"));
//                    assertEquals(90, assetTitle.getInt("len"));
//                    assertEquals(1, assetTitle.getInt("required"));
//                    assertEquals(1, assetImage.getInt("type"));
//                    assertEquals(20, assetImage.getInt("wmin"));
//                    assertEquals(20, assetImage.getInt("hmin"));
//                    assertEquals(1, asset1.getInt("required"));
//                    assertEquals(3, assetImage2.getInt("type"));
//                    assertEquals(200, assetImage2.getInt("wmin"));
//                    assertEquals(200, assetImage2.getInt("hmin"));
//                    assertEquals(1, asset2.getInt("required"));
//                    assertEquals(1, assetData.getInt("type"));
//                    assertEquals(90, assetData.getInt("len"));
//                    assertEquals(1, assetData.getInt("required"));
//
//                } catch (JSONException err) {
//                    Log.d("Error", err.toString());
//                }
//
//                return new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus());
//            }
//        });
//        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//        PrebidMobile.setPrebidServerAccountId("123456");
//
//        NativeAdUnit nativeAdUnit = new NativeAdUnit("123456");
//        nativeAdUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
//        nativeAdUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.RECOMMENDATION_WIDGET);
//        nativeAdUnit.setPlacementCount(10);
//        nativeAdUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
//        nativeAdUnit.setPrivacy(true);
//        nativeAdUnit.setDUrlSupport(true);
//        nativeAdUnit.setAUrlSupport(true);
//        nativeAdUnit.setSeq(12);
//        JSONObject ext = new JSONObject();
//        try {
//            ext.put("key", "value");
//        } catch (JSONException e) {
//            e.printStackTrace();
//
//        }
//        nativeAdUnit.setExt(ext);
//        ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods = new ArrayList<>();
//        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
//        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS);
//        try {
//            NativeEventTracker tracker = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods);
//            nativeAdUnit.addEventTracker(tracker);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        NativeTitleAsset title = new NativeTitleAsset();
//        title.setLength(90);
//        title.setRequired(true);
//        nativeAdUnit.addAsset(title);
//        NativeImageAsset icon = new NativeImageAsset();
//        icon.setImageType(NativeImageAsset.IMAGE_TYPE.ICON);
//        icon.setWMin(20);
//        icon.setHMin(20);
//        icon.setRequired(true);
//        nativeAdUnit.addAsset(icon);
//        NativeImageAsset image = new NativeImageAsset();
//        image.setImageType(NativeImageAsset.IMAGE_TYPE.MAIN);
//        image.setHMin(200);
//        image.setWMin(200);
//        image.setRequired(true);
//        nativeAdUnit.addAsset(image);
//        NativeDataAsset data = new NativeDataAsset();
//        data.setLen(90);
//        data.setDataType(NativeDataAsset.DATA_TYPE.SPONSORED);
//        data.setRequired(true);
//        nativeAdUnit.addAsset(data);
//        nativeAdUnit.setAutoRefreshPeriodMillis(30000);
//
//        MoPubView testView = new MoPubView(activity);
//        OnCompleteListener mockListener = mock(OnCompleteListener.class);
//        nativeAdUnit.fetchDemand(testView, mockListener);
//    }
//
//    @Test
//    public void testPostDataWithPrebidAdSlot() throws Exception {
//
//        //given
//        TargetingParams.addBidderToAccessControlList(TargetingParams.BIDDER_NAME_RUBICON_PROJECT);
//
//        //when
//        JSONObject postData = getPostDataHelper(AdType.BANNER, null, null, null, "/1111111/homepage/med-rect-2", null, null);
//
//        String adSlot = null;
//        try {
//            adSlot = postData.getJSONArray("imp").getJSONObject(0).getJSONObject("ext").getJSONObject("context").getJSONObject("data").getString("adslot");
//        } catch (Exception ex) {
//            fail("keywords parsing fail");
//        }
//
//        //then
//        assertNotNull(adSlot);
//        assertEquals("/1111111/homepage/med-rect-2", adSlot);
//
//    }
//
//    private JSONObject getPostDataHelper(AdType adType, @Nullable Map<String, Set<String>> contextDataDictionary, @Nullable Set<String> contextKeywordsSet, @Nullable AdSize minSizePerc, @Nullable String adSlot, @Nullable BannerBaseAdUnit.Parameters bannerParameters, @Nullable VideoBaseAdUnit.Parameters videoParameters) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//
//        PrebidMobile.setApplicationContext(activity.getApplicationContext());
//
//        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
//
//        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
//        PrebidServerAdapter adapter = new PrebidServerAdapter();
//        HashSet<AdSize> sizes = new HashSet<>();
//        sizes.add(new AdSize(300, 250));
//
//        AdUnitConfiguration configuration = new AdUnitConfiguration();
//        configuration.setConfigId("67890");
//        configuration.setAdType(adType);
//        configuration.addContextKeywords(contextKeywordsSet);
//        configuration.setMinSizePercentage(minSizePerc);
//        configuration.setPbAdSlot(adSlot);
//        configuration.setBannerParameters(bannerParameters);
//        configuration.setVideoParameters(videoParameters);
//
//        for (AdSize size : sizes) {
//            configuration.addSize(size);
//        }
//
//        if (contextDataDictionary != null) {
//            for (String key : contextDataDictionary.keySet()) {
//                configuration.addContextData(key, contextDataDictionary.get(key));
//            }
//        }
//
//        String uuid = UUID.randomUUID().toString();
//        adapter.requestDemand(configuration, mockListener, uuid);
//        @SuppressWarnings("unchecked")
//        ArrayList<PrebidServerAdapter.ServerConnector> connectors = (ArrayList<PrebidServerAdapter.ServerConnector>) FieldUtils.readDeclaredField(adapter, "serverConnectors", true);
//        PrebidServerAdapter.ServerConnector connector = connectors.get(0);
//
//        JSONObject postData = (JSONObject) MethodUtils.invokeMethod(connector, true, "getPostData");
//        return postData;
//    }
//}
