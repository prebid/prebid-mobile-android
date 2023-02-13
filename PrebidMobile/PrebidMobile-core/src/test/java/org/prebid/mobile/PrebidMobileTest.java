/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.reflection.sdk.PrebidMobileReflection;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class PrebidMobileTest extends BaseSetup {

    @Before
    public void clean() {
        Reflection.setStaticVariableTo(PrebidMobile.class, "customStatusEndpoint", null);
    }


    @Test
    public void testPrebidMobileSettings() {
        PrebidMobile.setPrebidServerAccountId("123456");
        assertEquals("123456", PrebidMobile.getPrebidServerAccountId());
        PrebidMobile.setTimeoutMillis(2500);
        assertEquals(2500, PrebidMobile.getTimeoutMillis());
        PrebidMobile.initializeSdk(activity.getApplicationContext(), null);
        assertNotNull(PrebidMobile.getApplicationContext());
        PrebidMobile.setShareGeoLocation(true);
        assertTrue(PrebidMobile.isShareGeoLocation());
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        assertEquals(Host.RUBICON, PrebidMobile.getPrebidServerHost());
        PrebidMobile.setStoredAuctionResponse("111122223333");
        assertEquals("111122223333", PrebidMobile.getStoredAuctionResponse());
        PrebidMobile.addStoredBidResponse("appnexus", "221144");
        PrebidMobile.addStoredBidResponse("rubicon", "221155");
        assertFalse(PrebidMobile.getStoredBidResponses().isEmpty());
        PrebidMobile.clearStoredBidResponses();
        assertTrue(PrebidMobile.getStoredBidResponses().isEmpty());
        PrebidMobile.setPbsDebug(true);
        assertTrue(PrebidMobile.getPbsDebug());
    }

    @Test
    public void testSetCustomHeaders() {
        HashMap<String, String> customHeaders = new HashMap<>();
        customHeaders.put("key1", "value1");
        customHeaders.put("key2", "value2");
        PrebidMobile.setCustomHeaders(customHeaders);

        assertFalse(PrebidMobile.getCustomHeaders().isEmpty());
        assertEquals(2, PrebidMobile.getCustomHeaders().size());
    }

    @Test
    public void setCustomStatusEndpoint_nullValue() {
        PrebidMobile.setCustomStatusEndpoint(null);

        assertNull(getInnerCustomEndpointValue());
    }

    @Test
    public void setCustomStatusEndpoint_ipAddress() {
        PrebidMobile.setCustomStatusEndpoint("192.168.0.106");

        assertEquals("https://192.168.0.106/", getInnerCustomEndpointValue());
    }

    @Test
    public void setCustomStatusEndpoint_valueWithoutHttp() {
        PrebidMobile.setCustomStatusEndpoint("site.com");

        assertEquals("https://site.com/", getInnerCustomEndpointValue());
    }

    @Test
    public void setCustomStatusEndpoint_valueWithoutHttpWithThreeW() {
        PrebidMobile.setCustomStatusEndpoint("www.site.com");

        assertEquals("https://www.site.com/", getInnerCustomEndpointValue());
    }

    @Test
    public void setCustomStatusEndpoint_valueWithoutHttpWithThreeWAndPath() {
        PrebidMobile.setCustomStatusEndpoint("www.site.com/status");

        assertEquals("https://www.site.com/status", getInnerCustomEndpointValue());
    }

    @Test
    public void setCustomStatusEndpoint_goodValue() {
        PrebidMobile.setCustomStatusEndpoint("http://site.com/status");

        assertEquals("http://site.com/status", getInnerCustomEndpointValue());
    }

    @Test
    public void setCustomStatusEndpoint_goodValueSecure() {
        PrebidMobile.setCustomStatusEndpoint("https://site.com/status?test=1");

        assertEquals("https://site.com/status?test=1", getInnerCustomEndpointValue());
    }

    private String getInnerCustomEndpointValue() {
        return PrebidMobileReflection.getCustomStatusEndpoint();
    }

}
