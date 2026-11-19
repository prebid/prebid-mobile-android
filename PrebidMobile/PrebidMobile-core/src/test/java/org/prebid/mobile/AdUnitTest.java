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

package org.prebid.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.prebid.mobile.api.data.BidInfo;
import org.prebid.mobile.api.original.OnFetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.io.IOException;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class AdUnitTest {

    @Before
    public void setUp() {
        CacheManager.clear();
    }

    @Test
    public void fetchDemandBidInfo_Success_PassesAdObjectAndReturnsBidInfo() throws IOException {
        BannerAdUnit adUnit = spy(new BannerAdUnit("configId", 320, 50));
        Object adObject = new Object();
        BidResponse bidResponse = new BidResponse(
                ResourceUtils.convertResourceToString("BidResponseTest/keywords_all_with_cache_id.json"),
                adUnit.getConfiguration()
        );
        stubFetchDemand(adUnit, adObject, bidResponse, ResultCode.SUCCESS);
        OnFetchDemandResult listener = mock(OnFetchDemandResult.class);

        adUnit.fetchDemandBidInfo(adObject, listener);

        BidInfo bidInfo = captureBidInfo(listener);
        assertEquals(ResultCode.SUCCESS, bidInfo.getResultCode());
        assertEquals("value3", bidInfo.getTargetingKeywords().get("hb_cache_id"));
        assertEquals(Double.valueOf(0.15), bidInfo.getPrice());
        assertEquals("USD", bidInfo.getCurrency());
        assertEquals("crid", bidInfo.getCreativeId());
        assertEquals("id", bidInfo.getRequestId());
    }

    @Test
    public void fetchDemandBidInfo_Error_ReturnsResultCodeWithoutBidEconomics() {
        BannerAdUnit adUnit = spy(new BannerAdUnit("configId", 320, 50));
        Object adObject = new Object();
        stubFetchDemand(adUnit, adObject, null, ResultCode.NO_BIDS);
        OnFetchDemandResult listener = mock(OnFetchDemandResult.class);

        adUnit.fetchDemandBidInfo(adObject, listener);

        BidInfo bidInfo = captureBidInfo(listener);
        assertEquals(ResultCode.NO_BIDS, bidInfo.getResultCode());
        assertNull(bidInfo.getPrice());
        assertNull(bidInfo.getRequestId());
    }

    @Test
    public void fetchDemandBidInfo_NativeAdUnit_CachesWinningBidOnce() throws IOException {
        NativeAdUnit adUnit = spy(new NativeAdUnit("configId"));
        Bundle adObject = new Bundle();
        stubFetchDemandThroughBidListener(adUnit, adObject, createNativeBidResponse(adUnit));
        OnFetchDemandResult listener = mock(OnFetchDemandResult.class);

        adUnit.fetchDemandBidInfo(adObject, listener);

        BidInfo bidInfo = captureBidInfo(listener);
        Map<String, String> cachedBids = ReflectionHelpers.getStaticField(CacheManager.class, "savedValues");
        assertEquals(1, cachedBids.size());
        assertNotNull(bidInfo.getNativeCacheId());
        assertTrue(cachedBids.containsKey(bidInfo.getNativeCacheId()));
        assertEquals(bidInfo.getNativeCacheId(), adObject.getString(NativeAdUnit.BUNDLE_KEY_CACHE_ID));
    }

    @Test
    public void fetchDemand_NativeAdUnitWithBidInfoListener_CachesWinningBidOnce() throws IOException {
        NativeAdUnit adUnit = spy(new NativeAdUnit("configId"));
        stubFetchDemandThroughBidListener(adUnit, null, createNativeBidResponse(adUnit));
        OnFetchDemandResult listener = mock(OnFetchDemandResult.class);

        adUnit.fetchDemand(listener);

        BidInfo bidInfo = captureBidInfo(listener);
        Map<String, String> cachedBids = ReflectionHelpers.getStaticField(CacheManager.class, "savedValues");
        assertEquals(1, cachedBids.size());
        assertTrue(cachedBids.containsKey(bidInfo.getNativeCacheId()));
    }

    @Test
    public void fetchDemandBidInfo_NullListener_DoesNotFetch() {
        BannerAdUnit adUnit = spy(new BannerAdUnit("configId", 320, 50));

        adUnit.fetchDemandBidInfo(new Object(), null);

        verify(adUnit, never()).fetchDemand(any(), any(OnCompleteListener.class));
    }

    // Stands in for the network fetch: stores the response the way the bid listener does and
    // completes with the given result code.
    private static void stubFetchDemand(
            AdUnit adUnit,
            Object adObject,
            @Nullable BidResponse bidResponse,
            ResultCode resultCode
    ) {
        doAnswer(invocation -> {
            adUnit.adObject = adObject;
            adUnit.bidResponse = bidResponse;
            OnCompleteListener listener = invocation.getArgument(1);
            listener.onComplete(resultCode);
            return null;
        }).when(adUnit).fetchDemand(eq(adObject), any(OnCompleteListener.class));
    }

    // Stands in for the network fetch: completes through the ad unit's real bid listener, the
    // way BidLoader does, so the native bid is cached as in production.
    private static void stubFetchDemandThroughBidListener(
            AdUnit adUnit,
            @Nullable Object adObject,
            BidResponse bidResponse
    ) {
        doAnswer(invocation -> {
            adUnit.adObject = adObject;
            OnCompleteListener listener = invocation.getArgument(1);
            adUnit.createBidListener(listener).onFetchCompleted(bidResponse);
            return null;
        }).when(adUnit).fetchDemand(eq(adObject), any(OnCompleteListener.class));
    }

    private static BidResponse createNativeBidResponse(NativeAdUnit adUnit) throws IOException {
        return new BidResponse(
                ResourceUtils.convertResourceToString("BidResponseTest/keywords_all_with_cache_id.json"),
                adUnit.getConfiguration()
        );
    }

    private static BidInfo captureBidInfo(OnFetchDemandResult listener) {
        ArgumentCaptor<BidInfo> captor = ArgumentCaptor.forClass(BidInfo.class);
        verify(listener).onComplete(captor.capture());
        return captor.getValue();
    }

}
