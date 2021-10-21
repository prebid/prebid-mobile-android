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

package org.prebid.mobile.rendering.bidding.display;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.NativeFetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.OnNativeFetchCompleteListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.test.utils.WhiteBox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class NativeAdUnitTest {

    @Mock
    private Context mMockContext;
    @Mock
    private OnNativeFetchCompleteListener mMockListener;
    @Mock
    private BidResponse mMockBidResponse;

    private NativeAdUnit mSpyNativeAdUnit;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final NativeAdConfiguration nativeAdConfiguration = new NativeAdConfiguration();
        mSpyNativeAdUnit = spy(new NativeAdUnit(mMockContext, "123", nativeAdConfiguration));

        assertEquals(NativeEventTracker.EventType.OMID, nativeAdConfiguration.getTrackers().get(0).getEventType());
    }

    @Test
    public void initAdConfig_AdUnitIsFilledWithExpectedFields() {
        String expectedId = "1234";
        mSpyNativeAdUnit.initAdConfig(expectedId, new AdSize(10, 10));

        AdConfiguration adUnitConfig = mSpyNativeAdUnit.mAdUnitConfig;

        assertEquals(expectedId, adUnitConfig.getConfigId());
        assertTrue(adUnitConfig.getAdSizes().isEmpty());
        assertEquals(AdConfiguration.AdUnitIdentifierType.NATIVE, adUnitConfig.getAdUnitIdentifierType());
    }

    @Test
    public void isAdObjectSupported_anyObject_ReturnTrue() {
        assertTrue(mSpyNativeAdUnit.isAdObjectSupported(null));
        assertTrue(mSpyNativeAdUnit.isAdObjectSupported(new Object()));
    }

    @Test
    public void onResponseReceived_withNullListener_DoNothing() {
        mSpyNativeAdUnit.onResponseReceived(mMockBidResponse);

        verifyZeroInteractions(mMockListener);
    }

    @Test
    public void onResponseReceived_withValidListener_SaveBidResponseAndNotifyListener() {
        WhiteBox.setInternalState(mSpyNativeAdUnit, "mNativeFetchCompleteListener", mMockListener);
        mSpyNativeAdUnit.onResponseReceived(mMockBidResponse);

        verify(mMockListener, times(1)).onComplete(any(NativeFetchDemandResult.class));
    }

    @Test
    public void onErrorReceived_withNullListener_DoNothing() {
        mSpyNativeAdUnit.onErrorReceived(new AdException("err", "orr"));

        verifyZeroInteractions(mMockListener);
    }

    @Test
    public void onErrorReceived_withValidListener_SaveBidResponseAndNotifyListener() {
        WhiteBox.setInternalState(mSpyNativeAdUnit, "mNativeFetchCompleteListener", mMockListener);

        mSpyNativeAdUnit.onErrorReceived(new AdException("Timeout", "Timeout"));

        verify(mMockListener, times(1)).onComplete(eq(new NativeFetchDemandResult(FetchDemandResult.TIMEOUT)));
    }
}