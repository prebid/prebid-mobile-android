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

package org.prebid.mobile.api.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.rendering.PrebidRenderer;
import org.prebid.mobile.api.rendering.pluginrenderer.PluginEventListener;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.PluginRendererList;
import org.prebid.mobile.testutils.FakePrebidMobilePluginRenderer;
import org.prebid.mobile.testutils.RecordingLogger;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
@SuppressWarnings("deprecation")
public class PluginRegistrationTest {

    private final PrebidMobilePluginRegister register = PrebidMobilePluginRegister.getInstance();
    private final PluginRegistry<PrebidMobilePluginRenderer> registry = PluginCapability.RENDERER.getRegistry();

    private List<PrebidMobilePluginRenderer> savedRenderers;
    private RecordingLogger logger;

    @Before
    public void setUp() {
        savedRenderers = registry.getActive();
        registry.removeAll();
        logger = new RecordingLogger(LogUtil.WARN).install();
    }

    @After
    public void tearDown() {
        logger.uninstall();
        registry.removeAll();
        for (PrebidMobilePluginRenderer renderer : savedRenderers) {
            registry.add(renderer);
        }
    }

    @Test
    public void rendererCapability() {
        assertEquals("renderer", PluginCapability.RENDERER.getName());
        assertEquals("renderer", PluginCapability.RENDERER.toString());
        assertSame(PluginCapability.RENDERER.getRegistry(), PluginCapability.RENDERER.getRegistry());
    }

    @Test
    public void registerPlugin_renderer_isAvailableToTheSdk() {
        PrebidMobilePluginRenderer renderer = renderer("Vendor", "1.0");

        PrebidMobile.registerPlugin(renderer, PluginCapability.RENDERER);

        assertTrue(PrebidMobile.containsPlugin(renderer, PluginCapability.RENDERER));
        assertTrue(register.containsPlugin(renderer));
        assertTrue(register.containsPlugin("Vendor"));
        assertEquals(Collections.singletonList(renderer), register.getRTBListOfRenderersFor(bannerConfig()));
        assertTrue(logger.getMessages().isEmpty());
    }

    @Test
    public void registerPlugin_sameNameOtherVersion_keepsFirstRendererAndWarns() {
        PrebidMobilePluginRenderer first = renderer("Vendor", "1.0");
        PrebidMobilePluginRenderer second = renderer("Vendor", "2.0");

        PrebidMobile.registerPlugin(first, PluginCapability.RENDERER);
        PrebidMobile.registerPlugin(second, PluginCapability.RENDERER);

        assertEquals(Collections.singletonList(first), registry.getActive());
        // Renderers are identified by name, so the second one is "contained" too.
        assertTrue(PrebidMobile.containsPlugin(second, PluginCapability.RENDERER));
        assertEquals(
                Collections.singletonList("[renderer] Plugin Vendor is not registered: a plugin with the same identity is already registered."),
                logger.getMessages()
        );
    }

    @Test
    public void deprecatedRegisterMethods_sameName_keepFirstRenderer() {
        PrebidMobilePluginRenderer first = renderer("Vendor", "1.0");

        PrebidMobile.registerPluginRenderer(first);
        PrebidMobile.registerPluginRenderer(renderer("Vendor", "2.0"));
        register.registerPlugin(renderer("Vendor", "3.0"));

        assertEquals(Collections.singletonList(first), registry.getActive());
    }

    @Test
    public void registerPlugin_replaceRenderer_byUnregisteringFirst() {
        PrebidMobilePluginRenderer first = renderer("Vendor", "1.0");
        PrebidMobilePluginRenderer second = renderer("Vendor", "2.0");
        PrebidMobile.registerPlugin(first, PluginCapability.RENDERER);

        PrebidMobile.unregisterPlugin(first, PluginCapability.RENDERER);
        PrebidMobile.registerPlugin(second, PluginCapability.RENDERER);

        assertEquals(Collections.singletonList(second), registry.getActive());
    }

    @Test
    public void unregisterPlugin_removesRendererWithSameName() {
        PrebidMobilePluginRenderer registered = renderer("Vendor", "1.0");
        PrebidMobilePluginRenderer other = renderer("Vendor", "2.0");
        PrebidMobile.registerPlugin(registered, PluginCapability.RENDERER);

        PrebidMobile.unregisterPlugin(other, PluginCapability.RENDERER);

        assertFalse(PrebidMobile.containsPlugin(registered, PluginCapability.RENDERER));
        assertTrue(registry.getActive().isEmpty());
    }

    @Test
    public void unregisterPlugin_unknownRenderer_isNoOp() {
        PrebidMobilePluginRenderer registered = renderer("Vendor", "1.0");
        PrebidMobile.registerPlugin(registered, PluginCapability.RENDERER);

        PrebidMobile.unregisterPlugin(renderer("Other", "1.0"), PluginCapability.RENDERER);

        assertEquals(Collections.singletonList(registered), registry.getActive());
    }

    @Test
    public void deprecatedRendererMethods_useRendererCapability() {
        PrebidMobilePluginRenderer renderer = renderer("Vendor", "1.0");

        PrebidMobile.registerPluginRenderer(renderer);

        assertTrue(PrebidMobile.containsPlugin(renderer, PluginCapability.RENDERER));
        assertTrue(PrebidMobile.containsPluginRenderer(renderer));
        // Both match by name.
        assertTrue(PrebidMobile.containsPluginRenderer(renderer("Vendor", "2.0")));

        PrebidMobile.unregisterPluginRenderer(renderer);

        assertFalse(PrebidMobile.containsPlugin(renderer, PluginCapability.RENDERER));
        assertFalse(PrebidMobile.containsPluginRenderer(renderer));

        register.registerPlugin(renderer);
        assertTrue(PrebidMobile.containsPlugin(renderer, PluginCapability.RENDERER));
        register.unregisterPlugin(renderer);
        assertFalse(register.containsPlugin(renderer));
    }

