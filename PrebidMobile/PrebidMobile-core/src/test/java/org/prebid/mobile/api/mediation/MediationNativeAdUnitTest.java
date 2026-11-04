/*
 *    Copyright 2018-2026 Prebid.org, Inc.
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

package org.prebid.mobile.api.mediation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.AdUnit;
import org.prebid.mobile.CacheManager;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.api.data.BidInfo;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.original.OnFetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ReflectionHelpers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Consumer;

@RunWith(RobolectricTestRunner.class)
public class MediationNativeAdUnitTest {

    private Bundle adObject;
    private NativeAdUnit nativeAdUnit;
    private MediationNativeAdUnit mediationNativeAdUnit;

    @Before
    public void setUp() {
        CacheManager.clear();
        adObject = new Bundle();
        nativeAdUnit = spy(new NativeAdUnit("config"));
        mediationNativeAdUnit = new MediationNativeAdUnit("config", adObject);
        WhiteBox.setInternalState(mediationNativeAdUnit, "nativeAdUnit", nativeAdUnit);
    }

    @Test
    public void fetchDemandBidInfo_ResponseReceived_CachesWinningBidOnceAndSavesItsIdToAdObject() throws IOException {
        BidResponse bidResponse = new BidResponse(
                ResourceUtils.convertResourceToString("BidResponseTest/keywords_all_with_cache_id.json"),
                nativeAdUnit.getConfiguration()
        );
        stubFetchDemand(bidListener -> bidListener.onFetchCompleted(bidResponse));
        OnFetchDemandResult listener = mock(OnFetchDemandResult.class);

        mediationNativeAdUnit.fetchDemandBidInfo(listener);

        BidInfo bidInfo = MediationBaseAdUnitTest.captureBidInfo(listener);
        assertEquals(ResultCode.SUCCESS, bidInfo.getResultCode());
        assertEquals(Double.valueOf(0.15), bidInfo.getPrice());
        assertEquals("crid", bidInfo.getCreativeId());
        Map<String, String> cachedBids = ReflectionHelpers.getStaticField(CacheManager.class, "savedValues");
        assertEquals(1, cachedBids.size());
        assertNotNull(bidInfo.getNativeCacheId());
        assertTrue(cachedBids.containsKey(bidInfo.getNativeCacheId()));
        assertEquals(bidInfo.getNativeCacheId(), adObject.getString(NativeAdUnit.BUNDLE_KEY_CACHE_ID));
    }

    @Test
    public void fetchDemandBidInfo_ErrorReceived_ReturnsResultCodeOnly() {
        stubFetchDemand(bidListener -> bidListener.onError(new AdException(AdException.INTERNAL_ERROR, "No bids")));
        OnFetchDemandResult listener = mock(OnFetchDemandResult.class);

        mediationNativeAdUnit.fetchDemandBidInfo(listener);

        BidInfo bidInfo = MediationBaseAdUnitTest.captureBidInfo(listener);
        assertEquals(ResultCode.NO_BIDS, bidInfo.getResultCode());
        assertNull(bidInfo.getPrice());
        assertNull(bidInfo.getNativeCacheId());
        assertNull(adObject.getString(NativeAdUnit.BUNDLE_KEY_CACHE_ID));
    }

    // Stands in for the network fetch: completes through the ad unit's real bid listener, the
    // way BidLoader does, so the cache id and keywords reach the ad object as in production.
    private void stubFetchDemand(Consumer<BidRequesterListener> completeFetch) {
        doAnswer(invocation -> {
            WhiteBox.setInternalState(nativeAdUnit, "adObject", invocation.getArgument(0));
            Method createBidListener = WhiteBox.method(AdUnit.class, "createBidListener", OnCompleteListener.class);
            BidRequesterListener bidListener = (BidRequesterListener) createBidListener.invoke(
                    nativeAdUnit,
                    (OnCompleteListener) invocation.getArgument(1)
            );
            completeFetch.accept(bidListener);
            return null;
        }).when(nativeAdUnit).fetchDemand(eq(adObject), any(OnCompleteListener.class));
    }

}
