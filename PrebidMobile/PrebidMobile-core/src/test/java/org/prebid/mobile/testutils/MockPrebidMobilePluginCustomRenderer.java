package org.prebid.mobile.testutils;

import static org.prebid.mobile.api.rendering.customrenderer.PluginRegisterCustomRenderer.PREBID_MOBILE_RENDERER_NAME;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.api.rendering.customrenderer.PrebidMobileInterstitialControllerInterface;
import org.prebid.mobile.api.rendering.customrenderer.PrebidMobilePluginCustomRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;

public class MockPrebidMobilePluginCustomRenderer {
    public static PrebidMobilePluginCustomRenderer getMockPrebidRenderer(
            InterstitialController mockInterstitialController,
            View mockBannerAdView,
            Boolean isSupportRenderingFor
    ) {
        return new PrebidMobilePluginCustomRenderer() {
            @Override
            public String getName() { return PREBID_MOBILE_RENDERER_NAME; }

            @Override
            public String getVersion() { return null; }

            @Nullable
            @Override
            public String getToken() { return null; }

            @Override
            public View createBannerAdView(
                    @NonNull Context context,
                    @NonNull DisplayViewListener displayViewListener,
                    @NonNull AdUnitConfiguration adUnitConfiguration,
                    @NonNull BidResponse bidResponse
            ) {
                return mockBannerAdView;
            }

            @Override
            public PrebidMobileInterstitialControllerInterface createInterstitialController(
                    @NonNull Context context,
                    @NonNull InterstitialControllerListener interstitialControllerListener,
                    @NonNull AdUnitConfiguration adUnitConfiguration,
                    @NonNull BidResponse bidResponse
            ) {
                return mockInterstitialController;
            }

            @Override
            public boolean isSupportRenderingFor(AdUnitConfiguration adUnitConfiguration) {
                return isSupportRenderingFor;
            }
        };
    }
}
