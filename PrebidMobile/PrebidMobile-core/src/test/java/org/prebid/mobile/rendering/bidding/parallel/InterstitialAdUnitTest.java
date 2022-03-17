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

package org.prebid.mobile.rendering.bidding.parallel;

import android.app.Activity;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialEventHandler;
import org.prebid.mobile.rendering.bidding.interfaces.StandaloneInterstitialEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.listeners.InterstitialAdUnitListener;
import org.prebid.mobile.rendering.bidding.listeners.InterstitialEventListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.test.utils.WhiteBox;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.*;

@RunWith(RobolectricTestRunner.class)
public class InterstitialAdUnitTest {
    private static final String CONFIGURATION_ID = "12345678";
    private static final AdSize AD_SIZE = new AdSize(320, 480);

    private InterstitialAdUnit mInterstitialAdUnit;
    private Context mContext;

    @Mock
    BidLoader mMockBidLoader;
    @Mock
    InterstitialAdUnitListener mMockInterstitialAdUnitListener;
    @Mock
    InterstitialEventHandler mMockInterstitialEventHandler;
    @Mock
    InterstitialController mMockInterstitialController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mContext = Robolectric.buildActivity(Activity.class).create().get();

        mInterstitialAdUnit = new InterstitialAdUnit(mContext, CONFIGURATION_ID, AD_SIZE, mMockInterstitialEventHandler);

        mInterstitialAdUnit.setInterstitialAdUnitListener(mMockInterstitialAdUnitListener);
        WhiteBox.setInternalState(mInterstitialAdUnit, "mBidLoader", mMockBidLoader);
        WhiteBox.setInternalState(mInterstitialAdUnit, "mInterstitialController", mMockInterstitialController);

