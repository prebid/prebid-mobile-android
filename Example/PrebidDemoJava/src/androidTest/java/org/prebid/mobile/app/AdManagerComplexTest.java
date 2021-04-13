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

package org.prebid.mobile.app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.AdUnit;
import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.Host;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.OnCompleteListener2;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.addendum.PbFindSizeError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nullable;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class AdManagerComplexTest {
    @Rule
    public ActivityTestRule<TestActivity> testActivityRule = new ActivityTestRule<>(TestActivity.class);

    MockWebServer mockServer;

    @Before
    public void setUp() {
        mockServer = new MockWebServer();
        try {
            mockServer.start();
        } catch (IOException e) {
            fail("Mock server start failed.");
        }
    }

    @After
    public void tearDown() throws Exception {
        mockServer.shutdown();
        mockServer = null;
    }

    private interface FetchDemandCompleteListener {
        void preFetchDemand();
        void executeFetchDemand(AdUnit adUnit, List<Map<String, String>> resultList);
        void postFetchDemand(List<Map<String, String>> resultList);
    }

    @Test
    public void testFetchDemand() throws Exception {

        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();

        fetchDemandHelper(0, new FetchDemandCompleteListener() {

            @Override
            public void preFetchDemand() {

            }

            @Override
            public void executeFetchDemand(AdUnit adUnit, final List<Map<String, String>> resultList) {
                adUnit.fetchDemand(builder, new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {

                        Bundle customTargetingBundle = builder.build().getCustomTargeting();

                        Map<String, String> map = new HashMap<>(customTargetingBundle.keySet().size());
                        for (String key : customTargetingBundle.keySet()) {
                            map.put(key, customTargetingBundle.get(key).toString());
                        }

                        resultList.add(map);

                    }
                });
            }

            @Override
            public void postFetchDemand(List<Map<String, String>> resultList) {

            }
        });
    }

    @Test
    public void testFetchDemandBids() throws Exception {

        fetchDemandHelper(0, new FetchDemandCompleteListener() {

            @Override
            public void preFetchDemand() {

            }

            @Override
            public void executeFetchDemand(AdUnit adUnit, final List<Map<String, String>> resultList) {
                adUnit.fetchDemand(new OnCompleteListener2() {
                    @Override
                    public void onComplete(ResultCode resultCode, Map<String, String> unmodifiableMap) {
                        resultList.add(unmodifiableMap);
                    }
                });
            }

            @Override
            public void postFetchDemand(List<Map<String, String>> resultList) {

            }
        });

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFetchDemandBidsUnmodifiable() throws Exception {

        fetchDemandHelper(0, new FetchDemandCompleteListener() {

            @Override
            public void preFetchDemand() {

            }

            @Override
            public void executeFetchDemand(AdUnit adUnit, final List<Map<String, String>> resultList) {
                adUnit.fetchDemand(new OnCompleteListener2() {
                    @Override
                    public void onComplete(ResultCode resultCode, Map<String, String> unmodifiableMap) {
                        resultList.add(unmodifiableMap);
                    }
                });
            }

            @Override
            public void postFetchDemand(List<Map<String, String>> resultList) {
                resultList.get(0).put("someKey", "someValue");
            }
        });

    }

    @Test
    public void testFetchDemandAutoRefresh() throws Exception {

        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();
        final IntegerWrapper requestCountWrapper = new IntegerWrapper();

        fetchDemandHelper(31_000, new FetchDemandCompleteListener() {
            @Override
            public void preFetchDemand() {
                mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\n" +
                        "  \"seatbid\": [\n" +
                        "    {\n" +
                        "      \"bid\": [\n" +
                        "        {\n" +
                        "          \"ext\": {\n" +
                        "            \"prebid\": {\n" +
                        "              \"targeting\": {\n" +
                        "                \"hb_cache_id\": \"top_bid_1\",\n" +
                        "                \"key1\": \"value1\"\n" +
                        "              }\n" +
                        "            }\n" +
                        "          }\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"));
            }

            @Override
            public void executeFetchDemand(AdUnit adUnit, final List<Map<String, String>> resultList) {
                adUnit.fetchDemand(builder, new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {

                        requestCountWrapper.value++;

                        AdManagerAdRequest adManagerAdRequest = builder.build();
                        Bundle customTargetingBundle = adManagerAdRequest.getCustomTargeting();

                        Map<String, String> map = new HashMap<>(customTargetingBundle.keySet().size());
                        for (String key : customTargetingBundle.keySet()) {
                            map.put(key, customTargetingBundle.get(key).toString());
                        }

                        resultList.add(map);
                    }
                });
            }

            @Override
            public void postFetchDemand(List<Map<String, String>> resultList) {
                Map<String, String> result = resultList.get(1);

                assertEquals("top_bid_1", result.get("hb_cache_id"));
                assertEquals("value1", result.get("key1"));
            }
        });

        assertEquals(2, requestCountWrapper.value);
    }

    @Test
    public void testFetchDemandBidsAutoRefresh() throws Exception {
        final IntegerWrapper requestCountWrapper = new IntegerWrapper();

        fetchDemandHelper(31_000, new FetchDemandCompleteListener() {
            @Override
            public void preFetchDemand() {
                mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\n" +
                        "  \"seatbid\": [\n" +
                        "    {\n" +
                        "      \"bid\": [\n" +
                        "        {\n" +
                        "          \"ext\": {\n" +
                        "            \"prebid\": {\n" +
                        "              \"targeting\": {\n" +
                        "                \"hb_cache_id\": \"top_bid_1\",\n" +
                        "                \"key1\": \"value1\"\n" +
                        "              }\n" +
                        "            }\n" +
                        "          }\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"));
            }

            @Override
            public void executeFetchDemand(AdUnit adUnit, final List<Map<String, String>> resultList) {

                adUnit.fetchDemand(new OnCompleteListener2() {
                    @Override
                    public void onComplete(ResultCode resultCode, Map<String, String> unmodifiableMap) {
                        requestCountWrapper.value++;

                        resultList.add(new HashMap(unmodifiableMap));
                    }
                });
            }

            @Override
            public void postFetchDemand(List<Map<String, String>> resultList) {
                Map<String, String> result = resultList.get(1);

                assertEquals("top_bid_1", result.get("hb_cache_id"));
                assertEquals("value1", result.get("key1"));
            }
        });

        assertEquals(2, requestCountWrapper.value);
    }

    public void fetchDemandHelper(int autoRefreshMillis, FetchDemandCompleteListener listener) throws Exception {
        //given
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        HttpUrl httpUrl = mockServer.url("/fetchDemandHelper");
        Host.CUSTOM.setHostUrl(httpUrl.toString());
        PrebidMobile.setPrebidServerAccountId("1001");
        mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\n" +
                "  \"seatbid\": [\n" +
                "    {\n" +
                "      \"bid\": [\n" +
                "        {\n" +
                "          \"ext\": {\n" +
                "            \"prebid\": {\n" +
                "              \"targeting\": {\n" +
                "                \"hb_env\": \"mobile-app\",\n" +
                "                \"hb_cache_hostpath\": \"https://prebid-cache-europe.rubiconproject.com/cache\",\n" +
                "                \"hb_size_rubicon\": \"300x250\",\n" +
                "                \"hb_cache_id\": \"a2f41588-4727-425c-9ef0-3b382debef1e\",\n" +
                "                \"hb_cache_path_rubicon\": \"/cache\",\n" +
                "                \"hb_cache_host_rubicon\": \"prebid-cache-europe.rubiconproject.com\",\n" +
                "                \"hb_pb\": \"1.20\",\n" +
                "                \"hb_pb_rubicon\": \"1.20\",\n" +
                "                \"hb_cache_id_rubicon\": \"a2f41588-4727-425c-9ef0-3b382debef1e\",\n" +
                "                \"hb_cache_path\": \"/cache\",\n" +
                "                \"hb_size\": \"300x250\",\n" +
                "                \"hb_cache_hostpath_rubicon\": \"https://prebid-cache-europe.rubiconproject.com/cache\",\n" +
                "                \"hb_env_rubicon\": \"mobile-app\",\n" +
                "                \"hb_bidder\": \"rubicon\",\n" +
                "                \"hb_bidder_rubicon\": \"rubicon\",\n" +
                "                \"hb_cache_host\": \"prebid-cache-europe.rubiconproject.com\"\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}"));

        listener.preFetchDemand();

        final List<Map<String, String>> resultList = new ArrayList<>();

        //when
        AdUnit adUnit = new BannerAdUnit("1001-1", 300, 250);
        adUnit.setAutoRefreshPeriodMillis(autoRefreshMillis);

        listener.executeFetchDemand(adUnit, resultList);

        try {
            Thread.sleep(autoRefreshMillis + 1_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //then
        Map<String, String> map = resultList.get(0);

        assertEquals("mobile-app", map.get("hb_env"));
        assertEquals("https://prebid-cache-europe.rubiconproject.com/cache", map.get("hb_cache_hostpath"));
        assertEquals("300x250", map.get("hb_size_rubicon"));
        assertEquals("a2f41588-4727-425c-9ef0-3b382debef1e", map.get("hb_cache_id"));
        assertEquals("/cache", map.get("hb_cache_path_rubicon"));
        assertEquals("prebid-cache-europe.rubiconproject.com", map.get("hb_cache_host_rubicon"));
        assertEquals("1.20", map.get("hb_pb"));
        assertEquals("1.20", map.get("hb_pb_rubicon"));
        assertEquals("a2f41588-4727-425c-9ef0-3b382debef1e", map.get("hb_cache_id_rubicon"));
        assertEquals("/cache", map.get("hb_cache_path"));
        assertEquals("300x250", map.get("hb_size"));
        assertEquals("https://prebid-cache-europe.rubiconproject.com/cache", map.get("hb_cache_hostpath_rubicon"));
        assertEquals("mobile-app", map.get("hb_env_rubicon"));
        assertEquals("rubicon", map.get("hb_bidder"));
        assertEquals("rubicon", map.get("hb_bidder_rubicon"));
        assertEquals("prebid-cache-europe.rubiconproject.com", map.get("hb_cache_host"));

        listener.postFetchDemand(resultList);
    }

    @Test
    public void testAdManagerAdRequest() throws Exception {
        AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();
        builder.addCustomTargeting("key1", "value1");
        builder.addCustomTargeting("key2", "value2");

        AdManagerAdRequest adManagerAdRequest1 = builder.build();
        Bundle bundle1 = adManagerAdRequest1.getCustomTargeting();

        assertEquals(2, bundle1.keySet().size());
        assertEquals("value1", bundle1.getString("key1"));
        assertEquals("value2", bundle1.getString("key2"));

        bundle1.remove("key2");
        assertEquals(1, bundle1.keySet().size());
        assertEquals("value1", bundle1.getString("key1"));

        AdManagerAdRequest AdManagerAdRequest2 = builder.build();
        Bundle bundle2 = AdManagerAdRequest2.getCustomTargeting();
        assertEquals(1, bundle2.keySet().size());
        assertEquals("value1", bundle2.getString("key1"));

    }

    @Test
    public void testAdManagerAdRequestBuilderUseCase() throws Exception {
        //given

        mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\n" +
                "  \"seatbid\": [\n" +
                "    {\n" +
                "      \"bid\": [\n" +
                "        {\n" +
                "          \"ext\": {\n" +
                "            \"prebid\": {\n" +
                "              \"targeting\": {\n" +
                "                \"hb_cache_id\": \"top_bid_1\",\n" +
                "                \"key1\": \"value1\"\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}"));


        //important line
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);

        HttpUrl httpUrl = mockServer.url("/testAdManagerAdRequestBuilderUseCase");
        Host.CUSTOM.setHostUrl(httpUrl.toString());
        PrebidMobile.setPrebidServerAccountId("1001");

        final ReferenceWrapper<Bundle> customTargetingBundleWrapper1 = new ReferenceWrapper<>();

        //when
        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();
        AdUnit adUnit = new BannerAdUnit("1001-1", 300, 250);

        adUnit.fetchDemand(builder, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {

                builder.addCustomTargeting("key2", "value2");
                AdManagerAdRequest adManagerAdRequest = builder.build();
                Bundle customTargetingBundle = adManagerAdRequest.getCustomTargeting();

                customTargetingBundleWrapper1.value = (Bundle)customTargetingBundle.clone();

            }
        });

        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //then
        Bundle customTargetingBundle = customTargetingBundleWrapper1.value;

        assertEquals(3, customTargetingBundle.keySet().size());
        assertEquals("top_bid_1", customTargetingBundle.getString("hb_cache_id"));
        assertEquals("value1", customTargetingBundle.getString("key1"));
        assertEquals("value2", customTargetingBundle.getString("key2"));

    }

    //30x250 -> 728x90
    @Test
    public void testRubiconDFPBannerResizeSanityAppCheckTest() throws Exception {

        final CountDownLatch lock = new CountDownLatch(1);

        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID_RUBICON);
        PrebidMobile.setStoredAuctionResponse("1001-rubicon-300x250");

        TestActivity activity = testActivityRule.getActivity();

        final IntegerWrapper firstTransactionCount = new IntegerWrapper();
        final IntegerWrapper secondTransactionCount = new IntegerWrapper();

        final int transactionFailRepeatCount = 5;
        final int screenshotDelayMillis = 3_000;
        final int transactionFailDelayMillis = 3_000;

        final FrameLayout adFrame = activity.findViewById(R.id.adFrame);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adFrame.removeAllViews();
            }
        });

        final AdManagerAdView dfpAdView = new AdManagerAdView(activity);
        //Programmatic fix
        dfpAdView.setAdUnitId("/5300653/test_adunit_pavliuchyk_300x250_puc_ucTagData_prebid-server.rubiconproject.com");
        dfpAdView.setAdSizes(new AdSize(300, 250));

        //Targeting creative
        /*
        dfpAdView.setAdUnitId("/5300653/Banner_PUC_b397711");
        dfpAdView.setAdSizes(new AdSize(300, 250), new AdSize(728, 90));
        */

        final BannerAdUnit bannerAdUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250_RUBICON, 300, 250);

        final AdManagerAdRequest request = new AdManagerAdRequest.Builder().build();
        final OnCompleteListener completeListener = new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                dfpAdView.loadAd(request);
            }
        };

        dfpAdView.setAdListener(new AdListener() {

            private void notifyResult() {
                lock.countDown();
            }

            private void update(boolean isSuccess) {
                if (isSuccess) {

                    if (firstTransactionCount.value != -1) {
                        firstTransactionCount.value = -1;

                        PrebidMobile.setStoredAuctionResponse("1001-rubicon-300x50");

                        bannerAdUnit.fetchDemand(request, completeListener);
                    } else if (secondTransactionCount.value != -1) {
                        secondTransactionCount.value = -1;

                        notifyResult();
                    }

                } else {
                    try {
                        Thread.sleep(transactionFailDelayMillis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (firstTransactionCount.value != -1) {
                        if (firstTransactionCount.value > transactionFailRepeatCount -2) {
                            fail("first Transaction Count == " + transactionFailRepeatCount);
                        } else {
                            //repeat
                            firstTransactionCount.value++;
                            bannerAdUnit.fetchDemand(request, completeListener);
                        }
                    } else if (secondTransactionCount.value != -1) {
                        if (secondTransactionCount.value > transactionFailRepeatCount -2) {
                            fail("second Transaction Count == " + transactionFailRepeatCount);
                        } else {
                            //repeat
                            secondTransactionCount.value++;
                            bannerAdUnit.fetchDemand(request, completeListener);
                        }
                    } else {
                        fail("Unexpected");
                    }

                }

            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                //Programmatic fix
                AdViewUtils.findPrebidCreativeSize(dfpAdView, new AdViewUtils.PbFindSizeListener() {
                    @Override
                    public void success(final int width, final int height) {
                        dfpAdView.setAdSizes(new AdSize(width, height));

                        final View child = dfpAdView.getChildAt(0);
                        child.setBackgroundColor(Color.RED);

                        dfpAdView.post(new Runnable() {
                            @Override
                            public void run() {

                                float density = dfpAdView.getResources().getDisplayMetrics().density;
                                double dpW = Math.ceil(child.getMinimumWidth() / density);
                                double dpH = Math.ceil(child.getMinimumHeight() / density);

                                try {
                                    Thread.sleep(screenshotDelayMillis);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                assertEquals((int)dpW, width);
                                assertEquals((int)dpH, height);

                                update(true);

                            }
                        });
                    }

                    @Override
                    public void failure(PbFindSizeError error) {
                        LogUtil.w("failure:" + error.getDescription());
                        update(false);
                    }
                });

                //Targeting creative
                /*
                try {
                    Thread.sleep(screenshotDelayMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                update(true);
                */
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                LogUtil.w("onAdFailedToLoad:" + loadAdError);

                update(false);
            }
        });

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adFrame.addView(dfpAdView);
            }
        });

        bannerAdUnit.fetchDemand(request, completeListener);

        //TravisCI fix
        Thread.sleep(2 * transactionFailRepeatCount * transactionFailDelayMillis + 2 * screenshotDelayMillis);
        //local test
//        lock.await();

        try {
            Thread.sleep(screenshotDelayMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(-1, firstTransactionCount.value);
        assertEquals(-1, secondTransactionCount.value);

    }

    private static class ReferenceWrapper<T> {
        @Nullable
        T value = null;
    }

    private static class IntegerWrapper {
        int value = 0;
    }

}
