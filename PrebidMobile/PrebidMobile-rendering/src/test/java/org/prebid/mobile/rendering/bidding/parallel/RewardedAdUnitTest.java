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
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.RewardedEventHandler;
import org.prebid.mobile.rendering.bidding.interfaces.StandaloneRewardedVideoEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.listeners.RewardedAdUnitListener;
import org.prebid.mobile.rendering.bidding.listeners.RewardedVideoEventListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.LOADING;
import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.OXB_LOADING;
import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_FOR_LOAD;
import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM;
import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_OXB;

@RunWith(RobolectricTestRunner.class)
public class RewardedAdUnitTest {
    private static final String CONFIGURATION_ID = "12345678";

    private RewardedAdUnit mRewardedAdUnit;
    private Context mContext;

    @Mock
    BidLoader mMockBidLoader;
    @Mock
    RewardedAdUnitListener mMockRewardedAdUnitListener;
    @Mock
    RewardedEventHandler mMockRewardedEventHandler;
    @Mock
    InterstitialController mMockInterstitialController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mContext = Robolectric.buildActivity(Activity.class).create().get();

        mRewardedAdUnit = new RewardedAdUnit(mContext, CONFIGURATION_ID, mMockRewardedEventHandler);

        mRewardedAdUnit.setRewardedAdUnitListener(mMockRewardedAdUnitListener);

        WhiteBox.setInternalState(mRewardedAdUnit, "mBidLoader", mMockBidLoader);
        WhiteBox.setInternalState(mRewardedAdUnit, "mInterstitialController", mMockInterstitialController);

