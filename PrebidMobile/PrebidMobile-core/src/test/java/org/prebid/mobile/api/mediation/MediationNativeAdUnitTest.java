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

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.api.data.BidInfo;
import org.prebid.mobile.api.original.OnFetchDemandResult;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MediationNativeAdUnitTest {

    private Bundle adObject;
    private NativeAdUnit mockNativeAdUnit;
    private MediationNativeAdUnit mediationNativeAdUnit;

    @Before
    public void setUp() {
        adObject = new Bundle();
        mockNativeAdUnit = mock(NativeAdUnit.class);
        mediationNativeAdUnit = new MediationNativeAdUnit("config", adObject);
        WhiteBox.setInternalState(mediationNativeAdUnit, "nativeAdUnit", mockNativeAdUnit);
    }

    @Test
    public void fetchDemandBidInfo_PassesAdObjectAndReturnsBidInfo() {
        BidInfo expected = BidInfo.create(ResultCode.SUCCESS, null, null);
        doAnswer(invocation -> {
            OnFetchDemandResult listener = invocation.getArgument(1);
            listener.onComplete(expected);
            return null;
        }).when(mockNativeAdUnit).fetchDemandBidInfo(eq(adObject), any(OnFetchDemandResult.class));
        OnFetchDemandResult listener = mock(OnFetchDemandResult.class);

        mediationNativeAdUnit.fetchDemandBidInfo(listener);

        verify(mockNativeAdUnit).fetchDemandBidInfo(eq(adObject), any(OnFetchDemandResult.class));
        assertSame(expected, MediationBaseAdUnitTest.captureBidInfo(listener));
    }

}
