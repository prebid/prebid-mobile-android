package org.prebid.mobile.admob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration;

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
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = 29)
public class PrebidRewardedAdapterTest {

    private static final String RESPONSE_ID = "test-response-id-rewarded";
    private static final String CUSTOM_RENDERER = "CustomRenderer";
    private static final String CUSTOM_VERSION = "2.0";

    private static final String SERVER_PARAM_KEY = "parameter";
    private static final String SERVER_PARAM_VALUE = "{}";

    @SuppressWarnings("unchecked")
    private final MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback> mockLoadCallback =
            mock(MediationAdLoadCallback.class);

    private PrebidRewardedAdapter adapter;
    private MediationRewardedAdConfiguration mockConfig;
    private Context mockContext;
    private List<PrebidMobilePluginRenderer> registeredRenderers;

    @Before
    public void setUp() {
        adapter = new PrebidRewardedAdapter();
        mockContext = mock(Context.class);
        mockConfig = mock(MediationRewardedAdConfiguration.class);
        registeredRenderers = new ArrayList<>();
        when(mockConfig.getContext()).thenReturn(mockContext);

        Bundle serverParameters = new Bundle();
        serverParameters.putString(SERVER_PARAM_KEY, SERVER_PARAM_VALUE);
        when(mockConfig.getServerParameters()).thenReturn(serverParameters);

        Bundle extras = new Bundle();
        extras.putString(PrebidRewardedAdapter.EXTRA_RESPONSE_ID, RESPONSE_ID);
        when(mockConfig.getMediationExtras()).thenReturn(extras);
    }

    @After
    public void tearDown() {
        for (PrebidMobilePluginRenderer renderer : registeredRenderers) {
            PrebidMobilePluginRegister.getInstance().unregisterPlugin(renderer);
        }
    }

    @Test
    public void loadRewardedAd_noResponseInCache_callsOnFailure() {
        Bundle extras = new Bundle();
        extras.putString(PrebidRewardedAdapter.EXTRA_RESPONSE_ID, "unknown-id-rewarded-never-cached");
        when(mockConfig.getMediationExtras()).thenReturn(extras);

        adapter.loadRewardedAd(mockConfig, mockLoadCallback);

        verify(mockLoadCallback).onFailure(any());
    }

    @Test
    public void loadRewardedAd_defaultRenderer_usesDefaultRenderer() {
        buildAndCacheBidResponse(null, null);

        PrebidMobileInterstitialControllerInterface mockController =
                mock(PrebidMobileInterstitialControllerInterface.class);
        PrebidMobilePluginRenderer defaultRenderer = buildRenderer(
                PREBID_MOBILE_RENDERER_NAME, "1.0", true, mockController);
        registerPlugin(defaultRenderer);

        adapter.loadRewardedAd(mockConfig, mockLoadCallback);

        verify(defaultRenderer).createInterstitialController(any(), any(), any(), any());
        verify(mockLoadCallback, never()).onFailure(any());
    }

    @Test
    public void loadRewardedAd_rewardedConfigPassedToRenderer() {
        buildAndCacheBidResponse(null, null);

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

        adapter.loadRewardedAd(mockConfig, mockLoadCallback);

        assertNotNull(capturedConfig[0]);
        assertEquals(true, capturedConfig[0].isRewarded());
        assertNotNull(capturedConfig[0].getRewardManager().getRewardListener());
    }

    @Test
    public void loadRewardedAd_customRenderer_delegatesToCustomRenderer() {
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

        adapter.loadRewardedAd(mockConfig, mockLoadCallback);

        verify(customRenderer).createInterstitialController(any(), any(), any(), any());
        verify(defaultRenderer, never()).createInterstitialController(any(), any(), any(), any());

    }

    @Test
    public void loadRewardedAd_rendererReturnsNull_callsOnFailure() {
        buildAndCacheBidResponse(null, null);

        PrebidMobilePluginRenderer nullRenderer = buildRenderer(
                PREBID_MOBILE_RENDERER_NAME, "1.0", true, null);
        registerPlugin(nullRenderer);

        adapter.loadRewardedAd(mockConfig, mockLoadCallback);

        verify(mockLoadCallback).onFailure(any());
    }


    private void buildAndCacheBidResponse(String rendererName, String rendererVersion) {
        BidResponse mockResponse = mock(BidResponse.class);
        when(mockResponse.getId()).thenReturn(RESPONSE_ID);
        when(mockResponse.getPreferredPluginRendererName()).thenReturn(rendererName);
        when(mockResponse.getPreferredPluginRendererVersion()).thenReturn(rendererVersion);
        when(mockResponse.getTargeting()).thenReturn(new HashMap<>());
        AdUnitConfiguration config = new AdUnitConfiguration();
        when(mockResponse.getAdUnitConfiguration()).thenReturn(config);
        BidResponseCache.getInstance().putBidResponse(RESPONSE_ID, mockResponse);
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