        final AdConfiguration adUnitConfig = mRewardedAdUnit.mAdUnitConfig;
        assertEquals(AdPosition.FULLSCREEN.getValue(), adUnitConfig.getAdPositionValue());
    }

    @Test
    public void createRewardedAdUnitNoEventHandler_InstanceCreatedStandaloneEventHandlerProvidedBidLoaderIsNotNull() {
        RewardedAdUnit rewardedAdUnit = new RewardedAdUnit(mContext, CONFIGURATION_ID);

        Object eventHandler = WhiteBox.getInternalState(rewardedAdUnit, "mEventHandler");
        BidLoader bidLoader = ((BidLoader) WhiteBox.getInternalState(rewardedAdUnit, "mBidLoader"));

        assertNotNull(rewardedAdUnit);
        assertTrue(eventHandler instanceof StandaloneRewardedVideoEventHandler);
        assertNotNull(bidLoader);
    }

    @Test
    public void loadAdWithNullBidLoader_NoExceptionIsThrown() {
        WhiteBox.setInternalState(mRewardedAdUnit, "mBidLoader", null);

        mRewardedAdUnit.loadAd();
    }

    @Test
    public void loadAdWithInvalidInterstitialAdState_DoNothing() {
        changeInterstitialState(LOADING);
        mRewardedAdUnit.loadAd();
        verifyZeroInteractions(mMockBidLoader);

        changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_OXB);
        mRewardedAdUnit.loadAd();
        verifyZeroInteractions(mMockBidLoader);

        changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM);
        mRewardedAdUnit.loadAd();
        verifyZeroInteractions(mMockBidLoader);

        changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState.OXB_LOADING);
        mRewardedAdUnit.loadAd();
        verifyZeroInteractions(mMockBidLoader);
    }

    @Test
    public void loadAdWithValidBidLoaderAndAdUnitState_ExecuteBidLoad() {
        mRewardedAdUnit.loadAd();

        verify(mMockBidLoader, times(1)).load();
    }

    @Test
    public void showWhenAuctionWinnerIsNotReadyToDisplay_DoNothing() {
        mRewardedAdUnit.show();

        verify(mMockRewardedEventHandler, times(0)).show();
    }

    @Test
    public void showWhenAuctionWinnerIsGAM_ShowGam() {
        changeInterstitialState(READY_TO_DISPLAY_GAM);

        mRewardedAdUnit.show();

        verify(mMockRewardedEventHandler, times(1)).show();
    }

    @Test
    public void showWhenAuctionWinnerIsOXB_ShowOXB() {
        final InterstitialController mockInterstitialController = mock(InterstitialController.class);

        changeInterstitialState(READY_TO_DISPLAY_OXB);

        WhiteBox.setInternalState(mRewardedAdUnit, "mInterstitialController", mockInterstitialController);
        mRewardedAdUnit.show();

        verify(mockInterstitialController, times(1)).show();
    }

    @Test
    public void isLoadedWhenAuctionIsNotReadyForDisplay_ReturnFalse() {
        changeInterstitialState(READY_FOR_LOAD);
        assertFalse(mRewardedAdUnit.isLoaded());

        changeInterstitialState(LOADING);
        assertFalse(mRewardedAdUnit.isLoaded());

        changeInterstitialState(OXB_LOADING);
        assertFalse(mRewardedAdUnit.isLoaded());
    }

    @Test
    public void isLoadedWhenAuctionIsReadyForDisplay_ReturnTrue() {
        changeInterstitialState(READY_TO_DISPLAY_OXB);
        assertTrue(mRewardedAdUnit.isLoaded());

        changeInterstitialState(READY_TO_DISPLAY_GAM);
        assertTrue(mRewardedAdUnit.isLoaded());
    }

    @Test
    public void destroy_DestroyEventHandlerAndBidLoader() {
        mRewardedAdUnit.destroy();

        verify(mMockRewardedEventHandler).destroy();
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

        verify(mMockRewardedEventHandler, times(1)).requestAdWithBid(eq(mockBid));
        assertEquals(LOADING, mRewardedAdUnit.getAdUnitState());
    }

    @Test
    public void onError_RequestAdWitNullBid() {
        BidRequesterListener listener = getBidRequesterListener();
        listener.onError(any());

        verify(mMockRewardedEventHandler, times(1)).requestAdWithBid(eq(null));
    }
    //endregion ======================= BidRequestListener tests

    //region ================= EventListener tests
    @Test
    public void onOxbSdkWinAndWinnerBidIsNull_AdStatusReadyForLoadNotifyErrorListener() {
        final RewardedVideoEventListener eventListener = getEventListener();

        eventListener.onOXBSdkWin();

        verify(mMockRewardedAdUnitListener, times(1)).onAdFailed(eq(mRewardedAdUnit), any(AdException.class));
        assertEquals(READY_FOR_LOAD, mRewardedAdUnit.getAdUnitState());
    }

    @Test
    public void onAdServerWin_AdStatusReadyToDisplayGAMNotifyAdLoaded() {
        final RewardedVideoEventListener eventListener = getEventListener();

        eventListener.onAdServerWin(any());

        assertEquals(BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM, mRewardedAdUnit.getAdUnitState());
        verify(mMockRewardedAdUnitListener, times(1)).onAdLoaded(mRewardedAdUnit);
    }

    @Test
    public void onFailedAndNoWinnerBid_AdStatusReadyForLoadNotifyErrorListener() {
        final AdException exception = new AdException(AdException.INTERNAL_ERROR, "Test");
        final RewardedVideoEventListener eventListener = getEventListener();

        eventListener.onAdFailed(exception);

        verify(mMockRewardedAdUnitListener, times(1)).onAdFailed(mRewardedAdUnit, exception);
        assertEquals(READY_FOR_LOAD, mRewardedAdUnit.getAdUnitState());
    }

    @Test
    public void onFailedAndWithWinnerBid_ExecuteInterstitialControllerLoadAd()
    throws IllegalAccessException {
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final InterstitialController mockInterstitialController = mock(InterstitialController.class);
        final Bid mockBid = mock(Bid.class);
        final RewardedVideoEventListener spyEventListener = spy(getEventListener());
        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);

        WhiteBox.setInternalState(mRewardedAdUnit, "mBidResponse", mockBidResponse);
        WhiteBox.setInternalState(mRewardedAdUnit, "mInterstitialController", mockInterstitialController);

        spyEventListener.onAdFailed(new AdException(AdException.INTERNAL_ERROR, "Test"));

        verify(spyEventListener, times(1)).onOXBSdkWin();
        verify(mockInterstitialController, times(1)).loadAd(any(), any());
    }

    @Test
    public void onAdOpened_NotifyClickListener() {
        final RewardedVideoEventListener eventListener = getEventListener();
        eventListener.onAdClicked();

        verify(mMockRewardedAdUnitListener, times(1)).onAdClicked(mRewardedAdUnit);
    }

    @Test
    public void onAdClosed_NotifyAdClosedListener() {
        final RewardedVideoEventListener eventListener = getEventListener();
        eventListener.onAdClosed();

        verify(mMockRewardedAdUnitListener, times(1)).onAdClosed(mRewardedAdUnit);
    }
    //endregion ================= EventListener tests

    private BidRequesterListener getBidRequesterListener() {
        return (BidRequesterListener) WhiteBox.getInternalState(mRewardedAdUnit, "mBidRequesterListener");
    }

    private RewardedVideoEventListener getEventListener() {
        return (RewardedVideoEventListener) WhiteBox.getInternalState(mRewardedAdUnit, "mEventListener");
    }

    private void changeInterstitialState(BaseInterstitialAdUnit.InterstitialAdUnitState adUnitState) {
        WhiteBox.setInternalState(mRewardedAdUnit, "mInterstitialAdUnitState", adUnitState);
    }
}