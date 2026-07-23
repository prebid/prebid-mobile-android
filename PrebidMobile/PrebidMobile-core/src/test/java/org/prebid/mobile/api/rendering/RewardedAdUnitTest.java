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
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.listeners.RewardedAdUnitListener;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.RewardedEventHandler;
import org.prebid.mobile.rendering.bidding.interfaces.StandaloneRewardedVideoEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.listeners.RewardedVideoEventListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.test.utils.WhiteBox;
import org.prebid.mobile.testutils.FakePrebidMobilePluginRenderer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.prebid.mobile.api.rendering.BaseInterstitialAdUnit.InterstitialAdUnitState.*;
import static org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME;

@RunWith(RobolectricTestRunner.class)
public class RewardedAdUnitTest {

    private static final String CONFIGURATION_ID = "12345678";

    private RewardedAdUnit rewardedAdUnit;
    private Context context;

    @Mock
    BidLoader mockBidLoader;
    @Mock
    RewardedAdUnitListener mockRewardedAdUnitListener;
    @Mock
    RewardedEventHandler mockRewardedEventHandler;
    @Mock
    InterstitialController mockInterstitialController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        context = Robolectric.buildActivity(Activity.class).create().get();

        rewardedAdUnit = new RewardedAdUnit(context, CONFIGURATION_ID, mockRewardedEventHandler);

        rewardedAdUnit.setRewardedAdUnitListener(mockRewardedAdUnitListener);

        WhiteBox.setInternalState(rewardedAdUnit, "bidLoader", mockBidLoader);
        WhiteBox.setInternalState(rewardedAdUnit, "interstitialController", mockInterstitialController);

