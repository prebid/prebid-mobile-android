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

package org.prebid.mobile.api.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Looper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.CacheManager;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.configuration.NativeAdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
public class BidInfoTest {

    @Before
    public void setUp() {
        CacheManager.clear();
    }

    @Test
    public void createNativeBidInfoWithExpiration_CachedBidExpiresAfterExp() {
        BidInfo bidInfo = BidInfo.create(ResultCode.SUCCESS, createNativeBidResponse(1), createNativeConfiguration());

        assertNotNull(bidInfo.getNativeCacheId());
        assertTrue(CacheManager.isValid(bidInfo.getNativeCacheId()));

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(CacheManager.isValid(bidInfo.getNativeCacheId()));
    }

    @Test
    public void createNativeBidInfoWithoutExpiration_CachedBidUsesDefaultExpiry() {
        BidInfo bidInfo = BidInfo.create(ResultCode.SUCCESS, createNativeBidResponse(null), createNativeConfiguration());

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertTrue(CacheManager.isValid(bidInfo.getNativeCacheId()));
    }

    private static BidResponse createNativeBidResponse(Integer exp) {
        BidResponse bidResponse = mock(BidResponse.class);
        when(bidResponse.getTargeting()).thenReturn(new HashMap<>());
        when(bidResponse.getWinningBidJson()).thenReturn("{\"adm\":\"native\"}");
        when(bidResponse.getExpirationTimeSeconds()).thenReturn(exp);
        return bidResponse;
    }

    private static AdUnitConfiguration createNativeConfiguration() {
        AdUnitConfiguration configuration = new AdUnitConfiguration();
        configuration.setNativeConfiguration(new NativeAdUnitConfiguration());
        return configuration;
    }

}
