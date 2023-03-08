package org.prebid.mobile.api.rendering;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.testutils.FakePrebidMobilePluginRenderer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class DisplayViewTest {

    private DisplayView displayView;
    private Context context;
    private BidResponse mockResponse;
    private PrebidMobilePluginRenderer fakePrebidMobilePluginRenderer;
    @Spy
    private AdUnitConfiguration adUnitConfiguration;
    @Mock
    private View mockBannerView;
    @Mock
    private DisplayViewListener mockDisplayViewListener;


    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(DisplayViewTest.this);

        context = Robolectric.buildActivity(Activity.class).create().get();

        adUnitConfiguration.setAdFormat(AdFormat.BANNER);

        fakePrebidMobilePluginRenderer = Mockito.spy(FakePrebidMobilePluginRenderer.getFakePrebidRenderer(null, mockBannerView, true));
        PrebidMobile.registerPluginRenderer(fakePrebidMobilePluginRenderer);

        mockResponse = mock(BidResponse.class);
        Bid mockBid = mock(Bid.class);
        when(mockBid.getAdm()).thenReturn("adm");
        when(mockResponse.getWinningBid()).thenReturn(mockBid);
        when(mockResponse.gePreferredPluginRendererName()).thenReturn(PREBID_MOBILE_RENDERER_NAME);

        displayView = new DisplayView(context, mockDisplayViewListener, adUnitConfiguration, mockResponse);
    }

    @Test
    public void onDisplayViewWinNotification_returnBannerAdView() {
        verify(adUnitConfiguration).modifyUsingBidResponse(mockResponse);
        verify(fakePrebidMobilePluginRenderer).createBannerAdView(context, mockDisplayViewListener, adUnitConfiguration, mockResponse);
        // bannerAdView added to view hierarchy
        assertEquals(1, displayView.getChildCount());
    }
}