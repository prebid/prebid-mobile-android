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

import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.formats.NativeCustomTemplateAd;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.tasksmanager.BackgroundThreadExecutor;
import org.prebid.mobile.tasksmanager.MainThreadExecutor;
import org.prebid.mobile.tasksmanager.TasksManager;
import org.prebid.mobile.testutils.BaseSetup;
import org.prebid.mobile.testutils.MockPrebidServerResponses;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK, manifest = Config.NONE)
public class PrebidNativeNativeTest extends BaseSetup {

    static final String PBS_CONFIG_ID_NATIVE_APPNEXUS = "25e17008-5081-4676-94d5-923ced4359d3";
    static final String PBS_ACCOUNT_ID_APPNEXUS = "bfa84af2-bd16-4d35-96ad-31c6bb888df0";
    private boolean adExpired = false;
    private boolean adClicked = false;

    @Rule
    public ErrorCollector errorCollector = new ErrorCollector();

    @Override
    public void setup() {
        super.setup();
        CacheManager.clear();
        ((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).startThread();
    }

    @Override
    public void tearDown() {
        super.tearDown();
        ((BackgroundThreadExecutor)TasksManager.getInstance().backgroundThreadExecutor).shutdown();
        adExpired = false;
        adClicked = false;
        TargetingParams.clearAccessControlList();
        TargetingParams.clearUserData();
        TargetingParams.clearContextData();
        TargetingParams.clearContextKeywords();
        TargetingParams.clearUserKeywords();
    }

    @Test
    public void testSuccessfulPrebidNativeResponse() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.validResponsePrebidNativeNative()));
        HttpUrl hostUrl = server.url("/");
        Host.CUSTOM.setHostUrl(hostUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setPrebidServerAccountId(PBS_ACCOUNT_ID_APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        RequestParams requestParams = new RequestParams(PBS_CONFIG_ID_NATIVE_APPNEXUS, AdType.NATIVE, null, new HashMap<String, Set<String>>(), new HashSet<String>(), null, null, null, null);
        requestParams.setNativeRequestParams(new NativeRequestParams());
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
        bgLooper.runOneTask();
        bgLooper.runOneTask();

        runAllMainThreadExecutorTasks();

        Robolectric.getBackgroundThreadScheduler().runOneTask();
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        String cacheId = "";
        try {
            Class clazz = Class.forName(CacheManager.class.getName());
            Field field = clazz.getDeclaredField("savedValues");
            field.setAccessible(true);
            HashMap<String, String> map = (HashMap<String, String>) field.get(clazz);
            for (String key : map.keySet()) {
                cacheId = key;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        HashMap<String, String> bids = new HashMap<String, String>();
        bids.put("hb_bidder", "appnexus");
        bids.put("hb_bidder_appnexus", "appnexus");
        bids.put("hb_cache_id", "dfad62f2-8ed5-4cf8-a597-f702147b7c98");
        bids.put("hb_cache_id_appnexus", "dfad62f2-8ed5-4cf8-a597-f702147b7c98");
        bids.put("hb_env", "mobile-app");
        bids.put("hb_env_appnexus", "mobile-app");
        bids.put("hb_pb", "0.00");
        bids.put("hb_pb_appnexus", "0.00");
        bids.put("hb_cache_host_appnex", "prebid.sin3.adnxs.com");
        bids.put("hb_cache_path_appnex", "/pbc/v1/cache");
        bids.put("hb_cache_path", "/pbc/v1/cache");
        bids.put("hb_cache_host", "prebid.sin3.adnxs.com");
        bids.put("hb_cache_id_local", cacheId);
        verify(mockListener).onDemandReady(bids, uuid);

        // Test findNative on responded cacheId local
        NativeCustomTemplateAd nativeCustomTemplateAd = Mockito.mock(NativeCustomTemplateAd.class);
        Mockito.when(nativeCustomTemplateAd.getText("isPrebid")).thenReturn("1");
        Mockito.when(nativeCustomTemplateAd.getText("hb_cache_id_local")).thenReturn(cacheId);
        AdViewUtils.findNative(nativeCustomTemplateAd, new PrebidNativeAdListener() {
            @Override
            public void onPrebidNativeLoaded(PrebidNativeAd ad) {
                assertTrue(ad != null);
            }

            @Override
            public void onPrebidNativeNotFound() {
                fail();
            }

            @Override
            public void onPrebidNativeNotValid() {
                fail();
            }
        });

    }

    @Test
    public void testSuccessfulPrebidNativeAdExpiry() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.validResponsePrebidNativeNative()));
        HttpUrl hostUrl = server.url("/");
        Host.CUSTOM.setHostUrl(hostUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setPrebidServerAccountId(PBS_ACCOUNT_ID_APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        RequestParams requestParams = new RequestParams(PBS_CONFIG_ID_NATIVE_APPNEXUS, AdType.NATIVE, null, new HashMap<String, Set<String>>(), new HashSet<String>(), null, null, null, null);
        requestParams.setNativeRequestParams(new NativeRequestParams());
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
        bgLooper.runOneTask();
        bgLooper.runOneTask();

        runAllMainThreadExecutorTasks();

        Robolectric.getBackgroundThreadScheduler().runOneTask();
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        String cacheId = "";
        try {
            Class clazz = Class.forName(CacheManager.class.getName());
            Field field = clazz.getDeclaredField("savedValues");
            field.setAccessible(true);
            HashMap<String, String> map = (HashMap<String, String>) field.get(clazz);
            for (String key : map.keySet()) {
                cacheId = key;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        HashMap<String, String> bids = new HashMap<String, String>();
        bids.put("hb_bidder", "appnexus");
        bids.put("hb_bidder_appnexus", "appnexus");
        bids.put("hb_cache_id", "dfad62f2-8ed5-4cf8-a597-f702147b7c98");
        bids.put("hb_cache_id_appnexus", "dfad62f2-8ed5-4cf8-a597-f702147b7c98");
        bids.put("hb_env", "mobile-app");
        bids.put("hb_env_appnexus", "mobile-app");
        bids.put("hb_pb", "0.00");
        bids.put("hb_pb_appnexus", "0.00");
        bids.put("hb_cache_host_appnex", "prebid.sin3.adnxs.com");
        bids.put("hb_cache_path_appnex", "/pbc/v1/cache");
        bids.put("hb_cache_path", "/pbc/v1/cache");
        bids.put("hb_cache_host", "prebid.sin3.adnxs.com");
        bids.put("hb_cache_id_local", cacheId);
        verify(mockListener).onDemandReady(bids, uuid);

        // Test findNative on responded cacheId local
        NativeCustomTemplateAd nativeCustomTemplateAd = Mockito.mock(NativeCustomTemplateAd.class);
        Mockito.when(nativeCustomTemplateAd.getText("isPrebid")).thenReturn("1");
        Mockito.when(nativeCustomTemplateAd.getText("hb_cache_id_local")).thenReturn(cacheId);
        AdViewUtils.findNative(nativeCustomTemplateAd, new PrebidNativeAdListener() {
            @Override
            public void onPrebidNativeLoaded(PrebidNativeAd ad) {
                assertTrue(ad != null);
                View view = new View(activity);
                view.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
                ad.registerPrebidNativeAdEventListener(new PrebidNativeAdEventListener() {
                    @Override
                    public void onAdClicked() {
                        fail();
                    }

                    @Override
                    public void onAdImpression() {
                        fail();
                    }

                    @Override
                    public void onAdExpired() {
                        adExpired = true;
                    }
                });
                assertFalse(adExpired);
                Robolectric.getBackgroundThreadScheduler().advanceBy(302, TimeUnit.SECONDS);
                Robolectric.getForegroundThreadScheduler().advanceBy(302, TimeUnit.SECONDS);
                assertTrue(adExpired);
            }

            @Override
            public void onPrebidNativeNotFound() {
                fail();
            }

            @Override
            public void onPrebidNativeNotValid() {
                fail();
            }
        });
    }

    @Test
    public void testSuccessfulPrebidNativeAdClick() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.validResponsePrebidNativeNative()));
        HttpUrl hostUrl = server.url("/");
        Host.CUSTOM.setHostUrl(hostUrl.toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setPrebidServerAccountId(PBS_ACCOUNT_ID_APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        DemandAdapter.DemandAdapterListener mockListener = mock(DemandAdapter.DemandAdapterListener.class);
        PrebidServerAdapter adapter = new PrebidServerAdapter();
        RequestParams requestParams = new RequestParams(PBS_CONFIG_ID_NATIVE_APPNEXUS, AdType.NATIVE, null, new HashMap<String, Set<String>>(), new HashSet<String>(), null, null, null, null);
        requestParams.setNativeRequestParams(new NativeRequestParams());
        String uuid = UUID.randomUUID().toString();
        adapter.requestDemand(requestParams, mockListener, uuid);
        ShadowLooper bgLooper = Shadows.shadowOf(((BackgroundThreadExecutor) TasksManager.getInstance().backgroundThreadExecutor).getBackgroundHandler().getLooper());
        bgLooper.runOneTask();
        bgLooper.runOneTask();

        runAllMainThreadExecutorTasks();

        Robolectric.getBackgroundThreadScheduler().runOneTask();
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        String cacheId = "";
        try {
            Class clazz = Class.forName(CacheManager.class.getName());
            Field field = clazz.getDeclaredField("savedValues");
            field.setAccessible(true);
            HashMap<String, String> map = (HashMap<String, String>) field.get(clazz);
            for (String key : map.keySet()) {
                cacheId = key;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        HashMap<String, String> bids = new HashMap<String, String>();
        bids.put("hb_bidder", "appnexus");
        bids.put("hb_bidder_appnexus", "appnexus");
        bids.put("hb_cache_id", "dfad62f2-8ed5-4cf8-a597-f702147b7c98");
        bids.put("hb_cache_id_appnexus", "dfad62f2-8ed5-4cf8-a597-f702147b7c98");
        bids.put("hb_env", "mobile-app");
        bids.put("hb_env_appnexus", "mobile-app");
        bids.put("hb_pb", "0.00");
        bids.put("hb_pb_appnexus", "0.00");
        bids.put("hb_cache_host_appnex", "prebid.sin3.adnxs.com");
        bids.put("hb_cache_path_appnex", "/pbc/v1/cache");
        bids.put("hb_cache_path", "/pbc/v1/cache");
        bids.put("hb_cache_host", "prebid.sin3.adnxs.com");
        bids.put("hb_cache_id_local", cacheId);
        verify(mockListener).onDemandReady(bids, uuid);

        // Test findNative on responded cacheId local
        NativeCustomTemplateAd nativeCustomTemplateAd = Mockito.mock(NativeCustomTemplateAd.class);
        Mockito.when(nativeCustomTemplateAd.getText("isPrebid")).thenReturn("1");
        Mockito.when(nativeCustomTemplateAd.getText("hb_cache_id_local")).thenReturn(cacheId);
        AdViewUtils.findNative(nativeCustomTemplateAd, new PrebidNativeAdListener() {
            @Override
            public void onPrebidNativeLoaded(PrebidNativeAd ad) {
                assertTrue(ad != null);
                View view = new View(activity);
                view.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
                ad.registerView(view, new PrebidNativeAdEventListener() {
                    @Override
                    public void onAdClicked() {
                        adClicked = true;
                    }

                    @Override
                    public void onAdImpression() {
                        fail();
                    }

                    @Override
                    public void onAdExpired() {
                        fail();
                    }
                });
                activity.addContentView(view, new ViewGroup.LayoutParams(200, 200));
                view.setVisibility(View.VISIBLE);
                assertFalse(adClicked);
                view.performClick();
                assertTrue(adClicked);
            }

            @Override
            public void onPrebidNativeNotFound() {
                fail();
            }

            @Override
            public void onPrebidNativeNotValid() {
                fail();
            }
        });
    }

    private void runAllMainThreadExecutorTasks() {
        ShadowLooper mainLooper = Shadows.shadowOf(((MainThreadExecutor) TasksManager.getInstance().mainThreadExecutor)
                .getMainExecutor().getLooper());
        mainLooper.runToEndOfTasks();
    }
}