        final AdUnitConfiguration adUnitConfig = mInterstitialAdUnit.mAdUnitConfig;
        assertEquals(AdPosition.FULLSCREEN.getValue(), adUnitConfig.getAdPositionValue());
    }

    @Test
    public void createInterstitialAdUnitNoEventHandler_InstanceCreatedStandaloneEventHandlerProvidedBidLoaderIsNotNull() {
        InterstitialAdUnit interstitialAdUnit = new InterstitialAdUnit(mContext, CONFIGURATION_ID, AdUnitFormat.VIDEO);

        Object eventHandler = WhiteBox.getInternalState(interstitialAdUnit, "mEventHandler");
        BidLoader bidLoader = ((BidLoader) WhiteBox.getInternalState(interstitialAdUnit, "mBidLoader"));

        assertNotNull(interstitialAdUnit);
        assertTrue(eventHandler instanceof StandaloneInterstitialEventHandler);
        assertNotNull(bidLoader);
    }

    @Test
    public void loadAdWithNullBidLoader_NoExceptionIsThrown() {
        WhiteBox.setInternalState(mInterstitialAdUnit, "mBidLoader", null);

        mInterstitialAdUnit.loadAd();
    }

    @Test
    public void loadAdWithInvalidInterstitialAdState_DoNothing() {
        changeInterstitialState(LOADING);
        mInterstitialAdUnit.loadAd();
        verifyZeroInteractions(mMockBidLoader);

        changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_PREBID);
        mInterstitialAdUnit.loadAd();
        verifyZeroInteractions(mMockBidLoader);

        changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM);
        mInterstitialAdUnit.loadAd();
        verifyZeroInteractions(mMockBidLoader);

        changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState.PREBID_LOADING);
        mInterstitialAdUnit.loadAd();
        verifyZeroInteractions(mMockBidLoader);
    }

    @Test
    public void loadAdWithValidBidLoaderAndAdUnitState_ExecuteBidLoad() {
        mInterstitialAdUnit.loadAd();

        verify(mMockBidLoader, times(1)).load();
    }

    @Test
    public void showWhenAuctionWinnerIsNotReadyToDisplay_DoNothing() {
        mInterstitialAdUnit.show();

        verify(mMockInterstitialEventHandler, times(0)).show();
    }

    @Test
    public void showWhenAuctionWinnerIsGAM_ShowGam() {
        changeInterstitialState(READY_TO_DISPLAY_GAM);

        mInterstitialAdUnit.show();

        verify(mMockInterstitialEventHandler, times(1)).show();
    }

    @Test
    public void showWhenAuctionWinnerIsPrebid_ShowPrebid() {
        final InterstitialController mockInterstitialController = mock(InterstitialController.class);

        changeInterstitialState(READY_TO_DISPLAY_PREBID);

        WhiteBox.setInternalState(mInterstitialAdUnit, "mInterstitialController", mockInterstitialController);
        mInterstitialAdUnit.show();

        verify(mockInterstitialController, times(1)).show();
    }

    @Test
    public void isLoadedWhenAuctionIsNotReadyForDisplay_ReturnFalse() {
        changeInterstitialState(READY_FOR_LOAD);
        assertFalse(mInterstitialAdUnit.isLoaded());

        changeInterstitialState(LOADING);
        assertFalse(mInterstitialAdUnit.isLoaded());

        changeInterstitialState(PREBID_LOADING);
        assertFalse(mInterstitialAdUnit.isLoaded());
    }

    @Test
    public void isLoadedWhenAuctionIsReadyForDisplay_ReturnTrue() {
        changeInterstitialState(READY_TO_DISPLAY_PREBID);
        assertTrue(mInterstitialAdUnit.isLoaded());

        changeInterstitialState(READY_TO_DISPLAY_GAM);
        assertTrue(mInterstitialAdUnit.isLoaded());
    }

    @Test
    public void destroy_DestroyEventHandlerAndBidLoader() {
        mInterstitialAdUnit.destroy();

        verify(mMockInterstitialEventHandler).destroy();
        verify(mMockBidLoader).destroy();
        verify(mMockInterstitialController).destroy();
    }

    //region ======================= BidRequestListener tests
    @Test
    public void onFetchComplete_ChangeInterstitialStateToLoadingAndRequestAdWithBid() {
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final Bid mockBid = mock(Bid.class);

        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);

        BidRequesterListener listener = getBidRequesterListener();
        listener.onFetchCompleted(mockBidResponse);

        verify(mMockInterstitialEventHandler, times(1)).requestAdWithBid(eq(mockBid));
        assertEquals(LOADING, mInterstitialAdUnit.getAdUnitState());
    }

    @Test
    public void onError_RequestAdWitNullBid() {
        BidRequesterListener listener = getBidRequesterListener();
        listener.onError(any());

        verify(mMockInterstitialEventHandler, times(1)).requestAdWithBid(eq(null));
    }
    //endregion ======================= BidRequestListener tests

    //region ================= EventListener tests
    @Test
    public void onPrebidSdkWinAndWinnerBidIsNull_AdStatusReadyForLoadNotifyErrorListener() {
        final InterstitialEventListener eventListener = getEventListener();

        eventListener.onPrebidSdkWin();

        verify(mMockInterstitialAdUnitListener, times(1)).onAdFailed(eq(mInterstitialAdUnit), any(AdException.class));
        assertEquals(READY_FOR_LOAD, mInterstitialAdUnit.getAdUnitState());
    }

    @Test
    public void onAdServerWin_AdStatusReadyToDisplayGAMNotifyAdLoaded() {
        final InterstitialEventListener eventListener = getEventListener();

        eventListener.onAdServerWin();

        assertEquals(BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM, mInterstitialAdUnit.getAdUnitState());
        verify(mMockInterstitialAdUnitListener, times(1)).onAdLoaded(mInterstitialAdUnit);
    }

    @Test
    public void onFailedAndNoWinnerBid_AdStatusReadyForLoadNotifyErrorListener() {
        final AdException exception = new AdException(AdException.INTERNAL_ERROR, "Test");
        final InterstitialEventListener eventListener = getEventListener();

        eventListener.onAdFailed(exception);

        verify(mMockInterstitialAdUnitListener, times(1)).onAdFailed(mInterstitialAdUnit, exception);
        assertEquals(READY_FOR_LOAD, mInterstitialAdUnit.getAdUnitState());
    }

    @Test
    public void onFailedAndWithWinnerBid_ExecuteInterstitialControllerLoadAd() {
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final InterstitialController mockInterstitialController = mock(InterstitialController.class);
        final Bid mockBid = mock(Bid.class);
        final InterstitialEventListener spyEventListener = spy(getEventListener());
        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);

        WhiteBox.setInternalState(mInterstitialAdUnit, "mBidResponse", mockBidResponse);
        WhiteBox.setInternalState(mInterstitialAdUnit, "mInterstitialController", mockInterstitialController);

        spyEventListener.onAdFailed(new AdException(AdException.INTERNAL_ERROR, "Test"));

        verify(spyEventListener, times(1)).onPrebidSdkWin();
        verify(mockInterstitialController, times(1)).loadAd(any(), any());
    }

    @Test
    public void onAdClosed_NotifyAdClosedListener() {
        final InterstitialEventListener eventListener = getEventListener();
        eventListener.onAdClosed();

        verify(mMockInterstitialAdUnitListener, times(1)).onAdClosed(mInterstitialAdUnit);
    }
    //endregion ================= EventListener tests

    private BidRequesterListener getBidRequesterListener() {
        return (BidRequesterListener) WhiteBox.getInternalState(mInterstitialAdUnit, "mBidRequesterListener");
    }

    private InterstitialEventListener getEventListener() {
        return (InterstitialEventListener) WhiteBox.getInternalState(mInterstitialAdUnit, "mInterstitialEventListener");
    }

    private void changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState adUnitState) {
        WhiteBox.setInternalState(mInterstitialAdUnit, "mInterstitialAdUnitState", adUnitState);
    }
}