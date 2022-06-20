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

package org.prebid.mobile.rendering.networking.urlBuilder;

import org.json.JSONException;
import org.junit.Test;
import org.prebid.mobile.rendering.models.openrtb.BidRequest;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Device;
import org.prebid.mobile.rendering.networking.parameters.AdRequestInput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class URLComponentsTest {

    @Test
    public void testFullURL() throws Exception {
        AdRequestInput adRequestInput = new AdRequestInput();
        adRequestInput.getBidRequest().getApp().name = "app";
        URLComponents urlComponents = new URLComponents("www.domain.com", adRequestInput);
        assertEquals("www.domain.com?openrtb=%7B%22app%22%3A%7B%22name%22%3A%22app%22%7D%7D", urlComponents.getFullUrl());
    }

    /**
     * Tests when JSONException is thrown and the OpenRTB object is not appended to query arg string and the result query is empty
     */
    @Test
    public void getQueryArgStringThrowsException_EmptyString() throws Exception {
        BidRequest mockBidRequest = mock(BidRequest.class);
        when(mockBidRequest.getJsonObject()).thenThrow(new JSONException((String) null));
        when(mockBidRequest.getApp()).thenCallRealMethod();
        mockBidRequest.getApp().name = "ignored";

        AdRequestInput mockAdRequestInput = mock(AdRequestInput.class);
        when(mockAdRequestInput.getBidRequest()).thenReturn(mockBidRequest);

        URLComponents urlComponents = new URLComponents("", mockAdRequestInput);
        assertTrue(urlComponents.getQueryArgString().isEmpty());
    }

    /**
     * Test urlencoding of keys
     */
    @Test
    public void testEncoding() throws Exception {
        BidRequest bidRequest = new BidRequest();
        AdRequestInput mockAdRequestInput = new AdRequestInput();
        Device device = new Device();
        device.carrier = "tmobile";

        bidRequest.setId("123");
        bidRequest.setDevice(device);

        mockAdRequestInput.setBidRequest(bidRequest);

        URLComponents urlComponents = new URLComponents("", mockAdRequestInput);
        String expected = "openrtb=%7B%22id%22%3A%22123%22%2C%22device%22%3A%7B%22carrier%22%3A%22tmobile%22%7D%7D";
        assertEquals(expected, urlComponents.getQueryArgString());
    }
}