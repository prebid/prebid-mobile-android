package org.prebid.mobile.rendering.bidding.display;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.rendering.PrebidDestroyable;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
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

    private void registerPlugin(PrebidMobilePluginRenderer renderer) {
        PrebidMobilePluginRegister.getInstance().registerPlugin(renderer);
        registeredRenderers.add(renderer);
    }
}
