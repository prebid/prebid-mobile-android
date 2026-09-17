package org.prebid.mobile.rendering.bidding.display;

import static org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.api.rendering.PrebidRenderer;
import org.prebid.mobile.api.rendering.PrebidMobileInterstitialControllerInterface;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayVideoListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;

/**
 * Resolves the preferred plugin renderer from bid metadata and delegates ad component
 * creation to it. Falls back to the default Prebid SDK renderer when no custom renderer
 * matches the bid response or the preferred renderer cannot create an ad component.
 */
public class PluginRendererFactory {

    /**
     * Creates the renderer-owned banner view after the caller has completed win notification.
     * Mediation banner containers such as {@link MediationBannerView} and legacy DisplayView
     * call {@link org.prebid.mobile.rendering.networking.WinNotifier} before invoking this method.
     */
    @Nullable
    public static View createBannerAdView(
            @NonNull Context context,
            @NonNull DisplayViewListener displayViewListener,
            @Nullable DisplayVideoListener displayVideoListener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    ) {
        adUnitConfiguration.modifyUsingBidResponse(bidResponse);
        PrebidMobilePluginRenderer renderer = getPreferredRenderer(bidResponse, adUnitConfiguration);
        if (renderer == null) {
            return null;
        }

        View adView = renderer.createBannerAdView(context, displayViewListener, displayVideoListener, adUnitConfiguration, bidResponse);
        if (adView != null) {
            return adView;
        }

        if (PREBID_MOBILE_RENDERER_NAME.equals(renderer.getName())) {
            return null;
        }
        return createDefaultRenderer().createBannerAdView(
                context,
                displayViewListener,
                displayVideoListener,
                adUnitConfiguration,
                bidResponse
        );
    }

    @Nullable
    public static PrebidMobileInterstitialControllerInterface createInterstitialController(
            @NonNull Context context,
            @NonNull InterstitialControllerListener listener,
            @NonNull AdUnitConfiguration adUnitConfiguration,
            @NonNull BidResponse bidResponse
    ) {
        adUnitConfiguration.modifyUsingBidResponse(bidResponse);
        PrebidMobilePluginRenderer renderer = getPreferredRenderer(bidResponse, adUnitConfiguration);
        if (renderer == null) {
            return null;
        }

        PrebidMobileInterstitialControllerInterface controller = renderer.createInterstitialController(
                context,
                listener,
                adUnitConfiguration,
                bidResponse
        );
        if (controller != null) {
            return controller;
        }

        if (PREBID_MOBILE_RENDERER_NAME.equals(renderer.getName())) {
            return null;
        }
        return createDefaultRenderer().createInterstitialController(context, listener, adUnitConfiguration, bidResponse);
    }

    @Nullable
    private static PrebidMobilePluginRenderer getPreferredRenderer(
            @NonNull BidResponse bidResponse,
            @NonNull AdUnitConfiguration adUnitConfiguration
    ) {
        return PrebidMobilePluginRegister.getInstance().getPluginForPreferredRenderer(bidResponse, adUnitConfiguration);
    }

    @NonNull
    private static PrebidMobilePluginRenderer createDefaultRenderer() {
        PrebidMobilePluginRenderer registeredDefaultRenderer = PrebidMobilePluginRegister.getInstance().getDefaultPluginRenderer();
        if (registeredDefaultRenderer != null) {
            return registeredDefaultRenderer;
        }
        return new PrebidRenderer();
    }
}
