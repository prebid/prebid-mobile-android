/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.api.rendering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.prebid.mobile.api.rendering.BaseInterstitialAdUnit.InterstitialAdUnitState.LOADING;
import static org.prebid.mobile.api.rendering.BaseInterstitialAdUnit.InterstitialAdUnitState.PREBID_LOADING;
import static org.prebid.mobile.api.rendering.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_FOR_LOAD;
import static org.prebid.mobile.api.rendering.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM;
import static org.prebid.mobile.api.rendering.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_PREBID;
import static org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME;

import android.app.Activity;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.listeners.InterstitialAdUnitListener;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialEventHandler;
import org.prebid.mobile.rendering.bidding.interfaces.StandaloneInterstitialEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.listeners.InterstitialEventListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.test.utils.WhiteBox;
import org.prebid.mobile.testutils.FakePrebidMobilePluginRenderer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.EnumSet;

@RunWith(RobolectricTestRunner.class)
public class InterstitialAdUnitTest {

    private static final String CONFIGURATION_ID = "12345678";
    private static final AdSize AD_SIZE = new AdSize(320, 480);

    private InterstitialAdUnit interstitialAdUnit;
    private Context context;

    @Mock
    BidLoader mockBidLoader;
    @Mock
    InterstitialAdUnitListener mockInterstitialAdUnitListener;
    @Mock
    InterstitialEventHandler mockInterstitialEventHandler;
    @Mock
    InterstitialController mockInterstitialController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        context = Robolectric.buildActivity(Activity.class).create().get();

        interstitialAdUnit = new InterstitialAdUnit(context, CONFIGURATION_ID, mockInterstitialEventHandler);
        interstitialAdUnit.setMinSizePercentage(AD_SIZE);

        interstitialAdUnit.setInterstitialAdUnitListener(mockInterstitialAdUnitListener);
        WhiteBox.setInternalState(interstitialAdUnit, "bidLoader", mockBidLoader);
        WhiteBox.setInternalState(interstitialAdUnit, "interstitialController", mockInterstitialController);

