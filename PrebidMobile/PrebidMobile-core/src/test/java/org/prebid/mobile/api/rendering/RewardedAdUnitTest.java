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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer;
import org.prebid.mobile.api.rendering.listeners.RewardedAdUnitListener;
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

        final AdUnitConfiguration adUnitConfig = rewardedAdUnit.adUnitConfig;
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
        changeInterstitialState(LOADING);
        rewardedAdUnit.loadAd();
        verifyNoInteractions(mockBidLoader);

        changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_PREBID);
        rewardedAdUnit.loadAd();
        verifyNoInteractions(mockBidLoader);

        changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM);
        rewardedAdUnit.loadAd();
        verifyNoInteractions(mockBidLoader);

        changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState.PREBID_LOADING);
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
        changeInterstitialState(READY_TO_DISPLAY_GAM);

        rewardedAdUnit.show();

        verify(mockRewardedEventHandler, times(1)).show();
    }

    @Test
    public void showWhenAuctionWinnerIsPrebid_ShowPrebid() {
        final InterstitialController mockInterstitialController = mock(InterstitialController.class);

        changeInterstitialState(READY_TO_DISPLAY_PREBID);

        WhiteBox.setInternalState(rewardedAdUnit, "interstitialController", mockInterstitialController);
        rewardedAdUnit.show();

        verify(mockInterstitialController, times(1)).show();
    }

    @Test
    public void isLoadedWhenAuctionIsNotReadyForDisplay_ReturnFalse() {
        changeInterstitialState(READY_FOR_LOAD);
        assertFalse(rewardedAdUnit.isLoaded());

        changeInterstitialState(LOADING);
        assertFalse(rewardedAdUnit.isLoaded());

        changeInterstitialState(PREBID_LOADING);
        assertFalse(rewardedAdUnit.isLoaded());
    }

    @Test
    public void isLoadedWhenAuctionIsReadyForDisplay_ReturnTrue() {
        changeInterstitialState(READY_TO_DISPLAY_PREBID);
        assertTrue(rewardedAdUnit.isLoaded());

        changeInterstitialState(READY_TO_DISPLAY_GAM);
        assertTrue(rewardedAdUnit.isLoaded());
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

        BidRequesterListener listener = getBidRequesterListener();
        listener.onFetchCompleted(mockBidResponse);

        verify(mockRewardedEventHandler, times(1)).requestAdWithBid(eq(mockBid));
        assertEquals(LOADING, rewardedAdUnit.getAdUnitState());
    }

    @Test
    public void onError_RequestAdWitNullBid() {
        BidRequesterListener listener = getBidRequesterListener();
        listener.onError(any());

        verify(mockRewardedEventHandler, times(1)).requestAdWithBid(eq(null));
    }
    //endregion ======================= BidRequestListener tests

    //region ================= EventListener tests
    @Test
    public void onPrebidSdkWinAndWinnerBidIsNull_AdStatusReadyForLoadNotifyErrorListener() {
        final RewardedVideoEventListener eventListener = getEventListener();

        eventListener.onPrebidSdkWin();

        verify(mockRewardedAdUnitListener, times(1)).onAdFailed(eq(rewardedAdUnit), any(AdException.class));
        assertEquals(READY_FOR_LOAD, rewardedAdUnit.getAdUnitState());
    }

    @Test
    public void onAdServerWin_AdStatusReadyToDisplayGAMNotifyAdLoaded() {
        final RewardedVideoEventListener eventListener = getEventListener();

        eventListener.onAdServerWin(any());

        assertEquals(BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM, rewardedAdUnit.getAdUnitState());
        verify(mockRewardedAdUnitListener, times(1)).onAdLoaded(rewardedAdUnit);
    }

    @Test
    public void onFailedAndNoWinnerBid_AdStatusReadyForLoadNotifyErrorListener() {
        final AdException exception = new AdException(AdException.INTERNAL_ERROR, "Test");
        final RewardedVideoEventListener eventListener = getEventListener();

        eventListener.onAdFailed(exception);

        verify(mockRewardedAdUnitListener, times(1)).onAdFailed(rewardedAdUnit, exception);
        assertEquals(READY_FOR_LOAD, rewardedAdUnit.getAdUnitState());
    }

    @Test
    public void onFailedAndWithWinnerBid_ExecuteInterstitialControllerLoadAd()
        throws IllegalAccessException {
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final InterstitialController mockInterstitialController = mock(InterstitialController.class);
        final Bid mockBid = mock(Bid.class);
        final RewardedVideoEventListener spyEventListener = spy(getEventListener());
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
        final RewardedVideoEventListener eventListener = getEventListener();
        eventListener.onAdClicked();

        verify(mockRewardedAdUnitListener, times(1)).onAdClicked(rewardedAdUnit);
    }

    @Test
    public void onAdClosed_NotifyAdClosedListener() {
        final RewardedVideoEventListener eventListener = getEventListener();
        eventListener.onAdClosed();

        verify(mockRewardedAdUnitListener, times(1)).onAdClosed(rewardedAdUnit);
    }
    //endregion ================= EventListener tests

    private BidRequesterListener getBidRequesterListener() {
        return (BidRequesterListener) WhiteBox.getInternalState(rewardedAdUnit, "bidRequesterListener");
    }

    private RewardedVideoEventListener getEventListener() {
        return (RewardedVideoEventListener) WhiteBox.getInternalState(rewardedAdUnit, "eventListener");
    }

    private void changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState adUnitState) {
        WhiteBox.setInternalState(rewardedAdUnit, "interstitialAdUnitState", adUnitState);
    }

}