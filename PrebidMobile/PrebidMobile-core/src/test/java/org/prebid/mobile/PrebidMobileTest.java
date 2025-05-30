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
import static org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.PBSConfig;
import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.reflection.sdk.PrebidMobileReflection;
import org.prebid.mobile.testutils.BaseSetup;
import org.prebid.mobile.testutils.FakePrebidMobilePluginRenderer;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class PrebidMobileTest extends BaseSetup {

    @Before
    public void clean() {
        Reflection.setStaticVariableTo(PrebidMobile.class, "customStatusEndpoint", null);
        Reflection.setStaticVariableTo(PrebidMobile.class, "auctionSettingsId", null);
    }


    @Test
    public void testPrebidMobileSettings() {
        PrebidMobile.setPrebidServerAccountId("123456");
        assertEquals("123456", PrebidMobile.getPrebidServerAccountId());
        PrebidMobile.setTimeoutMillis(2500);
        assertEquals(2500, PrebidMobile.getTimeoutMillis());
        assertNotNull(activity.getApplicationContext());
        PrebidMobile.initializeSdk(activity.getApplicationContext(), "https://test.com", null);
        PrebidMobile.setShareGeoLocation(true);
        assertTrue(PrebidMobile.isShareGeoLocation());
        assertEquals(Host.createCustomHost("https://test.com"), PrebidMobile.getPrebidServerHost());
        PrebidMobile.setStoredAuctionResponse("111122223333");
        assertEquals("111122223333", PrebidMobile.getStoredAuctionResponse());
        PrebidMobile.addStoredBidResponse("appnexus", "221144");
        PrebidMobile.addStoredBidResponse("rubicon", "221155");
        assertFalse(PrebidMobile.getStoredBidResponses().isEmpty());
        PrebidMobile.clearStoredBidResponses();
        assertTrue(PrebidMobile.getStoredBidResponses().isEmpty());
        PrebidMobile.setPbsDebug(true);
        assertTrue(PrebidMobile.getPbsDebug());
        PrebidMobile.setCreativeFactoryTimeout(7000);
        assertEquals(7000, PrebidMobile.getCreativeFactoryTimeout());
        PrebidMobile.setCreativeFactoryTimeoutPreRenderContent(25000);
        assertEquals(25000, PrebidMobile.getCreativeFactoryTimeoutPreRenderContent());
        PrebidMobile.setAuctionSettingsId("987654");
        assertEquals("987654", PrebidMobile.getAuctionSettingsId());
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

    @Test
    public void registerPluginRenderer_registerProperly() {
        // Given
        PrebidMobilePluginRenderer fakePrebidMobilePluginRenderer = FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                null,
                null,
                true,
                PREBID_MOBILE_RENDERER_NAME,
                "1.0"
        );

        // When
        PrebidMobile.registerPluginRenderer(fakePrebidMobilePluginRenderer);

        // Then
        assertTrue(PrebidMobile.containsPluginRenderer(fakePrebidMobilePluginRenderer));
    }

    @Test
    public void registerPluginRenderer_unregisterProperly() {
        // Given
        PrebidMobilePluginRenderer fakePrebidMobilePluginRenderer = FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                null,
                null,
                true,
                PREBID_MOBILE_RENDERER_NAME,
                "1.0"
        );

        // When
        PrebidMobile.registerPluginRenderer(fakePrebidMobilePluginRenderer);

        // Then
        assertTrue(PrebidMobile.containsPluginRenderer(fakePrebidMobilePluginRenderer));

        // When
        PrebidMobile.unregisterPluginRenderer(fakePrebidMobilePluginRenderer);

        // Then
        assertFalse(PrebidMobile.containsPluginRenderer(fakePrebidMobilePluginRenderer));
    }

    @Test
    public void getCreativeFactoryTimeouts_usePbsConfig() {
        PrebidMobile.setPbsConfig(new PBSConfig(9000, 20000));
        PrebidMobile.setCreativeFactoryTimeout(8000);
        PrebidMobile.setCreativeFactoryTimeoutPreRenderContent(21000);
        assertEquals(9000, PrebidMobile.getCreativeFactoryTimeout());
        assertEquals(20000, PrebidMobile.getCreativeFactoryTimeoutPreRenderContent());
    }

    @Test
    public void getCreativeFactoryTimeouts_useSdk() {
        PrebidMobile.setCreativeFactoryTimeout(8000);
        PrebidMobile.setCreativeFactoryTimeoutPreRenderContent(21000);
        assertEquals(8000, PrebidMobile.getCreativeFactoryTimeout());
        assertEquals(21000, PrebidMobile.getCreativeFactoryTimeoutPreRenderContent());
    }

}
