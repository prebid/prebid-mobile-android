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

import android.os.Handler;
import android.os.Looper;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.prebid.mobile.AdUnit;
import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.Host;
import org.prebid.mobile.InterstitialAdUnit;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.TargetingParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import androidx.test.espresso.Espresso;
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
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webContent;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static androidx.test.espresso.web.matcher.DomMatchers.containingTextInBody;
import static androidx.test.espresso.web.model.Atoms.getCurrentUrl;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class ExtraTests {

    @Rule
    public ActivityTestRule<TestActivity> m = new ActivityTestRule<>(TestActivity.class);
    @Rule
    public GrantPermissionRule mGrant = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    MockWebServer server;
    Handler mHandler;

    @Before
    public void setUp() {
        mHandler = new Handler(Looper.getMainLooper());
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
    public void testMultipleDemand() throws Exception {
        final ArrayList<AdUnit> adUnits = new ArrayList<AdUnit>();
        final ArrayList<OnCompleteListener> spies = new ArrayList<>();
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                adObject.setMinimumWidth(300);
                adObject.setMinimumHeight(250);
                BannerAdUnit adUnit = new BannerAdUnit("1cfdfe39-18f2-45b9-964f-63d64cdc0399", 300, 250);
                adUnits.add(adUnit);
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        String keywords = adObject.getKeywords();
                        String[] keywordsArray = keywords.split(",");
                        assertEquals(15, keywordsArray.length);
                        assertTrue(keywords.contains("hb_pb:1.20"));
                        assertTrue(keywords.contains("hb_pb_rubicon:1.20"));
                        assertTrue(keywords.contains("hb_cache_id:"));
                        assertTrue(keywords.contains("hb_pb_appnexus:0.50"));
                        assertTrue(keywords.contains("hb_cache_id_appnexus:"));
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                l = spy(l);
                spies.add(l);
                adUnit.fetchDemand(adObject, l);
            }
        });
        Thread.sleep(10000);
        verify(spies.get(0), times(1)).onComplete(ResultCode.SUCCESS);
        onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
    }

    @Test
    public void testSameConfigIdOnDifferentAdObjects() throws Exception {
        final ArrayList<AdUnit> adUnits = new ArrayList<AdUnit>();
        final ArrayList<OnCompleteListener> spies = new ArrayList<>();
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject1 = new MoPubView(m.getActivity());
                adObject1.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_320x50_APPNEXUS);
                adObject1.setMinimumWidth(320);
                adObject1.setMinimumHeight(50);
                BannerAdUnit adUnit1 = new BannerAdUnit(Constants.PBS_CONFIG_ID_320x50_APPNEXUS, 320, 50);
                adUnits.add(adUnit1);
                OnCompleteListener l1 = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        assertTrue(adObject1.getKeywords().contains("hb_pb:0.50"));
                        assertTrue(adObject1.getKeywords().contains("hb_cache_id:"));
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject1);
                        adObject1.loadAd();
                    }
                };
                l1 = spy(l1);
                spies.add(l1);
                adUnit1.fetchDemand(adObject1, l1);
                PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
                final PublisherAdRequest adObject2 = builder.build();
                BannerAdUnit adUnit2 = new BannerAdUnit(Constants.PBS_CONFIG_ID_320x50_APPNEXUS, 320, 50);
                adUnits.add(adUnit2);
                OnCompleteListener l2 = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        assertEquals(10, adObject2.getCustomTargeting().size());
                        assertTrue(adObject2.getCustomTargeting().keySet().contains("hb_pb"));
                        assertTrue(adObject2.getCustomTargeting().keySet().contains("hb_cache_id"));
                        assertEquals("0.50", adObject2.getCustomTargeting().getString("hb_pb"));
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        PublisherAdView adView = new PublisherAdView(m.getActivity());
                        adView.setAdSizes(AdSize.BANNER);
                        adView.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_ALL_SIZES_APPNEXUS);
                        adFrame.addView(adView);
                        adView.setId(1);
                        adView.loadAd(adObject2);
                    }
                };
                l2 = spy(l2);
                spies.add(l2);
                adUnit1.fetchDemand(adObject2, l2);
            }
        });
        Thread.sleep(10000);
        verify(spies.get(0), times(1)).onComplete(ResultCode.SUCCESS);
        verify(spies.get(1), times(1)).onComplete(ResultCode.SUCCESS);
    }

    @Test
    public void testMultipleAdUnitsAllDemandFetched() throws Exception {
        final ArrayList<AdUnit> adUnits = new ArrayList<AdUnit>();
        final ArrayList<OnCompleteListener> spies = new ArrayList<>();
        final ArrayList<MoPubInterstitial> moPubInterstitials = new ArrayList<>();
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // test 0 set up Banner
                final MoPubView mopubTest1 = new MoPubView(m.getActivity());
                mopubTest1.setAdUnitId("9a8c2ccd3dae405bb925397d35eed8f9");
                mopubTest1.setMinimumHeight(50);
                mopubTest1.setMinimumWidth(320);
                BannerAdUnit adUnit1 = new BannerAdUnit("7cd2c7c8-cebe-4206-b5a4-97b9e840729e", 320, 50);
                adUnits.add(adUnit1);
                OnCompleteListener listener1 = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(mopubTest1);
                        mopubTest1.setId(1);
                        mopubTest1.loadAd();
                    }
                };
                listener1 = spy(listener1);
                spies.add(listener1);
                adUnit1.fetchDemand(mopubTest1, listener1);
                // test 1 set up Banner
                final MoPubView mopubTest2 = new MoPubView(m.getActivity());
                mopubTest2.setAdUnitId("50564379db734ebbb347849221a1081e");
                mopubTest2.setMinimumHeight(50);
                mopubTest2.setMinimumWidth(320);
                BannerAdUnit adUnit2 = new BannerAdUnit("525a5fee-ffbb-4f16-935d-3717c56e7aeb", 320, 50);
                adUnits.add(adUnit2);
                OnCompleteListener listener2 = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(mopubTest2);
                        mopubTest2.setId(2);
                        mopubTest2.loadAd();
                    }
                };
                listener2 = spy(listener2);
                spies.add(listener2);
                adUnit2.fetchDemand(mopubTest2, listener2);
                // test 2 set up Banner
                final MoPubView mopubTest3 = new MoPubView(m.getActivity());
                mopubTest3.setAdUnitId("5ff9556b05964e65b684ec54013df59d");
                mopubTest2.setMinimumHeight(250);
                mopubTest2.setMinimumWidth(300);
                BannerAdUnit adUnit3 = new BannerAdUnit("511c39f2-b527-41af-811a-adac6911bdfc", 300, 250);
                adUnits.add(adUnit3);
                OnCompleteListener listener3 = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(mopubTest3);
                        mopubTest3.setId(3);
                        mopubTest3.loadAd();
                    }
                };
                listener3 = spy(listener3);
                spies.add(listener3);
                adUnit3.fetchDemand(mopubTest3, listener3);
                // test 3 set up Banner
                final MoPubView mopubTest4 = new MoPubView(m.getActivity());
                mopubTest4.setAdUnitId("c5c9267bcf6247cb91a116d1ef6c7487");
                mopubTest4.setMinimumHeight(250);
                mopubTest4.setMinimumWidth(300);
                BannerAdUnit adUnit4 = new BannerAdUnit("42ad4418-9b36-4e39-ae54-2f7a13ad8616", 300, 250);
                adUnits.add(adUnit4);
                OnCompleteListener listener4 = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(mopubTest4);
                        mopubTest4.setId(4);
                        mopubTest4.loadAd();
                    }
                };
                listener4 = spy(listener4);
                spies.add(listener4);
                adUnit4.fetchDemand(mopubTest4, listener4);
                // test 4 set up Banner
                final MoPubView mopubTest5 = new MoPubView(m.getActivity());
                mopubTest5.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                mopubTest5.setMinimumHeight(250);
                mopubTest5.setMinimumWidth(300);
                BannerAdUnit adUnit5 = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250_APPNEXUS, 300, 250);
                adUnits.add(adUnit5);
                OnCompleteListener listener5 = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(mopubTest5);
                        mopubTest5.setId(5);
                        mopubTest5.loadAd();
                    }
                };
                listener5 = spy(listener5);
                spies.add(listener5);
                adUnit4.fetchDemand(mopubTest5, listener5);
                // test 5 set up Interstitial
                final MoPubInterstitial mopubInstl1 = new MoPubInterstitial(m.getActivity(), Constants.MOPUB_INTERSTITIAL_ADUNIT_ID_APPNEXUS);
                moPubInterstitials.add(mopubInstl1);
                InterstitialAdUnit adUnit6 = new InterstitialAdUnit(Constants.PBS_CONFIG_ID_INTERSTITIAL_APPNEXUS);
                adUnits.add(adUnit6);
                OnCompleteListener listener6 = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        mopubInstl1.load();
                    }
                };
                listener6 = spy(listener6);
                spies.add(listener6);
                adUnit6.fetchDemand(mopubInstl1, listener6);
                // test 6 set up Interstitial
                final MoPubInterstitial mopubInstl2 = new MoPubInterstitial(m.getActivity(), "c3fca03154a540bfa7f0971fb984e3e8");
                moPubInterstitials.add(mopubInstl2);
                InterstitialAdUnit adUnit7 = new InterstitialAdUnit("bde00f49-0a1b-483a-9716-e2dd427b794c");
                adUnits.add(adUnit7);
                OnCompleteListener listener7 = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        mopubInstl2.load();
                    }
                };
                listener7 = spy(listener7);
                spies.add(listener7);
                adUnit7.fetchDemand(mopubInstl2, listener7);
                // test 7 set up Interstitial
                final MoPubInterstitial mopubInstl3 = new MoPubInterstitial(m.getActivity(), "12ecf78eb8314f8bb36192a6286adc56");
                moPubInterstitials.add(mopubInstl3);
                InterstitialAdUnit adUnit8 = new InterstitialAdUnit("6ceca3d4-f5b8-4717-b4d9-178843f873f8");
                adUnits.add(adUnit8);
                OnCompleteListener listener8 = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        mopubInstl3.load();
                    }
                };
                listener8 = spy(listener8);
                spies.add(listener8);
                adUnit8.fetchDemand(mopubInstl3, listener8);
            }
        });
        Thread.sleep(10000);
        // verify line by line for easier debug
        ArgumentCaptor<ResultCode> resultCode = ArgumentCaptor.forClass(ResultCode.class);
        verify(spies.get(0), times(1)).onComplete(resultCode.capture());
        if (ResultCode.SUCCESS.equals(resultCode.getValue())) {
            onWebView(withParent(withId(1))).check(webContent(containingTextInBody("143135824")));
        } else {
            LogUtil.e("MultipleAdUnits", "For test 0, actual result code is " + resultCode.getValue());
        }
        ArgumentCaptor<ResultCode> resultCode1 = ArgumentCaptor.forClass(ResultCode.class);
        verify(spies.get(1), times(1)).onComplete(resultCode1.capture());
        if (ResultCode.SUCCESS.equals(resultCode1.getValue())) {
            onWebView(withParent(withId(2))).check(webContent(containingTextInBody("143208584")));
        } else {
            LogUtil.e("MultipleAdUnits", "For test 1, actual result code is " + resultCode1.getValue());
        }
        ArgumentCaptor<ResultCode> resultCode2 = ArgumentCaptor.forClass(ResultCode.class);
        verify(spies.get(2), times(1)).onComplete(resultCode2.capture());
        if (ResultCode.SUCCESS.equals(resultCode2.getValue())) {
            onWebView(withParent(withId(3))).check(webContent(containingTextInBody("143208598")));
        } else {
            LogUtil.e("MultipleAdUnits", "For test 2, actual result code is " + resultCode2.getValue());
        }
        ArgumentCaptor<ResultCode> resultCode3 = ArgumentCaptor.forClass(ResultCode.class);
        verify(spies.get(3), times(1)).onComplete(resultCode3.capture());
        if (ResultCode.SUCCESS.equals(resultCode3.getValue())) {
            onWebView(withParent(withId(4))).check(webContent(containingTextInBody("143208640")));
        } else {
            LogUtil.e("MultipleAdUnits", "For test 3, actual result code is " + resultCode3.getValue());
        }
        ArgumentCaptor<ResultCode> resultCode4 = ArgumentCaptor.forClass(ResultCode.class);
        verify(spies.get(4), times(1)).onComplete(resultCode4.capture());
        if (ResultCode.SUCCESS.equals(resultCode4.getValue())) {
            onWebView(withParent(withId(5))).check(webContent(containingTextInBody("ucTag.renderAd")));
        } else {
            LogUtil.e("MultipleAdUnits", "For test 4, actual result code is " + resultCode4.getValue());
        }
        ArgumentCaptor<ResultCode> resultCode5 = ArgumentCaptor.forClass(ResultCode.class);
        verify(spies.get(5), times(1)).onComplete(resultCode5.capture());
        if (ResultCode.SUCCESS.equals(resultCode5.getValue())) {
            moPubInterstitials.get(0).show();
            Thread.sleep(2000);
            assertEquals("com.mopub.mobileads.MoPubActivity", TestUtil.getCurrentActivity().getClass().getName());
            onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
            Espresso.pressBack();
        } else {
            LogUtil.e("MultipleAdUnits", "For test 5, actual result code is " + resultCode5.getValue());
        }
        ArgumentCaptor<ResultCode> resultCode6 = ArgumentCaptor.forClass(ResultCode.class);
        verify(spies.get(6), times(1)).onComplete(resultCode6.capture());
        if (ResultCode.SUCCESS.equals(resultCode6.getValue())) {
            moPubInterstitials.get(1).show();
            Thread.sleep(2000);
            assertEquals("com.mopub.mobileads.MoPubActivity", TestUtil.getCurrentActivity().getClass().getName());
            onWebView().check(webContent(containingTextInBody("143393807")));
            Espresso.pressBack();
        } else {
            LogUtil.e("MultipleAdUnits", "For test 6, actual result code is " + resultCode6.getValue());
        }
        ArgumentCaptor<ResultCode> resultCode7 = ArgumentCaptor.forClass(ResultCode.class);
        verify(spies.get(7), times(1)).onComplete(resultCode7.capture());
        if (ResultCode.SUCCESS.equals(resultCode7.getValue())) {
            moPubInterstitials.get(2).show();
            Thread.sleep(5000);
            assertEquals("com.mopub.mobileads.MoPubActivity", TestUtil.getCurrentActivity().getClass().getName());
            onWebView().check(webContent(containingTextInBody("143393864")));
            Espresso.pressBack();
        } else {
            LogUtil.e("MultipleAdUnits", "For test 7, actual result code is " + resultCode7.getValue());
        }
    }

    @Test
    public void testRubiconDemand() throws Exception {
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("1001");
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setShareGeoLocation(true);
        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("1001-2", 300, 250);
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(10000);
        verify(listener[0], times(1)).onComplete(ResultCode.SUCCESS);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
    }

    @Test
    public void testAppNexusKeyValueTargeting() throws Exception {
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("67bac530-9832-4f78-8c94-fbf88ac7bd14", 300, 250);
                adUnit.addUserKeyword("pbm_key", "pbm_value1");
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(10000);
        verify(listener[0], times(1)).onComplete(ResultCode.SUCCESS);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
    }

    @Test
    public void testAppNexusKeyValueTargeting2() throws Exception {
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("67bac530-9832-4f78-8c94-fbf88ac7bd14", 300, 250);
                adUnit.addUserKeyword("pbm_key", "pbm_value2");
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(10000);
        verify(listener[0], times(1)).onComplete(ResultCode.NO_BIDS);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("Hello, I'm not a Prebid ad.")));
    }

    @Test
    public void testEmptyInvalidPrebidServerAccountId() throws Exception {
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("67bac530-9832-4f78-8c94-fbf88ac7bd14", 300, 250);
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(1000);
        verify(listener[0], times(1)).onComplete(ResultCode.INVALID_ACCOUNT_ID);
    }

    @Test
    public void testRubiconEmptyInvalidPrebidServerAccountId() throws Exception {
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("");
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setShareGeoLocation(true);

        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_RUBICON);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("1001-1", 300, 250);
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(2000);
        verify(listener[0], times(1)).onComplete(ResultCode.INVALID_ACCOUNT_ID);
    }

    @Test
    public void testAppNexusInvalidPrebidServerAccountId() throws Exception {
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-ffffffffffff");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("6ace8c7d-88c0-4623-8117-75bc3f0a2e45", 300, 250);
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(10000);
        verify(listener[0], times(1)).onComplete(ResultCode.INVALID_ACCOUNT_ID);
    }

    @Test
    public void testRubiconInvalidPrebidServerAccountId() throws Exception {
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("1001_ERROR");
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setShareGeoLocation(true);

        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_RUBICON);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("1001-1", 300, 250);
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(2000);
        verify(listener[0], times(1)).onComplete(ResultCode.INVALID_ACCOUNT_ID);
    }

    @Test
    public void testEmptyInvalidPrebidServerConfigId() throws Exception {
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("", 300, 250);
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(1000);
        verify(listener[0], times(1)).onComplete(ResultCode.INVALID_CONFIG_ID);
    }

    @Test
    public void testRubiconEmptyInvalidPrebidServerConfigId() throws Exception {
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("1001");
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setShareGeoLocation(true);

        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_RUBICON);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("", 300, 250);
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(2000);
        verify(listener[0], times(1)).onComplete(ResultCode.INVALID_CONFIG_ID);
    }

    @Test
    public void testAppNexusInvalidPrebidServerConfigId() throws Exception {
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("6ace8c7d-88c0-4623-8117-ffffffffffff", 300, 250);
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(10000);
        verify(listener[0], times(1)).onComplete(ResultCode.INVALID_CONFIG_ID);
    }

    @Test
    public void testRubiconInvalidPrebidServerConfigId() throws Exception {
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("1001");
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setShareGeoLocation(true);

        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_RUBICON);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("1001-1_ERROR", 300, 250);
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(2000);
        verify(listener[0], times(1)).onComplete(ResultCode.INVALID_CONFIG_ID);
    }

    // Passing year 1855 is invalid yob, should not send yob and get back a no bid
    @Test
    public void testAppNexusAgeTargeting1() throws Exception {
        boolean errorThrown = false;
        try {
            TargetingParams.setYearOfBirth(1855);
        } catch (Exception e) {
            errorThrown = true;
            assertEquals(0, TargetingParams.getYearOfBirth());
        }
        assertTrue(errorThrown);
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
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        Host.CUSTOM.setHostUrl(server.url("/").toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setShareGeoLocation(true);
        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("47706260-ee91-4cd7-b656-2185aca89f59", 300, 250);
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(10000);
        verify(listener[0], times(1)).onComplete(ResultCode.NO_BIDS);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("Hello, I'm not a Prebid ad.")));
    }

    // Passing year -1 is invalid yob, should not send yob and get back a no bid
    @Test
    public void testAppNexusAgeTargeting2() throws Exception {
        boolean errorThrown = false;
        try {
            TargetingParams.setYearOfBirth(-1);
        } catch (Exception e) {
            errorThrown = true;
            assertEquals(0, TargetingParams.getYearOfBirth());
        }
        assertTrue(errorThrown);
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
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        Host.CUSTOM.setHostUrl(server.url("/").toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setShareGeoLocation(true);
        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("47706260-ee91-4cd7-b656-2185aca89f59", 300, 250);
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(10000);
        verify(listener[0], times(1)).onComplete(ResultCode.NO_BIDS);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("Hello, I'm not a Prebid ad.")));
    }

    // Passing year 2018 is valid yob, should send yob and but get back a no bid, since campaign targeting 25-34
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
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        Host.CUSTOM.setHostUrl(server.url("/").toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setShareGeoLocation(true);
        TargetingParams.setYearOfBirth(2018);
        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("47706260-ee91-4cd7-b656-2185aca89f59", 300, 250);
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(10000);
        verify(listener[0], times(1)).onComplete(ResultCode.NO_BIDS);
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
        PrebidMobile.setApplicationContext(m.getActivity().getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        Host.CUSTOM.setHostUrl(server.url("/").toString());
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setShareGeoLocation(true);
        TargetingParams.setYearOfBirth(1989);
        final OnCompleteListener[] listener = new OnCompleteListener[1];
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MoPubView adObject = new MoPubView(m.getActivity());
                adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                adObject.setMinimumHeight(250);
                adObject.setMinimumWidth(300);
                BannerAdUnit adUnit = new BannerAdUnit("47706260-ee91-4cd7-b656-2185aca89f59", 300, 250);
                OnCompleteListener l = new OnCompleteListener() {
                    @Override
                    public void onComplete(ResultCode resultCode) {
                        FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                        adFrame.addView(adObject);
                        adObject.loadAd();
                    }
                };
                listener[0] = spy(l);
                adUnit.fetchDemand(adObject, listener[0]);
            }
        });
        Thread.sleep(10000);
        verify(listener[0], times(1)).onComplete(ResultCode.SUCCESS);
        onView(withId(R.id.adFrame)).check(matches(isDisplayed()));
        onWebView().check(webMatches(getCurrentUrl(), containsString("ads.mopub.com")));
        onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
    }

    // This test should be run with a real device located in New York City, New York, USA
    @Test
    public void testLocationTargeting() throws Exception {
        if (!TestUtil.isEmulator()) {
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
            final OnCompleteListener[] listener = new OnCompleteListener[2];
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final MoPubView adObject = new MoPubView(m.getActivity());
                    adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                    adObject.setMinimumHeight(250);
                    adObject.setMinimumWidth(300);
                    BannerAdUnit adUnit = new BannerAdUnit("8522cead-1eb4-4f09-b6e2-083fa3a6e6ce", 300, 250);
                    OnCompleteListener l = new OnCompleteListener() {
                        @Override
                        public void onComplete(ResultCode resultCode) {

                            FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                            adFrame.addView(adObject);
                            adObject.loadAd();
                        }
                    };
                    listener[0] = spy(l);
                    adUnit.fetchDemand(adObject, listener[0]);
                }
            });
            Thread.sleep(10000);
            verify(listener[0], times(1)).onComplete(ResultCode.SUCCESS);
            onWebView().check(webContent(containingTextInBody("ucTag.renderAd")));
            // Do not share location
            PrebidMobile.setShareGeoLocation(false);
            Host.CUSTOM.setHostUrl(server.url("/sharefakegeo").toString());
            PrebidMobile.setPrebidServerHost(Host.CUSTOM);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    final MoPubView adObject = new MoPubView(m.getActivity());
                    adObject.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);
                    adObject.setMinimumHeight(250);
                    adObject.setMinimumWidth(300);
                    BannerAdUnit adUnit = new BannerAdUnit("8522cead-1eb4-4f09-b6e2-083fa3a6e6ce", 300, 250);
                    OnCompleteListener l = new OnCompleteListener() {
                        @Override
                        public void onComplete(ResultCode resultCode) {

                            FrameLayout adFrame = m.getActivity().findViewById(R.id.adFrame);
                            adFrame.addView(adObject);
                            adObject.loadAd();
                        }
                    };
                    listener[1] = spy(l);
                    adUnit.fetchDemand(adObject, listener[1]);
                }
            });
            Thread.sleep(10000);
            // TODO uncomment below two lines when https://jira.corp.appnexus.com/browse/ROR-481 is resolved
//            verify(listener[1], times(1)).onComplete(ResultCode.NO_BIDS);
//            onWebView().check(webContent(containingTextInBody("Hello, I'm not a Prebid ad.")));
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