        final AdUnitConfiguration adUnitConfig = rewardedAdUnit.config;
        assertEquals(AdPosition.FULLSCREEN.getValue(), adUnitConfig.getAdPositionValue());
    }

    @Test
    public void createRewardedAdUnitNoEventHandler_InstanceCreatedStandaloneEventHandlerProvidedBidLoaderIsNotNull() {
        RewardedAdUnit rewardedAdUnit = new RewardedAdUnit(context, CONFIGURATION_ID);

        Object eventHandler = WhiteBox.getInternalState(rewardedAdUnit, "eventHandler");
        BidLoader bidLoader = ((BidLoader) WhiteBox.getInternalState(rewardedAdUnit, "bidLoader"));

        assertNotNull(rewardedAdUnit);
        assertTrue(eventHandler instanceof StandaloneRewardedVideoEventHandler);
        assertNotNull(bidLoader);
    }

    @Test
    public void loadAdWithNullBidLoader_NoExceptionIsThrown() {
        WhiteBox.setInternalState(rewardedAdUnit, "bidLoader", null);

        rewardedAdUnit.loadAd();
    }

    @Test
    public void loadAdWithInvalidInterstitialAdState_DoNothing() {
        RenderingTestUtils.changeInterstitialState(rewardedAdUnit, LOADING);
        rewardedAdUnit.loadAd();
        verifyNoInteractions(mockBidLoader);

        RenderingTestUtils.changeInterstitialState(rewardedAdUnit, BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_PREBID);
        rewardedAdUnit.loadAd();
        verifyNoInteractions(mockBidLoader);

        RenderingTestUtils.changeInterstitialState(rewardedAdUnit, BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM);
        rewardedAdUnit.loadAd();
        verifyNoInteractions(mockBidLoader);

        RenderingTestUtils.changeInterstitialState(rewardedAdUnit, BaseInterstitialAdUnit.InterstitialAdUnitState.PREBID_LOADING);
        rewardedAdUnit.loadAd();
        verifyNoInteractions(mockBidLoader);
    }

    @Test
    public void loadAdWithValidBidLoaderAndAdUnitState_ExecuteBidLoad() {
        rewardedAdUnit.loadAd();

        verify(mockBidLoader, times(1)).load();
    }

    @Test
    public void showWhenAuctionWinnerIsNotReadyToDisplay_DoNothing() {
        rewardedAdUnit.show();

        verify(mockRewardedEventHandler, times(0)).show();
    }

    @Test
    public void showWhenAuctionWinnerIsGAM_ShowGam() {
        RenderingTestUtils.changeInterstitialState(rewardedAdUnit, READY_TO_DISPLAY_GAM);

        rewardedAdUnit.show();

        verify(mockRewardedEventHandler, times(1)).show();
    }

    @Test
    public void showWhenAuctionWinnerIsPrebid_ShowPrebid() {
        final InterstitialController mockInterstitialController = mock(InterstitialController.class);

        RenderingTestUtils.changeInterstitialState(rewardedAdUnit, READY_TO_DISPLAY_PREBID);

        WhiteBox.setInternalState(rewardedAdUnit, "interstitialController", mockInterstitialController);
        rewardedAdUnit.show();

        verify(mockInterstitialController, times(1)).show();
    }

    @Test
    public void isLoadedWhenAuctionIsNotReadyForDisplay_ReturnFalse() {
        RenderingTestUtils.changeInterstitialState(rewardedAdUnit, READY_FOR_LOAD);
        assertFalse(rewardedAdUnit.isLoaded());

        RenderingTestUtils.changeInterstitialState(rewardedAdUnit, LOADING);
        assertFalse(rewardedAdUnit.isLoaded());

        RenderingTestUtils.changeInterstitialState(rewardedAdUnit, PREBID_LOADING);
        assertFalse(rewardedAdUnit.isLoaded());
    }

    @Test
    public void isLoadedWhenAuctionIsReadyForDisplay_ReturnTrue() {
        RenderingTestUtils.changeInterstitialState(rewardedAdUnit, READY_TO_DISPLAY_PREBID);
        assertTrue(rewardedAdUnit.isLoaded());

        RenderingTestUtils.changeInterstitialState(rewardedAdUnit, READY_TO_DISPLAY_GAM);
        assertTrue(rewardedAdUnit.isLoaded());
    }

    @Test
    public void whenReadyAdExpires_IsLoadedReturnsFalseAndListenerIsNotified() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(1);
        WhiteBox.setInternalState(rewardedAdUnit, "bidResponse", mockBidResponse);

        rewardedAdUnit.controllerListener.onInterstitialReadyForDisplay();

        assertTrue(rewardedAdUnit.isLoaded());

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(rewardedAdUnit.isLoaded());
        assertTrue(rewardedAdUnit.isExpired());
        assertEquals(READY_FOR_LOAD, rewardedAdUnit.getAdUnitState());
        verify(mockInterstitialController).destroy();
        verify(mockRewardedAdUnitListener).onAdExpired(rewardedAdUnit);
    }

    @Test
    public void showWhenReadyAdExpired_DoNothing() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(1);
        WhiteBox.setInternalState(rewardedAdUnit, "bidResponse", mockBidResponse);

        rewardedAdUnit.controllerListener.onInterstitialReadyForDisplay();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        rewardedAdUnit.show();

        verify(mockInterstitialController, never()).show();
        verify(mockRewardedEventHandler, never()).show();
    }

    @Test
    public void whenDisplayedAdExpirationTimerFires_DoNotNotifyExpired() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(1);
        WhiteBox.setInternalState(rewardedAdUnit, "bidResponse", mockBidResponse);

        rewardedAdUnit.controllerListener.onInterstitialReadyForDisplay();
        rewardedAdUnit.controllerListener.onInterstitialDisplayed();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(rewardedAdUnit.isExpired());
        verify(mockInterstitialController, never()).destroy();
        verify(mockRewardedAdUnitListener, never()).onAdExpired(rewardedAdUnit);
    }

    @Test
    public void whenAdServerWinAdExpires_IsLoadedReturnsFalseAndListenerIsNotified() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(1);
        WhiteBox.setInternalState(rewardedAdUnit, "bidResponse", mockBidResponse);

        RenderingTestUtils.getRewardedVideoEventListener(rewardedAdUnit).onAdServerWin(new Object());
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(rewardedAdUnit.isLoaded());
        assertTrue(rewardedAdUnit.isExpired());
        assertEquals(READY_FOR_LOAD, rewardedAdUnit.getAdUnitState());
        verify(mockRewardedAdUnitListener).onAdExpired(rewardedAdUnit);
    }

    @Test
    public void whenDestroyedBeforeExpiration_DoNotNotifyExpired() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(1);
        WhiteBox.setInternalState(rewardedAdUnit, "bidResponse", mockBidResponse);

        rewardedAdUnit.controllerListener.onInterstitialReadyForDisplay();
        rewardedAdUnit.destroy();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        // isExpired() is the meaningful assertion: cancelExpiration() must have fired, otherwise
        // expireAd() would set expired=true. The verify(never()) below is a secondary check;
        // destroy() nulls userListener so the callback is suppressed regardless of cancellation.
        assertFalse(rewardedAdUnit.isExpired());
        verify(mockRewardedAdUnitListener, never()).onAdExpired(rewardedAdUnit);
    }

    @Test
    public void whenLoadAdBeforeExpiration_DoNotNotifyExpiredFromPreviousTimer() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(1);
        WhiteBox.setInternalState(rewardedAdUnit, "bidResponse", mockBidResponse);

        rewardedAdUnit.controllerListener.onInterstitialReadyForDisplay();
        RenderingTestUtils.changeInterstitialState(rewardedAdUnit, READY_FOR_LOAD);

        rewardedAdUnit.loadAd();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(rewardedAdUnit.isExpired());
        verify(mockBidLoader).load();
        verify(mockRewardedAdUnitListener, never()).onAdExpired(rewardedAdUnit);
    }

    @Test
    public void whenReadyWithoutExpiration_DoNotNotifyExpired() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(null);
        WhiteBox.setInternalState(rewardedAdUnit, "bidResponse", mockBidResponse);

        rewardedAdUnit.controllerListener.onInterstitialReadyForDisplay();
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS);

        assertFalse(rewardedAdUnit.isExpired());
        assertTrue(rewardedAdUnit.isLoaded());
        verify(mockRewardedAdUnitListener, never()).onAdExpired(rewardedAdUnit);
    }

    @Test
    public void destroy_DestroyEventHandlerAndBidLoader() {
        rewardedAdUnit.destroy();

        verify(mockRewardedEventHandler).destroy();
        verify(mockBidLoader).destroy();
        verify(mockInterstitialController).destroy();
    }

    //region ======================= BidRequestListener tests
    @Test
    public void onFetchComplete_ChangeInterstitialStateToLoadingAndRequestAdWithBid() {
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final Bid mockBid = mock(Bid.class);

        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);

        BidRequesterListener listener = RenderingTestUtils.getBidRequesterListener(rewardedAdUnit);
        listener.onFetchCompleted(mockBidResponse);

        verify(mockRewardedEventHandler, times(1)).requestAdWithBid(eq(mockBid));
        assertEquals(LOADING, rewardedAdUnit.getAdUnitState());
    }

    @Test
    public void onError_RequestAdWitNullBid() {
        BidRequesterListener listener = RenderingTestUtils.getBidRequesterListener(rewardedAdUnit);
        listener.onError(any());

        verify(mockRewardedEventHandler, times(1)).requestAdWithBid(eq(null));
    }
    //endregion ======================= BidRequestListener tests

    //region ================= EventListener tests
    @Test
    public void onPrebidSdkWinAndWinnerBidIsNull_AdStatusReadyForLoadNotifyErrorListener() {
        final RewardedVideoEventListener eventListener = RenderingTestUtils.getRewardedVideoEventListener(rewardedAdUnit);

        eventListener.onPrebidSdkWin();

        verify(mockRewardedAdUnitListener, times(1)).onAdFailed(eq(rewardedAdUnit), any(AdException.class));
        assertEquals(READY_FOR_LOAD, rewardedAdUnit.getAdUnitState());
    }

    @Test
    public void onAdServerWin_AdStatusReadyToDisplayGAMNotifyAdLoaded() {
        final RewardedVideoEventListener eventListener = RenderingTestUtils.getRewardedVideoEventListener(rewardedAdUnit);

        eventListener.onAdServerWin(new Object());

        assertEquals(BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM, rewardedAdUnit.getAdUnitState());
        verify(mockRewardedAdUnitListener, times(1)).onAdLoaded(rewardedAdUnit);
    }

    @Test
    public void onFailedAndNoWinnerBid_AdStatusReadyForLoadNotifyErrorListener() {
        final AdException exception = new AdException(AdException.INTERNAL_ERROR, "GAM error");
        final RewardedVideoEventListener eventListener = RenderingTestUtils.getRewardedVideoEventListener(rewardedAdUnit);

        eventListener.onAdFailed(exception);

        verify(mockRewardedAdUnitListener, times(1)).onAdFailed(rewardedAdUnit, new AdException(AdException.NO_BIDS, "GAM status: \"SDK internal error: GAM error\". Prebid status: \"SDK internal error: Unknown exception\""));
        assertEquals(READY_FOR_LOAD, rewardedAdUnit.getAdUnitState());
    }

    @Test
    public void onFailedAndWithWinnerBid_ExecuteInterstitialControllerLoadAd()
        throws IllegalAccessException {
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final InterstitialController mockInterstitialController = mock(InterstitialController.class);
        final Bid mockBid = mock(Bid.class);
        final RewardedVideoEventListener spyEventListener = spy(RenderingTestUtils.getRewardedVideoEventListener(rewardedAdUnit));
        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);
        PrebidMobilePluginRenderer fakePrebidRenderer = FakePrebidMobilePluginRenderer.getFakePrebidRenderer(mockInterstitialController, null, true, PREBID_MOBILE_RENDERER_NAME, "1.0");
        PrebidMobile.registerPluginRenderer(fakePrebidRenderer);

        WhiteBox.setInternalState(rewardedAdUnit, "bidResponse", mockBidResponse);
        WhiteBox.setInternalState(rewardedAdUnit, "interstitialController", mockInterstitialController);

        spyEventListener.onAdFailed(new AdException(AdException.INTERNAL_ERROR, "Test"));

        verify(spyEventListener, times(1)).onPrebidSdkWin();
        verify(mockInterstitialController, times(1)).loadAd(any(), any());
    }

    @Test
    public void onAdOpened_NotifyClickListener() {
        final RewardedVideoEventListener eventListener = RenderingTestUtils.getRewardedVideoEventListener(rewardedAdUnit);
        eventListener.onAdClicked();

        verify(mockRewardedAdUnitListener, times(1)).onAdClicked(rewardedAdUnit);
    }

    @Test
    public void onAdClosed_NotifyAdClosedListener() {
        final RewardedVideoEventListener eventListener = RenderingTestUtils.getRewardedVideoEventListener(rewardedAdUnit);
        eventListener.onAdClosed();

        verify(mockRewardedAdUnitListener, times(1)).onAdClosed(rewardedAdUnit);
    }
    //endregion ================= EventListener tests

}
