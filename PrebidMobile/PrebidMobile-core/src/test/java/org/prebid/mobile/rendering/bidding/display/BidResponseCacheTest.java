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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BidResponseCacheTest {

    @Before
    public void setUp() throws Exception {
        BidResponseCache.clearAll();
    }

    @Test
    public void whenPutBidResponse_ShouldPopulateMap() {
        BidResponse mockResponse = mock(BidResponse.class);
        when(mockResponse.getId()).thenReturn("id");
        BidResponseCache.getInstance().putBidResponse(mockResponse);
        final Map<String, BidResponse> cachedResponses = BidResponseCache.getCachedBidResponses();
        assertEquals(1, cachedResponses.size());
        assertEquals(mockResponse, cachedResponses.get("id"));
    }

    @Test
    public void whenPutBidResponse_WithMaxSizeReached_ShouldIgnoreStoreRequest() {
        for (int i = 0; i < BidResponseCache.MAX_SIZE; i++) {
            BidResponse mockResponse = mock(BidResponse.class);
            when(mockResponse.getId()).thenReturn("id" + Math.random());
            when(mockResponse.getCreationTime()).thenReturn(System.currentTimeMillis());
            BidResponseCache.getInstance().putBidResponse(mockResponse);
        }
        final Map<String, BidResponse> cachedTransactions = BidResponseCache.getCachedBidResponses();
        assertEquals(BidResponseCache.MAX_SIZE, cachedTransactions.size());

        BidResponse mockResponse = mock(BidResponse.class);
        when(mockResponse.getId()).thenReturn("id" + System.currentTimeMillis());
        BidResponseCache.getInstance().putBidResponse(mockResponse);

        assertEquals(BidResponseCache.MAX_SIZE, cachedTransactions.size());
    }

    @Test
    public void whenPutBidResponse_WithStaleResponsePresent_RemoveStaleResponse() {
        final Map<String, BidResponse> cachedTransactions = BidResponseCache.getCachedBidResponses();
        for (int i = 0; i < 5; i++) {
            BidResponse mockResponse = mock(BidResponse.class);
            when(mockResponse.getId()).thenReturn("id" + Math.random());
            when(mockResponse.getCreationTime()).thenReturn(System.currentTimeMillis() - 61 * 1000);
            cachedTransactions.put("id" + Math.random(), mockResponse);
        }

        assertEquals(5, cachedTransactions.size());

        BidResponse mockResponse = mock(BidResponse.class);
        when(mockResponse.getId()).thenReturn("id" + System.currentTimeMillis());
        when(mockResponse.getCreationTime()).thenReturn(System.currentTimeMillis());
        BidResponseCache.getInstance().putBidResponse(mockResponse);
        assertEquals(1, cachedTransactions.size());
    }

    @Test
    public void whenPopBidResponse_ShouldReturnBidResponse_ShouldRemoveMappings() {
        BidResponse mockResponse = mock(BidResponse.class);
        when(mockResponse.getId()).thenReturn("id");
        long time = System.currentTimeMillis();
        when(mockResponse.getCreationTime()).thenReturn(time);

        BidResponseCache.getInstance().putBidResponse(mockResponse);

        final BidResponse result = BidResponseCache.getInstance().popBidResponse("id");

        assertTrue(BidResponseCache.getCachedBidResponses().isEmpty());
        assertEquals(mockResponse, result);
    }
}