    @Test
    public void getRTBListOfRenderersFor_returnsRenderersInRegistrationOrder() throws JSONException {
        List<String> names = Arrays.asList("c", "a", "d", "b");
        for (String name : names) {
            PrebidMobile.registerPlugin(renderer(name, "1.0"), PluginCapability.RENDERER);
        }

        AdUnitConfiguration config = bannerConfig();
        List<String> rtbNames = new ArrayList<>();
        for (PrebidMobilePluginRenderer renderer : register.getRTBListOfRenderersFor(config)) {
            rtbNames.add(renderer.getName());
        }
        assertEquals(names, rtbNames);

        JSONArray renderers = Prebid.getJsonObjectForBidRequest("account", false, config)
                .getJSONObject("sdk")
                .getJSONArray(PluginRendererList.RENDERERS_KEY);
        List<String> jsonNames = new ArrayList<>();
        for (int i = 0; i < renderers.length(); i++) {
            jsonNames.add(renderers.getJSONObject(i).getString("name"));
        }
        assertEquals(names, jsonNames);
    }

    @Test
    public void sdkRenderer_isDefaultAndFallback() {
        PrebidRenderer sdkRenderer = new PrebidRenderer();
        PrebidMobilePluginRenderer vendor = renderer("Vendor", "1.0");
        PrebidMobile.registerPlugin(sdkRenderer, PluginCapability.RENDERER);
        PrebidMobile.registerPlugin(vendor, PluginCapability.RENDERER);

        assertSame(sdkRenderer, register.getDefaultPluginRenderer());
        assertSame(vendor, register.getPluginForPreferredRenderer(bidResponse("Vendor", "1.0"), bannerConfig()));
        assertSame(sdkRenderer, register.getPluginForPreferredRenderer(bidResponse("Vendor", "2.0"), bannerConfig()));
        assertSame(sdkRenderer, register.getPluginForPreferredRenderer(bidResponse("Unknown", "1.0"), bannerConfig()));
    }

    @Test
    public void getDefaultPluginRenderer_withoutSdkRenderer_returnsNull() {
        PrebidMobile.registerPlugin(renderer("Vendor", "1.0"), PluginCapability.RENDERER);

        assertNull(register.getDefaultPluginRenderer());
        assertNull(register.getPluginForPreferredRenderer(bidResponse("Unknown", "1.0"), bannerConfig()));
    }

    @Test
    public void eventListeners_reachRegisteredRenderers() {
        PrebidMobilePluginRenderer vendor = mock(PrebidMobilePluginRenderer.class);
        when(vendor.getName()).thenReturn("Vendor");
        PrebidMobilePluginRenderer other = mock(PrebidMobilePluginRenderer.class);
        when(other.getName()).thenReturn("Other");
        PrebidMobile.registerPlugin(vendor, PluginCapability.RENDERER);
        PrebidMobile.registerPlugin(other, PluginCapability.RENDERER);
        PluginEventListener listener = () -> "Vendor";

        register.registerEventListener(listener, "fingerprint");
        register.unregisterEventListener("fingerprint");

        verify(vendor).registerEventListener(listener, "fingerprint");
        verify(vendor).unregisterEventListener("fingerprint");
        verify(other).unregisterEventListener("fingerprint");
    }

    @Test
    public void rendererWithNullName_canBeRegisteredAndUnregistered() {
        PrebidMobilePluginRenderer renderer = mock(PrebidMobilePluginRenderer.class);

        PrebidMobile.registerPlugin(renderer, PluginCapability.RENDERER);

        assertTrue(PrebidMobile.containsPlugin(renderer, PluginCapability.RENDERER));

        PrebidMobile.unregisterPlugin(renderer, PluginCapability.RENDERER);

        assertFalse(PrebidMobile.containsPlugin(renderer, PluginCapability.RENDERER));
    }

    @Test
    public void unregisterAllPlugins_removesEveryRenderer() {
        PrebidMobile.registerPlugin(renderer("a", "1.0"), PluginCapability.RENDERER);
        PrebidMobile.registerPlugin(renderer("b", "1.0"), PluginCapability.RENDERER);

        register.unregisterAllPlugins();

        assertTrue(registry.getActive().isEmpty());
    }

    private static PrebidMobilePluginRenderer renderer(String name, String version) {
        return FakePrebidMobilePluginRenderer.getFakePrebidRenderer(null, null, true, name, version);
    }

    private static AdUnitConfiguration bannerConfig() {
        AdUnitConfiguration config = new AdUnitConfiguration();
        config.setAdFormat(AdFormat.BANNER);
        config.setIsOriginalAdUnit(false);
        return config;
    }

    private static BidResponse bidResponse(String rendererName, String rendererVersion) {
        BidResponse bidResponse = mock(BidResponse.class);
        when(bidResponse.getPreferredPluginRendererName()).thenReturn(rendererName);
        when(bidResponse.getPreferredPluginRendererVersion()).thenReturn(rendererVersion);
        return bidResponse;
    }
}