        final AdUnitConfiguration adUnitConfig = interstitialAdUnit.adUnitConfig;
        assertEquals(AdPosition.FULLSCREEN.getValue(), adUnitConfig.getAdPositionValue());
    }

    @Test
    public void createInterstitialAdUnit_BothDefaultAdUnitFormats() {
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit(
                context,
                CONFIGURATION_ID
        );

        EnumSet<AdFormat> adFormats = interstitialAdUnit.adUnitConfig.getAdFormats();
        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL, AdFormat.VAST), adFormats);
    }

    @Test
    public void createInterstitialAdUnitOtherConstructor_BothDefaultAdUnitFormats() {
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit(
                context,
                CONFIGURATION_ID,
                mock(InterstitialEventHandler.class)
        );

        EnumSet<AdFormat> adFormats = interstitialAdUnit.adUnitConfig.getAdFormats();
        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL, AdFormat.VAST), adFormats);
    }

    @Test
    public void createInterstitialAdUnitNoEventHandler_InstanceCreatedStandaloneEventHandlerProvidedBidLoaderIsNotNull() {
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit(
            context,
            CONFIGURATION_ID,
            EnumSet.of(AdUnitFormat.VIDEO)
        );

        Object eventHandler = WhiteBox.getInternalState(interstitialAdUnit, "eventHandler");
        BidLoader bidLoader = ((BidLoader) WhiteBox.getInternalState(interstitialAdUnit, "bidLoader"));

        assertNotNull(interstitialAdUnit);
        assertTrue(eventHandler instanceof StandaloneInterstitialEventHandler);
        assertNotNull(bidLoader);
        assertEquals(EnumSet.of(AdFormat.VAST), interstitialAdUnit.adUnitConfig.getAdFormats());
    }

    @Test
    public void createInterstitialAdUnitWithBannerParameter_AdFormatMustBeInterstitial() {
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit(
                context,
                CONFIGURATION_ID,
                EnumSet.of(AdUnitFormat.BANNER)
        );

        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL), interstitialAdUnit.adUnitConfig.getAdFormats());
    }

    @Test
    public void loadAdWithNullBidLoader_NoExceptionIsThrown() {
        WhiteBox.setInternalState(interstitialAdUnit, "bidLoader", null);

        interstitialAdUnit.loadAd();
    }

    @Test
    public void loadAdWithInvalidInterstitialAdState_DoNothing() {
        changeInterstitialState(LOADING);
        interstitialAdUnit.loadAd();
        verifyNoInteractions(mockBidLoader);

        changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_PREBID);
        interstitialAdUnit.loadAd();
        verifyNoInteractions(mockBidLoader);

        changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM);
        interstitialAdUnit.loadAd();
        verifyNoInteractions(mockBidLoader);

        changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState.PREBID_LOADING);
        interstitialAdUnit.loadAd();
        verifyNoInteractions(mockBidLoader);
    }

    @Test
    public void loadAdWithValidBidLoaderAndAdUnitState_ExecuteBidLoad() {
        interstitialAdUnit.loadAd();

        verify(mockBidLoader, times(1)).load();
    }

    @Test
    public void showWhenAuctionWinnerIsNotReadyToDisplay_DoNothing() {
        interstitialAdUnit.show();

        verify(mockInterstitialEventHandler, times(0)).show();
    }

    @Test
    public void showWhenAuctionWinnerIsGAM_ShowGam() {
        changeInterstitialState(READY_TO_DISPLAY_GAM);

        interstitialAdUnit.show();

        verify(mockInterstitialEventHandler, times(1)).show();
    }

    @Test
    public void showWhenAuctionWinnerIsPrebid_ShowPrebid() {
        final InterstitialController mockInterstitialController = mock(InterstitialController.class);

        changeInterstitialState(READY_TO_DISPLAY_PREBID);

        WhiteBox.setInternalState(interstitialAdUnit, "interstitialController", mockInterstitialController);
        interstitialAdUnit.show();

        verify(mockInterstitialController, times(1)).show();
    }

    @Test
    public void isLoadedWhenAuctionIsNotReadyForDisplay_ReturnFalse() {
        changeInterstitialState(READY_FOR_LOAD);
        assertFalse(interstitialAdUnit.isLoaded());

        changeInterstitialState(LOADING);
        assertFalse(interstitialAdUnit.isLoaded());

        changeInterstitialState(PREBID_LOADING);
        assertFalse(interstitialAdUnit.isLoaded());
    }

    @Test
    public void isLoadedWhenAuctionIsReadyForDisplay_ReturnTrue() {
        changeInterstitialState(READY_TO_DISPLAY_PREBID);
        assertTrue(interstitialAdUnit.isLoaded());

        changeInterstitialState(READY_TO_DISPLAY_GAM);
        assertTrue(interstitialAdUnit.isLoaded());
    }

    @Test
    public void destroy_DestroyEventHandlerAndBidLoader() {
        interstitialAdUnit.destroy();

        verify(mockInterstitialEventHandler).destroy();
        verify(mockBidLoader).destroy();
        verify(mockInterstitialController).destroy();
    }

    //region ======================= BidRequestListener tests
    @Test
    public void onFetchComplete_ChangeInterstitialStateToLoadingAndRequestAdWithBid() {
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final Bid mockBid = mock(Bid.class);

        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);

        BidRequesterListener listener = getBidRequesterListener();
        listener.onFetchCompleted(mockBidResponse);

        verify(mockInterstitialEventHandler, times(1)).requestAdWithBid(eq(mockBid));
        assertEquals(LOADING, interstitialAdUnit.getAdUnitState());
    }

    @Test
    public void onError_RequestAdWitNullBid() {
        BidRequesterListener listener = getBidRequesterListener();
        listener.onError(any());

        verify(mockInterstitialEventHandler, times(1)).requestAdWithBid(eq(null));
    }
    //endregion ======================= BidRequestListener tests

    //region ================= EventListener tests
    @Test
    public void onPrebidSdkWinAndWinnerBidIsNull_AdStatusReadyForLoadNotifyErrorListener() {
        final InterstitialEventListener eventListener = getEventListener();

        eventListener.onPrebidSdkWin();

        verify(mockInterstitialAdUnitListener, times(1)).onAdFailed(eq(interstitialAdUnit), any(AdException.class));
        assertEquals(READY_FOR_LOAD, interstitialAdUnit.getAdUnitState());
    }

    @Test
    public void onAdServerWin_AdStatusReadyToDisplayGAMNotifyAdLoaded() {
        final InterstitialEventListener eventListener = getEventListener();

        eventListener.onAdServerWin();

        assertEquals(BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM, interstitialAdUnit.getAdUnitState());
        verify(mockInterstitialAdUnitListener, times(1)).onAdLoaded(interstitialAdUnit);
    }

    @Test
    public void onFailedAndNoWinnerBid_AdStatusReadyForLoadNotifyErrorListener() {
        final AdException exception = new AdException(AdException.INTERNAL_ERROR, "Test");
        final InterstitialEventListener eventListener = getEventListener();

        eventListener.onAdFailed(exception);

        verify(mockInterstitialAdUnitListener, times(1)).onAdFailed(interstitialAdUnit, exception);
        assertEquals(READY_FOR_LOAD, interstitialAdUnit.getAdUnitState());
    }

    @Test
    public void onFailedAndWithWinnerBid_ExecuteInterstitialControllerLoadAd() {
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final InterstitialController mockInterstitialController = mock(InterstitialController.class);
        final Bid mockBid = mock(Bid.class);
        final InterstitialEventListener spyEventListener = spy(getEventListener());
        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);
        PrebidMobilePluginRenderer fakePrebidRenderer = FakePrebidMobilePluginRenderer.getFakePrebidRenderer(mockInterstitialController, null, true, PREBID_MOBILE_RENDERER_NAME, "1.0");
        PrebidMobile.registerPluginRenderer(fakePrebidRenderer);

        WhiteBox.setInternalState(interstitialAdUnit, "bidResponse", mockBidResponse);
        WhiteBox.setInternalState(interstitialAdUnit, "interstitialController", mockInterstitialController);

        spyEventListener.onAdFailed(new AdException(AdException.INTERNAL_ERROR, "Test"));

        verify(spyEventListener, times(1)).onPrebidSdkWin();
        verify(mockInterstitialController, times(1)).loadAd(any(), any());
    }

    @Test
    public void onAdClosed_NotifyAdClosedListener() {
        final InterstitialEventListener eventListener = getEventListener();
        eventListener.onAdClosed();

        verify(mockInterstitialAdUnitListener, times(1)).onAdClosed(interstitialAdUnit);
    }
    //endregion ================= EventListener tests

    private BidRequesterListener getBidRequesterListener() {
        return (BidRequesterListener) WhiteBox.getInternalState(interstitialAdUnit, "bidRequesterListener");
    }

    private InterstitialEventListener getEventListener() {
        return (InterstitialEventListener) WhiteBox.getInternalState(interstitialAdUnit, "interstitialEventListener");
    }

    private void changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState adUnitState) {
        WhiteBox.setInternalState(interstitialAdUnit, "interstitialAdUnitState", adUnitState);
    }

}