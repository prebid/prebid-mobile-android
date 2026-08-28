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

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
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
import org.robolectric.Shadows;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.prebid.mobile.api.rendering.BaseInterstitialAdUnit.InterstitialAdUnitState.*;
import static org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME;

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

        final AdUnitConfiguration adUnitConfig = interstitialAdUnit.config;
        assertEquals(AdPosition.FULLSCREEN.getValue(), adUnitConfig.getAdPositionValue());
    }

    @Test
    public void createInterstitialAdUnit_BothDefaultAdUnitFormats() {
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit(
                context,
                CONFIGURATION_ID
        );

        EnumSet<AdFormat> adFormats = interstitialAdUnit.config.getAdFormats();
        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL, AdFormat.VAST), adFormats);
    }

    @Test
    public void createInterstitialAdUnitOtherConstructor_BothDefaultAdUnitFormats() {
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit(
                context,
                CONFIGURATION_ID,
                mock(InterstitialEventHandler.class)
        );

        EnumSet<AdFormat> adFormats = interstitialAdUnit.config.getAdFormats();
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
        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL, AdFormat.VAST), interstitialAdUnit.config.getAdFormats());
    }

    @Test
    public void createInterstitialAdUnitWithBannerParameter_AdFormatMustBeInterstitial() {
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit(
                context,
                CONFIGURATION_ID,
                EnumSet.of(AdUnitFormat.BANNER)
        );

        assertEquals(EnumSet.of(AdFormat.INTERSTITIAL), interstitialAdUnit.config.getAdFormats());
    }

    @Test
    public void loadAdWithNullBidLoader_NoExceptionIsThrown() {
        WhiteBox.setInternalState(interstitialAdUnit, "bidLoader", null);

        interstitialAdUnit.loadAd();
    }

    @Test
    public void loadAdWithInvalidInterstitialAdState_DoNothing() {
        RenderingTestUtils.changeInterstitialState(interstitialAdUnit, LOADING);
        interstitialAdUnit.loadAd();
        verifyNoInteractions(mockBidLoader);

        RenderingTestUtils.changeInterstitialState(interstitialAdUnit, BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_PREBID);
        interstitialAdUnit.loadAd();
        verifyNoInteractions(mockBidLoader);

        RenderingTestUtils.changeInterstitialState(interstitialAdUnit, BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM);
        interstitialAdUnit.loadAd();
        verifyNoInteractions(mockBidLoader);

        RenderingTestUtils.changeInterstitialState(interstitialAdUnit, BaseInterstitialAdUnit.InterstitialAdUnitState.PREBID_LOADING);
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
        RenderingTestUtils.changeInterstitialState(interstitialAdUnit, READY_TO_DISPLAY_GAM);

        interstitialAdUnit.show();

        verify(mockInterstitialEventHandler, times(1)).show();
    }

    @Test
    public void showWhenAuctionWinnerIsPrebid_ShowPrebid() {
        final InterstitialController mockInterstitialController = mock(InterstitialController.class);

        RenderingTestUtils.changeInterstitialState(interstitialAdUnit, READY_TO_DISPLAY_PREBID);

        WhiteBox.setInternalState(interstitialAdUnit, "interstitialController", mockInterstitialController);
        interstitialAdUnit.show();

        verify(mockInterstitialController, times(1)).show();
    }

    @Test
    public void isLoadedWhenAuctionIsNotReadyForDisplay_ReturnFalse() {
        RenderingTestUtils.changeInterstitialState(interstitialAdUnit, READY_FOR_LOAD);
        assertFalse(interstitialAdUnit.isLoaded());

        RenderingTestUtils.changeInterstitialState(interstitialAdUnit, LOADING);
        assertFalse(interstitialAdUnit.isLoaded());

        RenderingTestUtils.changeInterstitialState(interstitialAdUnit, PREBID_LOADING);
        assertFalse(interstitialAdUnit.isLoaded());
    }

    @Test
    public void isLoadedWhenAuctionIsReadyForDisplay_ReturnTrue() {
        RenderingTestUtils.changeInterstitialState(interstitialAdUnit, READY_TO_DISPLAY_PREBID);
        assertTrue(interstitialAdUnit.isLoaded());

        RenderingTestUtils.changeInterstitialState(interstitialAdUnit, READY_TO_DISPLAY_GAM);
        assertTrue(interstitialAdUnit.isLoaded());
    }

    @Test
    public void whenReadyAdExpires_IsLoadedReturnsFalseAndListenerIsNotified() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(1);
        WhiteBox.setInternalState(interstitialAdUnit, "bidResponse", mockBidResponse);

        interstitialAdUnit.controllerListener.onInterstitialReadyForDisplay();

        assertTrue(interstitialAdUnit.isLoaded());

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(interstitialAdUnit.isLoaded());
        assertTrue(interstitialAdUnit.isExpired());
        assertEquals(READY_FOR_LOAD, interstitialAdUnit.getAdUnitState());
        verify(mockInterstitialController).destroy();
        verify(mockInterstitialAdUnitListener).onAdExpired(interstitialAdUnit);
    }

    @Test
    public void showWhenReadyAdExpired_DoNothing() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(1);
        WhiteBox.setInternalState(interstitialAdUnit, "bidResponse", mockBidResponse);

        interstitialAdUnit.controllerListener.onInterstitialReadyForDisplay();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        interstitialAdUnit.show();

        verify(mockInterstitialController, never()).show();
        verify(mockInterstitialEventHandler, never()).show();
    }

    @Test
    public void whenDisplayedAdExpirationTimerFires_DoNotNotifyExpired() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(1);
        WhiteBox.setInternalState(interstitialAdUnit, "bidResponse", mockBidResponse);

        interstitialAdUnit.controllerListener.onInterstitialReadyForDisplay();
        interstitialAdUnit.controllerListener.onInterstitialDisplayed();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(interstitialAdUnit.isExpired());
        verify(mockInterstitialController, never()).destroy();
        verify(mockInterstitialAdUnitListener, never()).onAdExpired(interstitialAdUnit);
    }

    @Test
    public void whenAdServerWinAdExpires_IsLoadedReturnsFalseAndListenerIsNotified() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(1);
        WhiteBox.setInternalState(interstitialAdUnit, "bidResponse", mockBidResponse);

        RenderingTestUtils.getInterstitialEventListener(interstitialAdUnit).onAdServerWin();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(interstitialAdUnit.isLoaded());
        assertTrue(interstitialAdUnit.isExpired());
        assertEquals(READY_FOR_LOAD, interstitialAdUnit.getAdUnitState());
        verify(mockInterstitialAdUnitListener).onAdExpired(interstitialAdUnit);
    }

    @Test
    public void whenDestroyedBeforeExpiration_DoNotNotifyExpired() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(1);
        WhiteBox.setInternalState(interstitialAdUnit, "bidResponse", mockBidResponse);

        interstitialAdUnit.controllerListener.onInterstitialReadyForDisplay();
        interstitialAdUnit.destroy();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(interstitialAdUnit.isExpired());
        verify(mockInterstitialAdUnitListener, never()).onAdExpired(interstitialAdUnit);
    }

    @Test
    public void whenLoadAdBeforeExpiration_DoNotNotifyExpiredFromPreviousTimer() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(1);
        WhiteBox.setInternalState(interstitialAdUnit, "bidResponse", mockBidResponse);

        interstitialAdUnit.controllerListener.onInterstitialReadyForDisplay();
        RenderingTestUtils.changeInterstitialState(interstitialAdUnit, READY_FOR_LOAD);

        interstitialAdUnit.loadAd();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(interstitialAdUnit.isExpired());
        verify(mockBidLoader).load();
        verify(mockInterstitialAdUnitListener, never()).onAdExpired(interstitialAdUnit);
    }

    @Test
    public void whenReadyWithoutExpiration_DoNotNotifyExpired() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(null);
        WhiteBox.setInternalState(interstitialAdUnit, "bidResponse", mockBidResponse);

        interstitialAdUnit.controllerListener.onInterstitialReadyForDisplay();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(interstitialAdUnit.isExpired());
        assertTrue(interstitialAdUnit.isLoaded());
        verify(mockInterstitialAdUnitListener, never()).onAdExpired(interstitialAdUnit);
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

        BidRequesterListener listener = RenderingTestUtils.getBidRequesterListener(interstitialAdUnit);
        listener.onFetchCompleted(mockBidResponse);

        verify(mockInterstitialEventHandler, times(1)).requestAdWithBid(eq(mockBid));
        assertEquals(LOADING, interstitialAdUnit.getAdUnitState());
    }

    @Test
    public void onError_RequestAdWitNullBid() {
        BidRequesterListener listener = RenderingTestUtils.getBidRequesterListener(interstitialAdUnit);
        listener.onError(any());

        verify(mockInterstitialEventHandler, times(1)).requestAdWithBid(eq(null));
    }
    //endregion ======================= BidRequestListener tests

    //region ================= EventListener tests
    @Test
    public void onPrebidSdkWinAndWinnerBidIsNull_AdStatusReadyForLoadNotifyErrorListener() {
        final InterstitialEventListener eventListener = RenderingTestUtils.getInterstitialEventListener(interstitialAdUnit);

        eventListener.onPrebidSdkWin();

        verify(mockInterstitialAdUnitListener, times(1)).onAdFailed(eq(interstitialAdUnit), any(AdException.class));
        assertEquals(READY_FOR_LOAD, interstitialAdUnit.getAdUnitState());
    }

    @Test
    public void onAdServerWin_AdStatusReadyToDisplayGAMNotifyAdLoaded() {
        final InterstitialEventListener eventListener = RenderingTestUtils.getInterstitialEventListener(interstitialAdUnit);

        eventListener.onAdServerWin();

        assertEquals(BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM, interstitialAdUnit.getAdUnitState());
        verify(mockInterstitialAdUnitListener, times(1)).onAdLoaded(interstitialAdUnit);
    }

    @Test
    public void onFailedAndNoWinnerBid_AdStatusReadyForLoadNotifyErrorListener() {
        final AdException exception = new AdException(AdException.INTERNAL_ERROR, "GAM error");
        final InterstitialEventListener eventListener = RenderingTestUtils.getInterstitialEventListener(interstitialAdUnit);

        eventListener.onAdFailed(exception);

        verify(mockInterstitialAdUnitListener, times(1)).onAdFailed(interstitialAdUnit, new AdException(AdException.NO_BIDS, "GAM status: \"SDK internal error: GAM error\". Prebid status: \"SDK internal error: Unknown exception\""));
        assertEquals(READY_FOR_LOAD, interstitialAdUnit.getAdUnitState());
    }

    @Test
    public void onFailedAndWithWinnerBid_ExecuteInterstitialControllerLoadAd() {
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final InterstitialController mockInterstitialController = mock(InterstitialController.class);
        final Bid mockBid = mock(Bid.class);
        final InterstitialEventListener spyEventListener = spy(RenderingTestUtils.getInterstitialEventListener(interstitialAdUnit));
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
        final InterstitialEventListener eventListener = RenderingTestUtils.getInterstitialEventListener(interstitialAdUnit);
        eventListener.onAdClosed();

        verify(mockInterstitialAdUnitListener, times(1)).onAdClosed(interstitialAdUnit);
    }
    //endregion ================= EventListener tests

}
