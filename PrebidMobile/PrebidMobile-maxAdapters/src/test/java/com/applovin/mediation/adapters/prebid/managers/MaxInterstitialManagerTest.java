package com.applovin.mediation.adapters.prebid.managers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME;

import android.app.Activity;
import android.os.Bundle;

import com.applovin.mediation.adapter.listeners.MaxInterstitialAdapterListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.mediation.adapters.PrebidMaxMediationAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.api.rendering.PrebidMobileInterstitialControllerInterface;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = 29)
public class MaxInterstitialManagerTest {

    private static final String RESPONSE_ID = "test-interstitial-id-max";
    private static final String CUSTOM_RENDERER = "CustomRenderer";
    private static final String CUSTOM_VERSION = "2.0";
    // ParametersMatcher requires keywords and custom params to have matching key-value pairs
    private static final String KEYWORD_KEY = "hb_pb";
    private static final String KEYWORD_VALUE = "0.10";

    private MaxInterstitialManager manager;
    private Activity activity;
    private MaxInterstitialAdapterListener mockMaxListener;
    private MaxAdapterResponseParameters mockParams;
    private List<PrebidMobilePluginRenderer> registeredRenderers;

    @Before
    public void setUp() {
        manager = new MaxInterstitialManager();
        activity = Robolectric.buildActivity(Activity.class).create().get();
        mockMaxListener = mock(MaxInterstitialAdapterListener.class);
        mockParams = buildParams(RESPONSE_ID);
        registeredRenderers = new ArrayList<>();
    }

    @After
    public void tearDown() {
        for (PrebidMobilePluginRenderer renderer : registeredRenderers) {
            PrebidMobilePluginRegister.getInstance().unregisterPlugin(renderer);
        }
    }

    @Test
    public void loadAd_noResponseInCache_reportsError() {
        MaxAdapterResponseParameters unknownParams =
                buildParams("unknown-id-interstitial-never-cached");

        manager.loadAd(unknownParams, activity, mockMaxListener);

        verify(mockMaxListener).onInterstitialAdLoadFailed(any());
    }

    @Test
    public void loadAd_defaultRenderer_createsController() {
        buildAndCacheBidResponse(null, null);

        PrebidMobileInterstitialControllerInterface mockController =
                mock(PrebidMobileInterstitialControllerInterface.class);
        PrebidMobilePluginRenderer defaultRenderer = buildRenderer(
                PREBID_MOBILE_RENDERER_NAME, "1.0", true, mockController);
        registerPlugin(defaultRenderer);

        manager.loadAd(mockParams, activity, mockMaxListener);

        verify(defaultRenderer).createInterstitialController(any(), any(), any(), any());
        verify(mockMaxListener, never()).onInterstitialAdLoadFailed(any());
    }

    @Test
    public void loadAd_customRenderer_delegatesToCustomRenderer() {
        buildAndCacheBidResponse(CUSTOM_RENDERER, CUSTOM_VERSION);

        PrebidMobileInterstitialControllerInterface customController =
                mock(PrebidMobileInterstitialControllerInterface.class);
        PrebidMobilePluginRenderer customRenderer = buildRenderer(
                CUSTOM_RENDERER, CUSTOM_VERSION, true, customController);
        registerPlugin(customRenderer);

        PrebidMobileInterstitialControllerInterface defaultController =
                mock(PrebidMobileInterstitialControllerInterface.class);
        PrebidMobilePluginRenderer defaultRenderer = buildRenderer(
                PREBID_MOBILE_RENDERER_NAME, "1.0", true, defaultController);
        registerPlugin(defaultRenderer);

        manager.loadAd(mockParams, activity, mockMaxListener);

        verify(customRenderer).createInterstitialController(any(), any(), any(), any());
        verify(defaultRenderer, never()).createInterstitialController(any(), any(), any(), any());

    }

    @Test
    public void loadAd_rendererReturnsNull_reportsError() {
        buildAndCacheBidResponse(null, null);

        PrebidMobilePluginRenderer nullRenderer = buildRenderer(
                PREBID_MOBILE_RENDERER_NAME, "1.0", true, null);
        registerPlugin(nullRenderer);

        manager.loadAd(mockParams, activity, mockMaxListener);

        verify(mockMaxListener).onInterstitialAdLoadFailed(any());
    }


