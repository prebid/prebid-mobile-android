package org.prebid.mobile.rendering.bidding.display;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
import org.json.JSONObject;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.rendering.PrebidDestroyable;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.MobileSdkPassThrough;
import org.prebid.mobile.rendering.networking.WinNotifier;
import org.prebid.mobile.testutils.FakePrebidMobilePluginRenderer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MediationBannerViewTest {

    private static final String CUSTOM_RENDERER_NAME = "CustomRenderer";
    private static final String CUSTOM_RENDERER_VERSION = "2.0";

    private Context context;
    private AdUnitConfiguration adUnitConfiguration;
    private BidResponse mockBidResponse;
    private DisplayViewListener mockDisplayViewListener;
    private List<PrebidMobilePluginRenderer> registeredRenderers;

    private static class DestroyableView extends View implements PrebidDestroyable {
        boolean destroyed;

        DestroyableView(Context context) {
            super(context);
        }

        @Override
        public void destroy() {
            destroyed = true;
        }
    }

    @Before
    public void setUp() {
        context = Robolectric.buildActivity(Activity.class).create().get();
        adUnitConfiguration = new AdUnitConfiguration();
        adUnitConfiguration.setAdFormat(AdFormat.BANNER);
        mockDisplayViewListener = mock(DisplayViewListener.class);
        mockBidResponse = mock(BidResponse.class);
        registeredRenderers = new ArrayList<>();

        Bid mockBid = mock(Bid.class);
        when(mockBid.getAdm()).thenReturn("adm");
        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);
        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn(PREBID_MOBILE_RENDERER_NAME);
        when(mockBidResponse.getPreferredPluginRendererVersion()).thenReturn("1.0");
        when(mockBidResponse.getAdUnitConfiguration()).thenReturn(adUnitConfiguration);
    }

    @After
    public void tearDown() {
        for (PrebidMobilePluginRenderer renderer : registeredRenderers) {
            PrebidMobilePluginRegister.getInstance().unregisterPlugin(renderer);
        }
    }

    @Test
    public void constructor_defaultRenderer_addsViewToHierarchy() {
        View mockBannerView = mock(View.class);
        PrebidMobilePluginRenderer defaultRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, mockBannerView, true,
                        PREBID_MOBILE_RENDERER_NAME, "1.0"
                )
        );
        registerPlugin(defaultRenderer);

        MediationBannerView bannerView = new MediationBannerView(
                context, mockDisplayViewListener, adUnitConfiguration, mockBidResponse
        );

        verify(defaultRenderer).createBannerAdView(any(), any(), any(), any(), any());
        assertEquals(1, bannerView.getChildCount());
        verify(mockDisplayViewListener, never()).onAdFailed(any());
    }

    @Test
    public void constructor_customRenderer_delegatesToCustomRenderer() {
        View customView = new View(context);
        PrebidMobilePluginRenderer customRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, customView, true,
                        CUSTOM_RENDERER_NAME, CUSTOM_RENDERER_VERSION
                )
        );
        registerPlugin(customRenderer);

        View defaultView = mock(View.class);
        PrebidMobilePluginRenderer defaultRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, defaultView, true,
                        PREBID_MOBILE_RENDERER_NAME, "1.0"
                )
        );
        registerPlugin(defaultRenderer);

        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn(CUSTOM_RENDERER_NAME);
        when(mockBidResponse.getPreferredPluginRendererVersion()).thenReturn(CUSTOM_RENDERER_VERSION);

        MediationBannerView bannerView = new MediationBannerView(
                context, mockDisplayViewListener, adUnitConfiguration, mockBidResponse
        );

        verify(customRenderer).createBannerAdView(any(), any(), any(), any(), any());
        verify(defaultRenderer, never()).createBannerAdView(any(), any(), any(), any(), any());
        assertEquals(1, bannerView.getChildCount());

    }

    @Test
    public void constructor_rendererReturnsNullView_firesOnAdFailed() {
        // Custom renderer returns null; factory falls back to default renderer which also returns null
        PrebidMobilePluginRenderer nullRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, null, true,
                        PREBID_MOBILE_RENDERER_NAME, "1.0"
                )
        );
        registerPlugin(nullRenderer);

        MediationBannerView bannerView = new MediationBannerView(
                context, mockDisplayViewListener, adUnitConfiguration, mockBidResponse
        );

        verify(mockDisplayViewListener).onAdFailed(any());
        assertEquals(0, bannerView.getChildCount());
    }

    @Test
    public void destroy_clearsChildViewsAndDestroysInnerView() {
        DestroyableView mockBannerView = new DestroyableView(context);
        PrebidMobilePluginRenderer defaultRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, mockBannerView, true,
                        PREBID_MOBILE_RENDERER_NAME, "1.0"
                )
        );
        registerPlugin(defaultRenderer);

        MediationBannerView bannerView = new MediationBannerView(
                context, mockDisplayViewListener, adUnitConfiguration, mockBidResponse
        );
        assertEquals(1, bannerView.getChildCount());

        bannerView.destroy();

        assertEquals(0, bannerView.getChildCount());
        assertTrue(mockBannerView.destroyed);
    }

    @Test
    public void constructor_customRenderer_sendsWinNoticeBeforeCreatingView() {
        PrebidMobilePluginRenderer customRenderer = registerCustomRenderer(new View(context));

        try (MockedConstruction<WinNotifier> winNotifiers = mockConstruction(
                WinNotifier.class,
                (winNotifier, constructionContext) -> doAnswer(invocation -> {
                    ((WinNotifier.WinNotifierListener) invocation.getArgument(1)).onResult();
                    return null;
                }).when(winNotifier).notifyWin(any(), any())
        )) {
            MediationBannerView bannerView = new MediationBannerView(
                    context, mockDisplayViewListener, adUnitConfiguration, mockBidResponse
            );

            assertEquals(1, winNotifiers.constructed().size());
            verify(winNotifiers.constructed().get(0)).notifyWin(eq(mockBidResponse), any());
            assertEquals(1, bannerView.getChildCount());
        }
        verify(customRenderer).createBannerAdView(any(), any(), any(), any(), any());
    }

    @Test
    public void constructor_customRendererSendsWinNoticeItself_skipsWinNotice() {
        PrebidMobilePluginRenderer customRenderer = registerCustomRenderer(new View(context));
        doReturn(true).when(customRenderer).sendsWinNotice();

        try (MockedConstruction<WinNotifier> winNotifiers = mockConstruction(WinNotifier.class)) {
            MediationBannerView bannerView = new MediationBannerView(
                    context, mockDisplayViewListener, adUnitConfiguration, mockBidResponse
            );

            assertTrue(winNotifiers.constructed().isEmpty());
            assertEquals(1, bannerView.getChildCount());
        }
        verify(customRenderer).createBannerAdView(any(), any(), any(), any(), any());
    }

    // The adapters hand the container a fresh configuration, so the mediation ad unit's isMuted
    // and the server side ismuted reach the renderer only through the bid response's passthrough.
    @Test
    public void constructor_appliesBidResponseRenderingControls_toTheRenderedConfiguration() throws Exception {
        PrebidMobilePluginRenderer defaultRenderer = registerDefaultRenderer(new View(context));
        when(mockBidResponse.getMobileSdkPassThrough()).thenReturn(passThroughWithIsMuted(true));
        assertFalse("Sanity: the adapter's configuration starts unmuted", adUnitConfiguration.isMuted());

        new MediationBannerView(context, mockDisplayViewListener, adUnitConfiguration, mockBidResponse);

        AdUnitConfiguration rendered = captureRenderedConfiguration(defaultRenderer);
        assertTrue(rendered.isMuted());
    }

    @Test
    public void constructor_bidResponseWithoutRenderingControls_keepsTheConfiguration() {
        PrebidMobilePluginRenderer defaultRenderer = registerDefaultRenderer(new View(context));
        when(mockBidResponse.getMobileSdkPassThrough()).thenReturn(null);
        adUnitConfiguration.setIsMuted(true);

        new MediationBannerView(context, mockDisplayViewListener, adUnitConfiguration, mockBidResponse);

        AdUnitConfiguration rendered = captureRenderedConfiguration(defaultRenderer);
        assertTrue(rendered.isMuted());
    }

    // The adapters pass a fresh configuration. The container renders from a copy of the ad unit's
    // own configuration, which the response carries, so the publisher's settings reach the renderer.
    @Test
    public void constructor_rendersFromACopyOfTheAdUnitConfiguration() {
        PrebidMobilePluginRenderer defaultRenderer = registerDefaultRenderer(new View(context));
        adUnitConfiguration.setIsMuted(true);
        adUnitConfiguration.setIsSoundButtonVisible(false);
        AdUnitConfiguration adapterConfiguration = freshAdapterConfiguration();

        new MediationBannerView(context, mockDisplayViewListener, adapterConfiguration, mockBidResponse);

        AdUnitConfiguration rendered = captureRenderedConfiguration(defaultRenderer);
        assertNotSame("The ad unit's own configuration must not be handed to a renderer", adUnitConfiguration, rendered);
        assertNotSame(adapterConfiguration, rendered);
        assertEquals(adUnitConfiguration.getFingerprint(), rendered.getFingerprint());
        assertTrue(rendered.isMuted());
        assertFalse("An explicit setIsSoundButtonVisible(false) must reach the renderer", rendered.isSoundButtonVisible());
    }

    @Test
    public void constructor_adUnitShowsTheSoundButton_rendersWithIt() {
        PrebidMobilePluginRenderer defaultRenderer = registerDefaultRenderer(new View(context));
        adUnitConfiguration.setIsSoundButtonVisible(true);

        new MediationBannerView(context, mockDisplayViewListener, freshAdapterConfiguration(), mockBidResponse);

        assertTrue(captureRenderedConfiguration(defaultRenderer).isSoundButtonVisible());
    }

    @Test
    public void constructor_adUnitNeverSetTheSoundButton_keepsItUnsetForTheRenderer() {
        PrebidMobilePluginRenderer defaultRenderer = registerDefaultRenderer(new View(context));

        new MediationBannerView(context, mockDisplayViewListener, freshAdapterConfiguration(), mockBidResponse);

        AdUnitConfiguration rendered = captureRenderedConfiguration(defaultRenderer);
        assertTrue("Banner video keeps its volume control", rendered.isSoundButtonVisible(true));
        assertFalse(rendered.isSoundButtonVisible());
    }

    @Test
    public void constructor_responseWithoutAdUnitConfiguration_rendersACopyOfTheAdapterConfiguration() {
        PrebidMobilePluginRenderer defaultRenderer = registerDefaultRenderer(new View(context));
        when(mockBidResponse.getAdUnitConfiguration()).thenReturn(null);
        AdUnitConfiguration adapterConfiguration = freshAdapterConfiguration();
        adapterConfiguration.setIsMuted(true);

        new MediationBannerView(context, mockDisplayViewListener, adapterConfiguration, mockBidResponse);

        AdUnitConfiguration rendered = captureRenderedConfiguration(defaultRenderer);
        assertNotSame(adapterConfiguration, rendered);
        assertTrue(rendered.isMuted());
    }

    private static AdUnitConfiguration freshAdapterConfiguration() {
        AdUnitConfiguration adapterConfiguration = new AdUnitConfiguration();
        adapterConfiguration.setAdFormat(AdFormat.BANNER);
        return adapterConfiguration;
    }

    private static MobileSdkPassThrough passThroughWithIsMuted(boolean isMuted) throws Exception {
        return MobileSdkPassThrough.create(new JSONObject(
                "{\"prebid\":{\"passthrough\":[{\"type\":\"prebidmobilesdk\",\"adconfiguration\":{\"ismuted\":" + isMuted + "}}]}}"
        ));
    }

    private AdUnitConfiguration captureRenderedConfiguration(PrebidMobilePluginRenderer renderer) {
        ArgumentCaptor<AdUnitConfiguration> captor = ArgumentCaptor.forClass(AdUnitConfiguration.class);
        verify(renderer).createBannerAdView(any(), any(), any(), captor.capture(), eq(mockBidResponse));
        return captor.getValue();
    }

    private PrebidMobilePluginRenderer registerDefaultRenderer(View bannerView) {
        PrebidMobilePluginRenderer defaultRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, bannerView, true,
                        PREBID_MOBILE_RENDERER_NAME, "1.0"
                )
        );
        registerPlugin(defaultRenderer);
        return defaultRenderer;
    }

    private PrebidMobilePluginRenderer registerCustomRenderer(View bannerView) {
        PrebidMobilePluginRenderer customRenderer = spy(
                FakePrebidMobilePluginRenderer.getFakePrebidRenderer(
                        null, bannerView, true,
                        CUSTOM_RENDERER_NAME, CUSTOM_RENDERER_VERSION
                )
        );
        registerPlugin(customRenderer);
        when(mockBidResponse.getPreferredPluginRendererName()).thenReturn(CUSTOM_RENDERER_NAME);
        when(mockBidResponse.getPreferredPluginRendererVersion()).thenReturn(CUSTOM_RENDERER_VERSION);
        return customRenderer;
    }

    private void registerPlugin(PrebidMobilePluginRenderer renderer) {
        PrebidMobilePluginRegister.getInstance().registerPlugin(renderer);
        registeredRenderers.add(renderer);
    }
}
