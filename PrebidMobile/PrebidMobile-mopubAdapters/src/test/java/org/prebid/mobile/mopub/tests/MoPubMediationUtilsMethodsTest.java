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
import org.prebid.mobile.AdSize;
import org.prebid.mobile.mopub.mock.TestResponse;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.MediationBannerAdUnit;
import org.prebid.mobile.rendering.bidding.display.MediationInterstitialAdUnit;
import org.prebid.mobile.rendering.bidding.display.MediationNativeAdUnit;
import org.prebid.mobile.rendering.bidding.display.MediationRewardedVideoAdUnit;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class MoPubMediationUtilsMethodsTest {

    private Context context;

    private static final AdSize AD_SIZE = new AdSize(1, 1);
    private static final String ID = "configId";

    private MoPubView moPubView;
    private MoPubInterstitial moPubInterstitial;
    private MoPubNative moPubNative;
    private HashMap<String, String> moPubNativeKeywords = mock(HashMap.class);
    private HashMap<String, String> moPubRewardedKeywords = mock(HashMap.class);

    private MediationBannerAdUnit mediationBannerAdUnit;
    private MediationInterstitialAdUnit mediationInterstitialAdUnit;
    private MediationNativeAdUnit mediationNativeAdUnit;
    private MediationRewardedVideoAdUnit moPubRewardedAdUnit;

    private MoPubBannerMediationUtils bannerUtils;
    private MoPubInterstitialMediationUtils interstitialUtils;
    private MoPubNativeMediationUtils nativeUtils;
    private MoPubRewardedVideoMediationUtils rewardedUtils;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        context = Robolectric.buildActivity(Activity.class).create().get();

        moPubView = mock(MoPubView.class);
        bannerUtils = new MoPubBannerMediationUtils(moPubView);
        mediationBannerAdUnit = new MediationBannerAdUnit(context, ID, AD_SIZE, bannerUtils);

        moPubInterstitial = mock(MoPubInterstitial.class);
        interstitialUtils = new MoPubInterstitialMediationUtils(moPubInterstitial);
        mediationInterstitialAdUnit = new MediationInterstitialAdUnit(context, ID, AD_SIZE, interstitialUtils);

        moPubNative = mock(MoPubNative.class);
        nativeUtils = new MoPubNativeMediationUtils(moPubNativeKeywords, moPubNative);
        mediationNativeAdUnit = new MediationNativeAdUnit(ID, nativeUtils);

        rewardedUtils = new MoPubRewardedVideoMediationUtils(moPubRewardedKeywords);
        moPubRewardedAdUnit = new MediationRewardedVideoAdUnit(context, ID, rewardedUtils);
    }

    @Test
    public void canPerformRefresh_ReturnTrueByDefault() {
        assertFalse(interstitialUtils.canPerformRefresh());
        assertFalse(rewardedUtils.canPerformRefresh());
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
        verify(moPubView).setLocalExtras(responseId);
    }

    @Test
    public void setResponseToLocalExtrasInInterstitial_MoPubViewSetsResponseId() {
        BidResponse bidResponse = new BidResponse(TestResponse.getResponse());
        interstitialUtils.setResponseToLocalExtras(bidResponse);

        HashMap<String, String> responseId = new HashMap<>();
        responseId.put("PREBID_BID_RESPONSE_ID", "id");
        verify(moPubInterstitial).setLocalExtras(responseId);
    }

    @Test
    public void handleKeywordsUpdateInBanner_MoPubViewSetsKeywords() {
        HashMap<String, String> keywords = TestResponse.getKeywordsMap();
        bannerUtils.handleKeywordsUpdate(keywords);

        verify(moPubView, atLeastOnce()).getKeywords();
        verify(moPubView).setKeywords(TestResponse.getKeywordsString());
    }

    @Test
    public void handleKeywordsUpdateInInterstitial_MoPubViewSetsKeywords() {
        HashMap<String, String> keywords = TestResponse.getKeywordsMap();
        interstitialUtils.handleKeywordsUpdate(keywords);

        verify(moPubInterstitial, atLeastOnce()).getKeywords();
        verify(moPubInterstitial).setKeywords(TestResponse.getKeywordsString());
    }

    @Test
    public void handleKeywordsUpdateInNative_MoPubViewSetsKeywords() {
        HashMap<String, String> keywords = TestResponse.getKeywordsMap();
        nativeUtils.handleKeywordsUpdate(keywords);

        verify(moPubNativeKeywords).clear();
        verify(moPubNativeKeywords).putAll(keywords);
    }

    @Test
    public void handleKeywordsUpdateInRewarded_MoPubViewSetsKeywords() {
        HashMap<String, String> keywords = TestResponse.getKeywordsMap();
        rewardedUtils.handleKeywordsUpdate(keywords);

        verify(moPubRewardedKeywords).clear();
        verify(moPubRewardedKeywords).putAll(keywords);
    }

}
