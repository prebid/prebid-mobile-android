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

package org.prebid.mobile.rendering.bidding.data.bid;

import org.junit.Test;
import org.prebid.mobile.api.data.Position;
import org.prebid.mobile.core.BuildConfig;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.MobileSdkPassThrough;
import org.prebid.mobile.test.utils.ResourceUtils;

import java.io.IOException;

import static org.junit.Assert.*;

public class BidResponseTest {

    @Test
    public void whenInstantiatedWithValidJson_NoParseError() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("bidding_response_obj.json");
        BidResponse bidResponse = new BidResponse(responseString);
        assertFalse(bidResponse.hasParseError());
        assertNotNull(bidResponse.getExt());
        assertNotNull(bidResponse.getSeatbids());
        assertEquals(1, bidResponse.getSeatbids().size());
        assertEquals("id", bidResponse.getId());
        assertEquals("USD", bidResponse.getCur());
        assertEquals("bidid", bidResponse.getBidId());
        assertEquals("custom", bidResponse.getCustomData());
        assertEquals(1, bidResponse.getNbr());
        assertNull(bidResponse.getMobileSdkPassThrough());
    }

    @Test
    public void whenInstantiatedWithInvalidJson_ParseError() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("bidding_response_obj.json").replaceFirst(",", "");
        BidResponse bidResponse = new BidResponse(responseString);
        assertTrue(bidResponse.hasParseError());
    }

    @Test
    public void whenInstantiatedWithNoBids_NoBidsError() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("bidding_response_no_bids_obj.json");
        BidResponse bidResponse = new BidResponse(responseString);
        assertTrue(bidResponse.hasParseError());
        assertEquals("Failed to parse bids. No winning bids were found.", bidResponse.getParseError());
        assertEquals("id", bidResponse.getId());
        assertNotNull(bidResponse.getExt());
    }

    @Test
    public void whenInstantiatedWithoutWinningKeywords_NoBidsError() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("bidding_response_no_winning_keywords_obj.json");
        BidResponse bidResponse = new BidResponse(responseString);
        assertTrue(bidResponse.hasParseError());
        assertEquals("Failed to parse bids. No winning bids were found.", bidResponse.getParseError());
        assertEquals("id", bidResponse.getId());
        assertNotNull(bidResponse.getExt());
    }

    @Test
    public void testMobileSdkPassThrough_checkFieldsUnification_returnUnifiedFields() throws IOException {
        if (!BuildConfig.DEBUG) {
            String responseString = ResourceUtils.convertResourceToString("BidResponseTest/mobile_sdk_pass_through.json");

            BidResponse subject = new BidResponse(responseString);
            MobileSdkPassThrough mobileSdkPassThrough = subject.getMobileSdkPassThrough();

            assertNotNull(mobileSdkPassThrough);
            assertTrue(mobileSdkPassThrough.isMuted);
            assertEquals((Double) 0.1, mobileSdkPassThrough.closeButtonArea);
            assertEquals(Position.TOP_LEFT, mobileSdkPassThrough.closeButtonPosition);
            assertEquals((Double) 0.2, mobileSdkPassThrough.skipButtonArea);
            assertEquals(Position.TOP_RIGHT, mobileSdkPassThrough.skipButtonPosition);
            assertEquals((Integer) 15, mobileSdkPassThrough.skipDelay);

            /* This field presents in both MobileSdkPassThrough objects */
            assertEquals((Integer) 11, mobileSdkPassThrough.maxVideoDuration);
        }
    }

}