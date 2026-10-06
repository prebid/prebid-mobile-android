package org.prebid.mobile.rendering.bidding.display;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.rendering.PrebidMobileInterstitialControllerInterface;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.networking.WinNotifier;
import org.prebid.mobile.testutils.FakePrebidMobilePluginRenderer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class PluginRendererFactoryTest {

    private static final String CUSTOM_RENDERER_NAME = "CustomRenderer";
    private static final String CUSTOM_RENDERER_VERSION = "2.0";

    private Context context;
    private InterstitialControllerListener mockListener;
    private AdUnitConfiguration config;
    private BidResponse mockBidResponse;
    private List<PrebidMobilePluginRenderer> registeredRenderers;

    @Before
    public void setUp() {
        context = Robolectric.buildActivity(Activity.class).create().get();
        mockListener = mock(InterstitialControllerListener.class);
        config = spy(new AdUnitConfiguration());
        mockBidResponse = mock(BidResponse.class);
        registeredRenderers = new ArrayList<>();

        // InterstitialController implements PrebidMobileInterstitialControllerInterface
        InterstitialController mockDefaultController = mock(InterstitialController.class);
        PrebidMobilePluginRenderer defaultRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        mockDefaultController, new View(context), true,
                        PREBID_MOBILE_RENDERER_NAME, "1.0"
                )
        );
        registerPlugin(defaultRenderer);
    }

    @After
    public void tearDown() {
        for (PrebidMobilePluginRenderer renderer : registeredRenderers) {
            PrebidMobilePluginRegister.getInstance().unregisterPlugin(renderer);
        }
    }

    @Test
    public void createInterstitialController_defaultRenderer_returnsController() {
        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn(null);
        when(mockBidResponse.getAdUnitConfiguration()).thenReturn(config);

        PrebidMobileInterstitialControllerInterface result =
                PluginRendererFactory.createInterstitialController(context, mockListener, config, mockBidResponse);

        assertNotNull(result);
        verify(config).modifyUsingBidResponse(mockBidResponse);
    }

    @Test
    public void createInterstitialController_customRendererMatches_usesCustomRenderer() {
        InterstitialController customController = mock(InterstitialController.class);
        PrebidMobilePluginRenderer customRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        customController, null, true,
                        CUSTOM_RENDERER_NAME, CUSTOM_RENDERER_VERSION
                )
        );
        registerPlugin(customRenderer);

        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn(CUSTOM_RENDERER_NAME);
        when(mockBidResponse.getPreferredPluginRendererVersion()).thenReturn(CUSTOM_RENDERER_VERSION);
        when(mockBidResponse.getAdUnitConfiguration()).thenReturn(config);

        PrebidMobileInterstitialControllerInterface result =
                PluginRendererFactory.createInterstitialController(context, mockListener, config, mockBidResponse);

        assertNotNull(result);
        verify(customRenderer).createInterstitialController(any(), any(), any(), any());
    }

    @Test
    public void createInterstitialController_customRendererReturnsNull_fallsBackToDefault() {
        PrebidMobilePluginRenderer customRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, null, true,
                        CUSTOM_RENDERER_NAME, CUSTOM_RENDERER_VERSION
                )
        );
        registerPlugin(customRenderer);

        config.setAdFormat(AdFormat.INTERSTITIAL);
        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn(CUSTOM_RENDERER_NAME);
        when(mockBidResponse.getPreferredPluginRendererVersion()).thenReturn(CUSTOM_RENDERER_VERSION);
        when(mockBidResponse.getAdUnitConfiguration()).thenReturn(config);

        PrebidMobileInterstitialControllerInterface result =
                PluginRendererFactory.createInterstitialController(context, mockListener, config, mockBidResponse);

        assertNotNull(result);
        verify(customRenderer).createInterstitialController(any(), any(), any(), any());
    }

    @Test
    public void createBannerAdView_customRendererMatches_usesCustomRenderer() {
        View customBannerView = new View(context);
        PrebidMobilePluginRenderer customRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, customBannerView, true,
                        CUSTOM_RENDERER_NAME, CUSTOM_RENDERER_VERSION
                )
        );
        registerPlugin(customRenderer);

        config.setAdFormat(AdFormat.BANNER);
        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn(CUSTOM_RENDERER_NAME);
        when(mockBidResponse.getPreferredPluginRendererVersion()).thenReturn(CUSTOM_RENDERER_VERSION);
        when(mockBidResponse.getAdUnitConfiguration()).thenReturn(config);

        View result = PluginRendererFactory.createBannerAdView(
                context,
                mock(org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener.class),
                null,
                config,
                mockBidResponse
        );

        assertSame(customBannerView, result);
        verify(customRenderer).createBannerAdView(any(), any(), any(), any(), any());
    }

    @Test
    public void createBannerAdView_customRendererReturnsNull_fallsBackToDefault() {
        PrebidMobilePluginRenderer customRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, null, true,
                        CUSTOM_RENDERER_NAME, CUSTOM_RENDERER_VERSION
                )
        );
        registerPlugin(customRenderer);

        config.setAdFormat(AdFormat.BANNER);
        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn(CUSTOM_RENDERER_NAME);
        when(mockBidResponse.getPreferredPluginRendererVersion()).thenReturn(CUSTOM_RENDERER_VERSION);
        when(mockBidResponse.getAdUnitConfiguration()).thenReturn(config);

        View result = PluginRendererFactory.createBannerAdView(
                context,
                mock(org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener.class),
                null,
                config,
                mockBidResponse
        );

        assertNotNull(result);
        verify(customRenderer).createBannerAdView(any(), any(), any(), any(), any());
    }

    @Test
    public void createInterstitialController_versionMismatch_fallsBackToDefault() {
        PrebidMobilePluginRenderer customRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, null, true,
                        CUSTOM_RENDERER_NAME, CUSTOM_RENDERER_VERSION
                )
        );
        registerPlugin(customRenderer);

        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn(CUSTOM_RENDERER_NAME);
        when(mockBidResponse.getPreferredPluginRendererVersion()).thenReturn("9.9");
        when(mockBidResponse.getAdUnitConfiguration()).thenReturn(config);

        PrebidMobileInterstitialControllerInterface result =
                PluginRendererFactory.createInterstitialController(context, mockListener, config, mockBidResponse);

        assertNotNull(result);
        verify(customRenderer, never()).createInterstitialController(any(), any(), any(), any());
    }

    @Test
    public void createInterstitialController_unknownRenderer_fallsBackToDefault() {
        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn("NoSuchRenderer");
        when(mockBidResponse.getAdUnitConfiguration()).thenReturn(config);

        PrebidMobileInterstitialControllerInterface result =
                PluginRendererFactory.createInterstitialController(context, mockListener, config, mockBidResponse);

        assertNotNull(result);
    }

    @Test
    public void createInterstitialController_rewardedConfig_rewardListenerPreserved() {
        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn(null);
        when(mockBidResponse.getAdUnitConfiguration()).thenReturn(config);

        Runnable rewardCallback = mock(Runnable.class);
        config.setRewarded(true);
        config.getRewardManager().setRewardListener(rewardCallback);

        PrebidMobileInterstitialControllerInterface result =
                PluginRendererFactory.createInterstitialController(context, mockListener, config, mockBidResponse);

        assertNotNull(result);
        assertEquals(rewardCallback, config.getRewardManager().getRewardListener());
    }

    @Test
    public void createInterstitialController_rendererNotSupportingAdUnit_fallsBackToDefault() {
        AdUnitConfiguration vastConfig = new AdUnitConfiguration();
        vastConfig.setAdFormat(AdFormat.VAST);

        PrebidMobilePluginRenderer limitedRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, null, false,
                        CUSTOM_RENDERER_NAME, CUSTOM_RENDERER_VERSION
                )
        );
        registerPlugin(limitedRenderer);

        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn(CUSTOM_RENDERER_NAME);
        when(mockBidResponse.getPreferredPluginRendererVersion()).thenReturn(CUSTOM_RENDERER_VERSION);
        when(mockBidResponse.getAdUnitConfiguration()).thenReturn(vastConfig);

        PrebidMobileInterstitialControllerInterface result =
                PluginRendererFactory.createInterstitialController(context, mockListener, vastConfig, mockBidResponse);

        assertNotNull(result);
        verify(limitedRenderer, never()).createInterstitialController(any(), any(), any(), any());
    }

    @Test
    public void createInterstitialController_customRendererOwnController_sendsWinNotice() {
        PrebidMobileInterstitialControllerInterface pluginController = mock(PrebidMobileInterstitialControllerInterface.class);
        PrebidMobilePluginRenderer customRenderer = registerCustomRenderer(pluginController, false);

        try (MockedConstruction<WinNotifier> winNotifiers = mockConstruction(WinNotifier.class)) {
            PrebidMobileInterstitialControllerInterface result =
                    PluginRendererFactory.createInterstitialController(context, mockListener, config, mockBidResponse);

            assertSame(pluginController, result);
            assertEquals(1, winNotifiers.constructed().size());
            verify(winNotifiers.constructed().get(0)).notifyWin(eq(mockBidResponse), any());
        }
        verify(customRenderer).createInterstitialController(any(), any(), any(), any());
    }

    @Test
    public void createInterstitialController_customRendererSendsWinNoticeItself_doesNotSendWinNotice() {
        PrebidMobileInterstitialControllerInterface pluginController = mock(PrebidMobileInterstitialControllerInterface.class);
        registerCustomRenderer(pluginController, true);

        try (MockedConstruction<WinNotifier> winNotifiers = mockConstruction(WinNotifier.class)) {
            PrebidMobileInterstitialControllerInterface result =
                    PluginRendererFactory.createInterstitialController(context, mockListener, config, mockBidResponse);

            assertSame(pluginController, result);
            assertTrue(winNotifiers.constructed().isEmpty());
        }
    }

    @Test
    public void createInterstitialController_customRendererReturnsInterstitialController_doesNotSendWinNotice() {
        // Prebid's InterstitialController sends the win notice itself in loadAd().
        InterstitialController prebidController = mock(InterstitialController.class);
        registerCustomRenderer(prebidController, false);

        try (MockedConstruction<WinNotifier> winNotifiers = mockConstruction(WinNotifier.class)) {
            PrebidMobileInterstitialControllerInterface result =
                    PluginRendererFactory.createInterstitialController(context, mockListener, config, mockBidResponse);

            assertSame(prebidController, result);
            assertTrue(winNotifiers.constructed().isEmpty());
        }
    }

    @Test
    public void createInterstitialController_defaultRenderer_doesNotSendWinNotice() {
        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn(null);
        when(mockBidResponse.getAdUnitConfiguration()).thenReturn(config);

        try (MockedConstruction<WinNotifier> winNotifiers = mockConstruction(WinNotifier.class)) {
            PrebidMobileInterstitialControllerInterface result =
                    PluginRendererFactory.createInterstitialController(context, mockListener, config, mockBidResponse);

            assertTrue(result instanceof InterstitialController);
            assertTrue(winNotifiers.constructed().isEmpty());
        }
    }

    @Test
    public void createInterstitialController_customRendererReturnsNull_doesNotSendWinNotice() {
        registerCustomRenderer(null, false);

        try (MockedConstruction<WinNotifier> winNotifiers = mockConstruction(WinNotifier.class)) {
            PrebidMobileInterstitialControllerInterface result =
                    PluginRendererFactory.createInterstitialController(context, mockListener, config, mockBidResponse);

            assertTrue(result instanceof InterstitialController);
            assertTrue(winNotifiers.constructed().isEmpty());
        }
    }

    @Test
    public void createBannerAdView_customRendererMatches_doesNotSendWinNotice() {
        // Banner containers send the win notice before they call the factory.
        View customBannerView = new View(context);
        PrebidMobilePluginRenderer customRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, customBannerView, true,
                        CUSTOM_RENDERER_NAME, CUSTOM_RENDERER_VERSION
                )
        );
        registerPlugin(customRenderer);
        stubCustomRendererBid();

        try (MockedConstruction<WinNotifier> winNotifiers = mockConstruction(WinNotifier.class)) {
            View result = PluginRendererFactory.createBannerAdView(
                    context, mock(DisplayViewListener.class), null, config, mockBidResponse
            );

            assertSame(customBannerView, result);
            assertTrue(winNotifiers.constructed().isEmpty());
        }
    }

    @Test
    public void createBannerAdView_customRendererReturnsNull_doesNotSendWinNotice() {
        registerPlugin(spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, null, true,
                        CUSTOM_RENDERER_NAME, CUSTOM_RENDERER_VERSION
                )
        ));
        stubCustomRendererBid();

        try (MockedConstruction<WinNotifier> winNotifiers = mockConstruction(WinNotifier.class)) {
            View result = PluginRendererFactory.createBannerAdView(
                    context, mock(DisplayViewListener.class), null, config, mockBidResponse
            );

            assertNotNull(result);
            assertTrue(winNotifiers.constructed().isEmpty());
        }
    }

    @Test
    public void createBannerAdView_customRendererSendsWinNoticeItselfReturnsNull_sendsWinNoticeForDefaultRenderer() {
        // The banner container skipped the notice for this renderer, and Prebid's banner view does not send it.
        PrebidMobilePluginRenderer customRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, null, true,
                        CUSTOM_RENDERER_NAME, CUSTOM_RENDERER_VERSION
                )
        );
        doReturn(true).when(customRenderer).sendsWinNotice();
        registerPlugin(customRenderer);
        stubCustomRendererBid();

        try (MockedConstruction<WinNotifier> winNotifiers = mockConstruction(WinNotifier.class)) {
            View result = PluginRendererFactory.createBannerAdView(
                    context, mock(DisplayViewListener.class), null, config, mockBidResponse
            );

            assertNotNull(result);
            assertEquals(1, winNotifiers.constructed().size());
            verify(winNotifiers.constructed().get(0)).notifyWin(eq(mockBidResponse), any());
        }
    }

    @Test
    public void preferredRendererSendsWinNotice_followsPreferredRenderer() {
        PrebidMobilePluginRenderer customRenderer = registerCustomRenderer(null, false);
        assertFalse(PluginRendererFactory.preferredRendererSendsWinNotice(mockBidResponse, config));

        doReturn(true).when(customRenderer).sendsWinNotice();
        assertTrue(PluginRendererFactory.preferredRendererSendsWinNotice(mockBidResponse, config));

        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn(null);
        assertFalse(PluginRendererFactory.preferredRendererSendsWinNotice(mockBidResponse, config));
    }

    /**
     * Registers a custom renderer for interstitials that the bid names, returning {@code controller}.
     */
    private PrebidMobilePluginRenderer registerCustomRenderer(
            PrebidMobileInterstitialControllerInterface controller,
            boolean sendsWinNotice
    ) {
        PrebidMobilePluginRenderer customRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, null, true,
                        CUSTOM_RENDERER_NAME, CUSTOM_RENDERER_VERSION
                )
        );
        doReturn(controller).when(customRenderer).createInterstitialController(any(), any(), any(), any());
        doReturn(sendsWinNotice).when(customRenderer).sendsWinNotice();
        registerPlugin(customRenderer);
        stubCustomRendererBid();
        return customRenderer;
    }

    private void stubCustomRendererBid() {
        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn(CUSTOM_RENDERER_NAME);
        when(mockBidResponse.getPreferredPluginRendererVersion()).thenReturn(CUSTOM_RENDERER_VERSION);
        when(mockBidResponse.getAdUnitConfiguration()).thenReturn(config);
    }

    private void registerPlugin(PrebidMobilePluginRenderer renderer) {
        PrebidMobilePluginRegister.getInstance().registerPlugin(renderer);
        registeredRenderers.add(renderer);
    }
}
