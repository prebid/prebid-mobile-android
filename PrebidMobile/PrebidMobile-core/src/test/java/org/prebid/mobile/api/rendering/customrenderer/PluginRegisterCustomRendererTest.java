package org.prebid.mobile.api.rendering.customrenderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.prebid.mobile.api.rendering.customrenderer.PluginRegisterCustomRenderer.PREBID_MOBILE_RENDERER_NAME;

import org.junit.Before;
import org.junit.Test;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.List;

public class PluginRegisterCustomRendererTest {

    private PluginRegisterCustomRenderer instance;
    private PrebidMobilePluginCustomRenderer mockPlugin;
    private BidResponse mockBidResponse;
    private AdUnitConfiguration mockAdUnitConfiguration;

    @Before
    public void setUp() {
        instance = PluginRegisterCustomRenderer.getInstance();
        mockPlugin = mock(PrebidMobilePluginCustomRenderer.class);
        mockBidResponse = mock(BidResponse.class);
        mockAdUnitConfiguration = mock(AdUnitConfiguration.class);

        // Default prebid renderer init is expected
        PrebidMobilePluginCustomRenderer mockPrebidPlugin = mock(PrebidMobilePluginCustomRenderer.class);
        when(mockPrebidPlugin.getName()).thenReturn(PREBID_MOBILE_RENDERER_NAME);
        instance.registerPlugin(mockPrebidPlugin);
    }

    @Test
    public void getRTBListOfCustomRenderersFor_withNoMatchingRenderers_returnsEmptyList() {
        // Given
        when(mockPlugin.getName()).thenReturn("MockPlugin");
        when(mockPlugin.isSupportRenderingFor(mockAdUnitConfiguration)).thenReturn(false);
        PrebidMobilePluginCustomRenderer mockPlugin2 = mock(PrebidMobilePluginCustomRenderer.class);
        when(mockPlugin2.getName()).thenReturn("MockPlugin2");
        when(mockPlugin2.isSupportRenderingFor(mockAdUnitConfiguration)).thenReturn(false);

        // When
        instance.registerPlugin(mockPlugin);
        instance.registerPlugin(mockPlugin2);
        List<String> result = instance.getRTBListOfCustomRenderersFor(mockAdUnitConfiguration);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    public void getRTBListOfCustomRenderersFor_withMatchingRenderer_returnsListWithOneOccurrence() {
        // Given
        when(mockPlugin.isSupportRenderingFor(mockAdUnitConfiguration)).thenReturn(true);
        when(mockPlugin.getName()).thenReturn("MockPlugin");
        PrebidMobilePluginCustomRenderer mockPlugin2 = mock(PrebidMobilePluginCustomRenderer.class);
        when(mockPlugin2.getName()).thenReturn("MockPlugin2");
        when(mockPlugin2.isSupportRenderingFor(mockAdUnitConfiguration)).thenReturn(false);

        // When
        instance.registerPlugin(mockPlugin);
        instance.registerPlugin(mockPlugin2);
        List<String> result = instance.getRTBListOfCustomRenderersFor(mockAdUnitConfiguration);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    public void getRTBListOfCustomRenderersFor_withMatchingRenderers_returnsList() {
        // Given
        when(mockPlugin.isSupportRenderingFor(mockAdUnitConfiguration)).thenReturn(true);
        when(mockPlugin.getName()).thenReturn("MockPlugin");
        PrebidMobilePluginCustomRenderer mockPlugin2 = mock(PrebidMobilePluginCustomRenderer.class);
        when(mockPlugin2.getName()).thenReturn("MockPlugin2");
        when(mockPlugin2.isSupportRenderingFor(mockAdUnitConfiguration)).thenReturn(true);

        // When
        instance.registerPlugin(mockPlugin);
        instance.registerPlugin(mockPlugin2);
        List<String> result = instance.getRTBListOfCustomRenderersFor(mockAdUnitConfiguration);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    public void getPluginForPreferredRenderer_withMatchingRenderer_returnsPreferredRenderer() {
        // Given
        when(mockPlugin.getName()).thenReturn("MockPlugin");
        when(mockPlugin.isSupportRenderingFor(any())).thenReturn(true);
        when(mockBidResponse.gePreferredCustomRendererName()).thenReturn("MockPlugin");

        // When
        instance.registerPlugin(mockPlugin);

        // Then
        PrebidMobilePluginCustomRenderer preferredRendered = instance.getPluginForPreferredRenderer(mockBidResponse);
        assertEquals(mockPlugin.getName(), preferredRendered.getName());
    }

    @Test
    public void getPluginForPreferredRenderer_withNoMatchingRenderer_returnsPrebidRenderer() {
        // Given
        when(mockPlugin.getName()).thenReturn("MockPlugin");
        when(mockPlugin.isSupportRenderingFor(any())).thenReturn(true);
        when(mockBidResponse.gePreferredCustomRendererName()).thenReturn("NoMatchingPluginName");

        // When
        instance.registerPlugin(mockPlugin);

        // Then
        PrebidMobilePluginCustomRenderer preferredRendered = instance.getPluginForPreferredRenderer(mockBidResponse);
        assertEquals(PREBID_MOBILE_RENDERER_NAME, preferredRendered.getName());
    }

    @Test
    public void unregisterPlugin_withExistingPlugin_returnsEmptyList() {
        // Given
        when(mockPlugin.getName()).thenReturn("MockPlugin");
        when(mockPlugin.isSupportRenderingFor(mockAdUnitConfiguration)).thenReturn(true);

        // When
        instance.registerPlugin(mockPlugin);
        instance.unregisterPlugin(mockPlugin);
        List<String> result = instance.getRTBListOfCustomRenderersFor(mockAdUnitConfiguration);

        // Then
        assertTrue(result.isEmpty());
    }
}
