package org.prebid.mobile.api.rendering.pluginrenderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.List;

public class PrebidMobilePluginRegisterTest {

    private PrebidMobilePluginRegister instance;
    private PrebidMobilePluginRenderer mockPlugin;
    private BidResponse mockBidResponse;
    private AdUnitConfiguration mockAdUnitConfiguration;

    @Before
    public void setUp() {
        instance = PrebidMobilePluginRegister.getInstance();
        mockPlugin = mock(PrebidMobilePluginRenderer.class);
        mockBidResponse = mock(BidResponse.class);
        mockAdUnitConfiguration = mock(AdUnitConfiguration.class);

        // Default prebid renderer init is expected
        PrebidMobilePluginRenderer mockPrebidPlugin = mock(PrebidMobilePluginRenderer.class);
        when(mockPrebidPlugin.getName()).thenReturn(PREBID_MOBILE_RENDERER_NAME);
        instance.registerPlugin(mockPrebidPlugin);
    }

    @Test
    public void getRTBListOfCustomRenderersFor_withNoMatchingRenderers_returnsEmptyList() {
        // Given
        when(mockPlugin.getName()).thenReturn("MockPlugin");
        when(mockPlugin.isSupportRenderingFor(mockAdUnitConfiguration)).thenReturn(false);
        PrebidMobilePluginRenderer mockPlugin2 = mock(PrebidMobilePluginRenderer.class);
        when(mockPlugin2.getName()).thenReturn("MockPlugin2");
        when(mockPlugin2.isSupportRenderingFor(mockAdUnitConfiguration)).thenReturn(false);

        // When
        instance.registerPlugin(mockPlugin);
        instance.registerPlugin(mockPlugin2);
        List<PrebidMobilePluginRenderer> result = instance.getRTBListOfRenderersFor(mockAdUnitConfiguration);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    public void getRTBListOfCustomRenderersFor_withMatchingRenderer_returnsListWithOneOccurrence() {
        // Given
        when(mockPlugin.isSupportRenderingFor(mockAdUnitConfiguration)).thenReturn(true);
        when(mockPlugin.getName()).thenReturn("MockPlugin");
        PrebidMobilePluginRenderer mockPlugin2 = mock(PrebidMobilePluginRenderer.class);
        when(mockPlugin2.getName()).thenReturn("MockPlugin2");
        when(mockPlugin2.isSupportRenderingFor(mockAdUnitConfiguration)).thenReturn(false);

        // When
        instance.registerPlugin(mockPlugin);
        instance.registerPlugin(mockPlugin2);
        List<PrebidMobilePluginRenderer> result = instance.getRTBListOfRenderersFor(mockAdUnitConfiguration);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    public void getRTBListOfCustomRenderersFor_withMatchingRenderers_returnsList() {
        // Given
        when(mockPlugin.isSupportRenderingFor(mockAdUnitConfiguration)).thenReturn(true);
        when(mockPlugin.getName()).thenReturn("MockPlugin");
        PrebidMobilePluginRenderer mockPlugin2 = mock(PrebidMobilePluginRenderer.class);
        when(mockPlugin2.getName()).thenReturn("MockPlugin2");
        when(mockPlugin2.isSupportRenderingFor(mockAdUnitConfiguration)).thenReturn(true);

        // When
        instance.registerPlugin(mockPlugin);
        instance.registerPlugin(mockPlugin2);
        List<PrebidMobilePluginRenderer> result = instance.getRTBListOfRenderersFor(mockAdUnitConfiguration);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    public void getPluginForPreferredRenderer_withMatchingRenderer_returnsPreferredRenderer() {
        // Given
        when(mockPlugin.getName()).thenReturn("MockPlugin");
        when(mockPlugin.getVersion()).thenReturn("1.0");
        when(mockPlugin.isSupportRenderingFor(any())).thenReturn(true);
        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn("MockPlugin");
        when(mockBidResponse.getPreferredPluginRendererVersion()).thenReturn("1.0");

        // When
        instance.registerPlugin(mockPlugin);

        // Then
        PrebidMobilePluginRenderer preferredRendered = instance.getPluginForPreferredRenderer(mockBidResponse);
        assertEquals(mockPlugin.getName(), preferredRendered.getName());
    }

    @Test
    public void getPluginForPreferredRenderer_withDifferentRendererVersion_returnsPrebidRenderer() {
        // Given
        when(mockPlugin.getName()).thenReturn("MockPlugin");
        when(mockPlugin.getVersion()).thenReturn("1.0");
        when(mockPlugin.isSupportRenderingFor(any())).thenReturn(true);
        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn("MockPlugin");
        when(mockBidResponse.getPreferredPluginRendererVersion()).thenReturn("2.0"); // version from bid response is greater

        // When
        instance.registerPlugin(mockPlugin);

        // Then
        PrebidMobilePluginRenderer preferredRendered = instance.getPluginForPreferredRenderer(mockBidResponse);
        assertEquals(PREBID_MOBILE_RENDERER_NAME, preferredRendered.getName());
    }

    @Test
    public void getPluginForPreferredRenderer_withNoMatchingRenderer_returnsPrebidRenderer() {
        // Given
        when(mockPlugin.getName()).thenReturn("MockPlugin");
        when(mockPlugin.isSupportRenderingFor(any())).thenReturn(true);
        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn("NoMatchingPluginName");

        // When
        instance.registerPlugin(mockPlugin);

        // Then
        PrebidMobilePluginRenderer preferredRendered = instance.getPluginForPreferredRenderer(mockBidResponse);
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
        List<PrebidMobilePluginRenderer> result = instance.getRTBListOfRenderersFor(mockAdUnitConfiguration);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    public void containsPlugin_withPluginInstance_returnsTrue() {
        // Given
        when(mockPlugin.getName()).thenReturn("MockPlugin");

        // When
        instance.registerPlugin(mockPlugin);
        Boolean result = instance.containsPlugin(mockPlugin);

        // Then
        assertTrue(result);
    }

    @Test
    public void containsPlugin_withPluginInstance_returnsFalse() {
        // Given
        when(mockPlugin.getName()).thenReturn("MockPlugin");
        PrebidMobilePluginRenderer otherMockPlugin = Mockito.mock(PrebidMobilePluginRenderer.class);

        // When
        instance.registerPlugin(mockPlugin);
        Boolean result = instance.containsPlugin(otherMockPlugin);

        // Then
        assertFalse(result);
    }

    @Test
    public void containsPlugin_withPluginName_returnsTrue() {
        // Given
        String pluginName = "MockPlugin";
        when(mockPlugin.getName()).thenReturn(pluginName);

        // When
        instance.registerPlugin(mockPlugin);
        Boolean result = instance.containsPlugin(pluginName);

        // Then
        assertTrue(result);
    }

    @Test
    public void containsPlugin_withPluginName_returnsFalse() {
        // Given
        String pluginName = "MockPlugin";
        when(mockPlugin.getName()).thenReturn(pluginName);

        // When
        instance.registerPlugin(mockPlugin);
        Boolean result = instance.containsPlugin("OtherPluginName");

        // Then
        assertFalse(result);
    }

    @Test
    public void registerEventListener_withExistedPlugin_triggerRegisteringCorrectly() {
        // Given
        String pluginName = "MockPlugin";
        String fakeFingerprint = "fingerprint";
        PluginEventListener fakePluginEventListener = () -> pluginName;
        when(mockPlugin.getName()).thenReturn(pluginName);

        // When
        instance.registerPlugin(mockPlugin);
        instance.registerEventListener(fakePluginEventListener, fakeFingerprint);

        // Then
        verify(mockPlugin, times(1)).registerEventListener(fakePluginEventListener, fakeFingerprint);
    }

    @Test
    public void registerEventListener_withWrongPluginEventListerName_failsOnRegistering() {
        // Given
        String pluginName = "MockPlugin";
        String fakeFingerprint = "fingerprint";
        PluginEventListener fakePluginEventListener = () -> "";
        when(mockPlugin.getName()).thenReturn(pluginName);

        // When
        instance.registerPlugin(mockPlugin);
        instance.registerEventListener(fakePluginEventListener, fakeFingerprint);

        // Then
        verify(mockPlugin, times(0)).registerEventListener(fakePluginEventListener, fakeFingerprint);
    }

    @Test
    public void unregisterEventListener_triggerUnregisteringCorrectly() {
        // Given
        String fakeFingerprint = "fingerprint";

        // When
        instance.registerPlugin(mockPlugin);
        instance.unregisterEventListener(fakeFingerprint);

        // Then
        verify(mockPlugin, times(1)).unregisterEventListener(fakeFingerprint);
    }
}