    // The adapter renders from a copy of the mediation ad unit's configuration, which the response
    // carries, so setIsMuted and setIsSoundButtonVisible on the ad unit reach the renderer.
    @Test
    public void loadAd_rendersFromACopyOfTheMediationAdUnitConfiguration() {
        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        adUnitConfiguration.setIsMuted(true);
        adUnitConfiguration.setIsSoundButtonVisible(true);
        buildAndCacheBidResponse(null, null, adUnitConfiguration);

        final AdUnitConfiguration[] capturedConfig = {null};
        PrebidMobilePluginRenderer capturingRenderer = mock(PrebidMobilePluginRenderer.class);
        when(capturingRenderer.getName()).thenReturn(PREBID_MOBILE_RENDERER_NAME);
        when(capturingRenderer.getVersion()).thenReturn("1.0");
        when(capturingRenderer.isSupportRenderingFor(any())).thenReturn(true);
        when(capturingRenderer.createInterstitialController(any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    capturedConfig[0] = invocation.getArgument(2);
                    return mock(PrebidMobileInterstitialControllerInterface.class);
                });
        registerPlugin(capturingRenderer);

        manager.loadAd(mockParams, activity, mockMaxListener);

        assertNotNull(capturedConfig[0]);
        assertNotSame("The ad unit's own configuration must not be handed to a renderer", adUnitConfiguration, capturedConfig[0]);
        assertTrue(capturedConfig[0].isMuted());
        assertTrue(capturedConfig[0].isSoundButtonVisible());
    }

    private void buildAndCacheBidResponse(String rendererName, String rendererVersion) {
        buildAndCacheBidResponse(rendererName, rendererVersion, new AdUnitConfiguration());
    }

    private void buildAndCacheBidResponse(String rendererName, String rendererVersion, AdUnitConfiguration adUnitConfiguration) {
        BidResponse mockResponse = mock(BidResponse.class);
        when(mockResponse.getId()).thenReturn(RESPONSE_ID);
        when(mockResponse.getPreferredPluginRendererName()).thenReturn(rendererName);
        when(mockResponse.getPreferredPluginRendererVersion()).thenReturn(rendererVersion);
        HashMap<String, String> targeting = new HashMap<>();
        targeting.put(KEYWORD_KEY, KEYWORD_VALUE);
        when(mockResponse.getTargeting()).thenReturn(targeting);
        when(mockResponse.getAdUnitConfiguration()).thenReturn(adUnitConfiguration);
        BidResponseCache.getInstance().putBidResponse(RESPONSE_ID, mockResponse);
    }

    // Custom params must match targeting returned by BidResponseCache.getKeywords()
    private MaxAdapterResponseParameters buildParams(String responseId) {
        MaxAdapterResponseParameters params = mock(MaxAdapterResponseParameters.class);
        Map<String, Object> extras = new HashMap<>();
        extras.put(PrebidMaxMediationAdapter.EXTRA_RESPONSE_ID, responseId);
        when(params.getLocalExtraParameters()).thenReturn(extras);
        Bundle customParams = new Bundle();
        customParams.putString(KEYWORD_KEY, KEYWORD_VALUE);
        when(params.getCustomParameters()).thenReturn(customParams);
        return params;
    }

    private PrebidMobilePluginRenderer buildRenderer(
            String name,
            String version,
            boolean supports,
            PrebidMobileInterstitialControllerInterface controller
    ) {
        PrebidMobilePluginRenderer renderer = mock(PrebidMobilePluginRenderer.class);
        when(renderer.getName()).thenReturn(name);
        when(renderer.getVersion()).thenReturn(version);
        when(renderer.isSupportRenderingFor(any())).thenReturn(supports);
        when(renderer.createInterstitialController(any(), any(), any(), any()))
                .thenReturn(controller);
        return renderer;
    }

    private void registerPlugin(PrebidMobilePluginRenderer renderer) {
        PrebidMobilePluginRegister.getInstance().registerPlugin(renderer);
        registeredRenderers.add(renderer);
    }
}
