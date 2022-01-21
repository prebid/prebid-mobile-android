/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.mopub.tests;

import android.app.Activity;
import android.content.Context;
import com.mopub.mediation.MoPubBannerMediationUtils;
import com.mopub.mediation.MoPubInterstitialMediationUtils;
import com.mopub.mediation.MoPubNativeMediationUtils;
import com.mopub.mediation.MoPubRewardedVideoMediationUtils;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import com.mopub.nativeads.MoPubNative;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.mopub.mock.TestResponse;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.MediationBannerAdUnit;
import org.prebid.mobile.rendering.bidding.display.MediationInterstitialAdUnit;
import org.prebid.mobile.rendering.bidding.display.MediationNativeAdUnit;
import org.prebid.mobile.rendering.bidding.display.MediationRewardedVideoAdUnit;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class MoPubMediationUtilsMethodsTest {

    private Context mContext;

    private static final AdSize AD_SIZE = new AdSize(1, 1);
    private static final String ID = "configId";

    private MoPubView mMoPubView;
    private MoPubInterstitial mMoPubInterstitial;
    private MoPubNative mMoPubNative;
    private HashMap<String, String> mMoPubNativeKeywords = mock(HashMap.class);
    private HashMap<String, String> mMoPubRewardedKeywords = mock(HashMap.class);

    private MediationBannerAdUnit mMediationBannerAdUnit;
    private MediationInterstitialAdUnit mMediationInterstitialAdUnit;
    private MediationNativeAdUnit mMediationNativeAdUnit;
    private MediationRewardedVideoAdUnit mMoPubRewardedAdUnit;

    private MoPubBannerMediationUtils bannerUtils;
    private MoPubInterstitialMediationUtils interstitialUtils;
    private MoPubNativeMediationUtils nativeUtils;
    private MoPubRewardedVideoMediationUtils rewardedUtils;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        mContext = Robolectric.buildActivity(Activity.class).create().get();

        mMoPubView = mock(MoPubView.class);
        bannerUtils = new MoPubBannerMediationUtils(mMoPubView);
        mMediationBannerAdUnit = new MediationBannerAdUnit(mContext, ID, AD_SIZE, bannerUtils);

        mMoPubInterstitial = mock(MoPubInterstitial.class);
        interstitialUtils = new MoPubInterstitialMediationUtils(mMoPubInterstitial);
        mMediationInterstitialAdUnit = new MediationInterstitialAdUnit(mContext, ID, AD_SIZE, interstitialUtils);

        mMoPubNative = mock(MoPubNative.class);
        nativeUtils = new MoPubNativeMediationUtils(mMoPubNativeKeywords, mMoPubNative);
        mMediationNativeAdUnit = new MediationNativeAdUnit(mContext, ID, mock(NativeAdConfiguration.class), nativeUtils);

        rewardedUtils = new MoPubRewardedVideoMediationUtils(mMoPubRewardedKeywords);
        mMoPubRewardedAdUnit = new MediationRewardedVideoAdUnit(mContext, ID, rewardedUtils);
    }

    @Test
    public void canPerformRefresh_ReturnTrueByDefault() {
        assertTrue(interstitialUtils.canPerformRefresh());
        assertTrue(nativeUtils.canPerformRefresh());
        assertTrue(rewardedUtils.canPerformRefresh());
    }

    @Test
    public void canPerformRefreshInBanner_ReturnFalseBecauseItIsNotVisible() {
        assertFalse(bannerUtils.canPerformRefresh());
    }

    @Test
    public void setResponseToLocalExtrasInBanner_MoPubViewSetsResponseId() {
        BidResponse bidResponse = new BidResponse(TestResponse.getResponse());
        bannerUtils.setResponseToLocalExtras(bidResponse);

        HashMap<String, String> responseId = new HashMap<>();
        responseId.put("PREBID_BID_RESPONSE_ID", "id");
        verify(mMoPubView).setLocalExtras(responseId);
    }

    @Test
    public void setResponseToLocalExtrasInInterstitial_MoPubViewSetsResponseId() {
        BidResponse bidResponse = new BidResponse(TestResponse.getResponse());
        interstitialUtils.setResponseToLocalExtras(bidResponse);

        HashMap<String, String> responseId = new HashMap<>();
        responseId.put("PREBID_BID_RESPONSE_ID", "id");
        verify(mMoPubInterstitial).setLocalExtras(responseId);
    }

    @Test
    public void setResponseToLocalExtrasInNative_MoPubViewSetsResponse() {
        BidResponse bidResponse = new BidResponse(TestResponse.getResponse());
        nativeUtils.setResponseToLocalExtras(bidResponse);

        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put("PREBID_BID_RESPONSE_ID", bidResponse);
        verify(mMoPubNative).setLocalExtras(responseMap);
    }

    @Test
    public void handleKeywordsUpdateInBanner_MoPubViewSetsKeywords() {
        HashMap<String, String> keywords = TestResponse.getKeywordsMap();
        bannerUtils.handleKeywordsUpdate(keywords);

        verify(mMoPubView, atLeastOnce()).getKeywords();
        verify(mMoPubView).setKeywords(TestResponse.getKeywordsString());
    }

    @Test
    public void handleKeywordsUpdateInInterstitial_MoPubViewSetsKeywords() {
        HashMap<String, String> keywords = TestResponse.getKeywordsMap();
        interstitialUtils.handleKeywordsUpdate(keywords);

        verify(mMoPubInterstitial, atLeastOnce()).getKeywords();
        verify(mMoPubInterstitial).setKeywords(TestResponse.getKeywordsString());
    }

    @Test
    public void handleKeywordsUpdateInNative_MoPubViewSetsKeywords() {
        HashMap<String, String> keywords = TestResponse.getKeywordsMap();
        nativeUtils.handleKeywordsUpdate(keywords);

        verify(mMoPubNativeKeywords).clear();
        verify(mMoPubNativeKeywords).putAll(keywords);
    }

    @Test
    public void handleKeywordsUpdateInRewarded_MoPubViewSetsKeywords() {
        HashMap<String, String> keywords = TestResponse.getKeywordsMap();
        rewardedUtils.handleKeywordsUpdate(keywords);

        verify(mMoPubRewardedKeywords).clear();
        verify(mMoPubRewardedKeywords).putAll(keywords);
    }

}
