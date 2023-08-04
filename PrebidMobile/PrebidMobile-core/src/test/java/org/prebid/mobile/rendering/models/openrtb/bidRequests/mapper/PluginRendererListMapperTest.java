package org.prebid.mobile.rendering.models.openrtb.bidRequests.mapper;

import static org.junit.Assert.*;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Test;
import org.prebid.mobile.api.rendering.PrebidMobileInterstitialControllerInterface;
import org.prebid.mobile.api.rendering.pluginrenderer.PluginEventListener;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayVideoListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.PluginRenderer;

import java.util.ArrayList;
import java.util.List;

public class PluginRendererListMapperTest {

    String pluginName;
    String pluginVersion;
    String pluginToken;

    PrebidMobilePluginRenderer testPlugin = new PrebidMobilePluginRenderer() {
        @Override
        public String getName() {
            return pluginName;
        }

        @Override
        public String getVersion() {
            return pluginVersion;
        }

        @Nullable
        @Override
        public String getToken() {
            return pluginToken;
        }

        @Override
        public void registerEventListener(PluginEventListener pluginEventListener, String listenerKey) { }

        @Override
        public void unregisterEventListener(String listenerKey) { }

        @Override
        public View createBannerAdView(@NonNull Context context, @NonNull DisplayViewListener displayViewListener, @Nullable DisplayVideoListener displayVideoListener, @NonNull AdUnitConfiguration adUnitConfiguration, @NonNull BidResponse bidResponse) {
            return null;
        }

        @Override
        public PrebidMobileInterstitialControllerInterface createInterstitialController(@NonNull Context context, @NonNull InterstitialControllerListener interstitialControllerListener, @NonNull AdUnitConfiguration adUnitConfiguration, @NonNull BidResponse bidResponse) {
            return null;
        }

        @Override
        public boolean isSupportRenderingFor(AdUnitConfiguration adUnitConfiguration) {
            return false;
        }
    };

    @Test
    public void whenMap_valuesAreCorrect() {
        // Given
        PluginRendererListMapper mapper = new PluginRendererListMapper();
        List<PrebidMobilePluginRenderer> pluginList = new ArrayList<>();
        pluginList.add(testPlugin);
        pluginName = "name";
        pluginVersion = "1.0";
        pluginToken = "token";

        // When
        List<PluginRenderer> result = mapper.map(pluginList);
        PluginRenderer mappedRenderer = result.get(0);

        // Then
        assertEquals(mappedRenderer.getName(), pluginName);
        assertEquals(mappedRenderer.getVersion(), pluginVersion);
        assertEquals(mappedRenderer.getToken(), pluginToken);
    }
}