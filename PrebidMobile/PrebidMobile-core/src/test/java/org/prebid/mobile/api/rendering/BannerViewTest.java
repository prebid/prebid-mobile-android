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
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.BannerAdPosition;
import org.prebid.mobile.api.data.VideoPlacementType;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.listeners.BannerVideoListener;
import org.prebid.mobile.api.rendering.listeners.BannerViewListener;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.BannerEventHandler;
import org.prebid.mobile.rendering.bidding.interfaces.StandaloneBannerEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.BannerEventListener;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayVideoListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.utils.broadcast.ScreenStateReceiver;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BannerViewTest {

    private static final String AD_UNIT_ID = "12345678";
    private static final AdSize AD_SIZE = new AdSize(320, 50);

    private BannerView bannerView;

    private Context mockContext;
    @Mock
    private BidLoader mockBidLoader;
    @Mock
    private BannerEventHandler mockEventHandler;
    @Mock
    private BannerViewListener mockBannerListener;
    @Mock
    private BannerVideoListener mockBannerVideoListener;
    @Mock
    private DisplayView mockDisplayView;
    @Mock
    private ScreenStateReceiver mockScreenStateReceiver;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockContext = Robolectric.buildActivity(Activity.class).create().get();

        when(mockEventHandler.getAdSizeArray()).thenReturn(new AdSize[]{AD_SIZE});
        bannerView = new BannerView(mockContext, AD_UNIT_ID, mockEventHandler);
        WhiteBox.field(BannerView.class, "bidLoader").set(bannerView, mockBidLoader);
        WhiteBox.field(BannerView.class, "displayView").set(bannerView, mockDisplayView);
        WhiteBox.field(BannerView.class, "displayView").set(bannerView, mockDisplayView);
        WhiteBox.field(BannerView.class, "screenStateReceiver").set(bannerView, mockScreenStateReceiver);
        bannerView.setBannerListener(mockBannerListener);
        bannerView.setBannerVideoListener(mockBannerVideoListener);

        assertEquals(BannerAdPosition.UNDEFINED.getValue(), bannerView.getAdPosition().getValue());
    }

    @Test
    public void createPrebidBannerViewWithAttributes_InstanceCreated() {
        AttributeSet attributes = Robolectric.buildAttributeSet().build();
        BannerView bannerView = new BannerView(mockContext, attributes);

        assertNotNull(bannerView);
    }

    @Test
    public void createPrebidBannerViewNoEventHandler_InstanceCreatedAndBidLoaderIsNotNullAndStandaloneEventHandlerProvided()
        throws IllegalAccessException {
        BannerView bannerView = new BannerView(mockContext, AD_UNIT_ID, AD_SIZE);

        Object eventHandler = WhiteBox.field(BannerView.class, "eventHandler").get(bannerView);
        BidLoader bidLoader = ((BidLoader) WhiteBox.field(BannerView.class, "bidLoader").get(bannerView));

        assertNotNull(bannerView);
        assertTrue(eventHandler instanceof StandaloneBannerEventHandler);
        assertNotNull(bidLoader);
    }

    @Test
    public void loadAdWithNullBidLoader_NoExceptionIsThrown() throws IllegalAccessException {
        WhiteBox.field(BannerView.class, "bidLoader").set(bannerView, null);

        bannerView.loadAd();
    }

    @Test
    public void loadAdWithPrimaryAdServerRequestInProgress_DoNothing() {
        changePrimaryAdServerRequestStatus(true);
        bannerView.loadAd();

        verifyNoInteractions(mockBidLoader);
    }

    @Test
    public void loadAd_bidResponseIsInitialized() {
        bannerView.loadAd();

        BidResponse response = bannerView.getBidResponse();
        System.out.println(response);
    }

    @Test
    public void loadAdWithValidBidLoaderAndPrimaryAdRequestNotInProgress_ExecuteBidLoad() {
        bannerView.loadAd();

        verify(mockBidLoader, times(1)).load();
    }

    @Test
    public void destroy_DestroyEventHandlerAndBidLoader() {
        bannerView.destroy();

        verify(mockEventHandler).destroy();
        verify(mockBidLoader).destroy();
        verify(mockScreenStateReceiver).unregister();
    }

    @Test
    public void setAutoRefreshDelayInSec_EqualsGetAutoRefreshDelayInMs() {
        final int interval = 30;
        bannerView.setAutoRefreshDelay(interval);

        assertEquals(interval * 1000, bannerView.getAutoRefreshDelayInMs());
    }

    @Test
    public void setRefreshIntervalInSecSmallerThanZero_ExpectDefaultRefresh() {
        final int interval = -1;
        bannerView.setAutoRefreshDelay(interval);

        assertEquals(0, bannerView.getAutoRefreshDelayInMs());
    }

    @Test
    public void addAdditionalSizes_GetterAndAdUnitConfigContainSetValueAndInitialSize() {
        final AdSize adSize = new AdSize(10, 20);
        final Set<AdSize> expectedSet = new HashSet<>();
        expectedSet.add(AD_SIZE); // from eventHandler
        expectedSet.add(adSize);

        bannerView.addAdditionalSizes(adSize);

        assertEquals(expectedSet, bannerView.getAdditionalSizes());
    }

    @Test
    public void setVideoPlacementType_AdUnitIdentifierTypeIsVASTAndVideoPlacementIsUpdated()
        throws IllegalAccessException {
        final VideoPlacementType videoPlacement = VideoPlacementType.IN_BANNER;
        AdUnitConfiguration mockAdConfiguration = mock(AdUnitConfiguration.class);
        WhiteBox.field(BannerView.class, "adUnitConfig").set(bannerView, mockAdConfiguration);

        bannerView.setVideoPlacementType(videoPlacement);

        verify(mockAdConfiguration, times(1)).setPlacementType(eq(VideoPlacementType.mapToPlacementType(videoPlacement)));
        verify(mockAdConfiguration, times(1)).setAdFormat(eq(AdFormat.VAST));
    }

    @Test
    public void setAdVideoPlacement_EqualsGetVideoPlacement() {
        bannerView.setVideoPlacementType(null);
        assertNull(bannerView.getVideoPlacementType());

        bannerView.setVideoPlacementType(VideoPlacementType.IN_ARTICLE);
        assertEquals(VideoPlacementType.IN_ARTICLE, bannerView.getVideoPlacementType());

        bannerView.setVideoPlacementType(VideoPlacementType.IN_BANNER);
        assertEquals(VideoPlacementType.IN_BANNER, bannerView.getVideoPlacementType());

        bannerView.setVideoPlacementType(VideoPlacementType.IN_FEED);
        assertEquals(VideoPlacementType.IN_FEED, bannerView.getVideoPlacementType());
    }

    //region ======================= BidRequestListener tests
    @Test
    public void onFetchComplete_AssignWinningBidMarkPrimaryServerRequestInProgressRequestAdWithBid() {
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final Bid mockBid = mock(Bid.class);

        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);

        BidRequesterListener listener = getBidRequesterListener();
        listener.onFetchCompleted(mockBidResponse);

        Bid winningBid = bannerView.getWinnerBid();
        BidResponse actualResponse = bannerView.getBidResponse();

        assertEquals(winningBid, mockBid);
        assertEquals(mockBidResponse, actualResponse);
        verify(mockEventHandler, times(1)).requestAdWithBid(eq(mockBid));
        assertTrue(bannerView.isPrimaryAdServerRequestInProgress());
    }

    @Test
    public void onError_NullifyWinnerBidAndRequestAdWitNullBid() {
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final Bid mockBid = mock(Bid.class);

        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);
        bannerView.setBidResponse(mockBidResponse);

        BidRequesterListener listener = getBidRequesterListener();
        listener.onError(any());

        assertNull(bannerView.getWinnerBid());
        assertNull(bannerView.getBidResponse());
        verify(mockEventHandler, times(1)).requestAdWithBid(eq(null));
    }
    //endregion ======================= BidRequestListener tests

    //region ================= BannerEventListener tests
    @Test
    public void onPrebidSdkWinAndWinnerBidIsNull_AdRequestStatusIsFinishedNotifyErrorListener() {
        changePrimaryAdServerRequestStatus(true);
        final BannerEventListener bannerEventListener = getBannerEventListener();
        bannerView.setBidResponse(null);

        bannerEventListener.onPrebidSdkWin();

        verify(mockBannerListener, times(1)).onAdFailed(eq(bannerView), any(AdException.class));
        assertFalse(bannerView.isPrimaryAdServerRequestInProgress());
    }

    @Test
    public void onPrebidSdkWin_AdRequestStatusIsFinishedDisplayAdView() {
        changePrimaryAdServerRequestStatus(true);
        final BannerEventListener bannerEventListener = getBannerEventListener();
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final Bid mockBid = mock(Bid.class);

        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);
        when(mockBidResponse.getWinningBidWidthHeightPairDips(any())).thenReturn(Pair.create(0, 0));

        bannerView.setBidResponse(mockBidResponse);

        bannerEventListener.onPrebidSdkWin();

        assertFalse(bannerView.isPrimaryAdServerRequestInProgress());
    }

    @Test
    public void onAdServerWin_AdRequestStatusIsFinishedDisplayAdViewAndNotifyAdReceivedListener() {
        changePrimaryAdServerRequestStatus(true);

        final View mockView = mock(View.class);
        final BannerEventListener bannerEventListener = getBannerEventListener();

        bannerEventListener.onAdServerWin(mockView);

        assertFalse(bannerView.isPrimaryAdServerRequestInProgress());
        verify(mockBannerListener, times(1)).onAdLoaded(bannerView);
        // verify(spyBannerView).displayAdServerView(mockView);
    }

    @Test
    public void onFailedAndNoWinnerBid_AdRequestStatusIsFinishedNotifyErrorListener() {
        changePrimaryAdServerRequestStatus(true);
        bannerView.setBidResponse(null);

        final AdException exception = new AdException(AdException.INTERNAL_ERROR, "Test");
        final BannerEventListener bannerEventListener = getBannerEventListener();

        bannerEventListener.onAdFailed(exception);

        verify(mockBannerListener, times(1)).onAdFailed(bannerView, exception);
    }

    @Test
    public void onFailedAndWithWinnerBid_AdRequestStatusIsFinishedNotifyPrebidSdkWin() {
        changePrimaryAdServerRequestStatus(true);

        final BidResponse mockBidResponse = mock(BidResponse.class);
        final Bid mockBid = mock(Bid.class);
        final BannerEventListener spyEventListener = spy(getBannerEventListener());
        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);
        when(mockBidResponse.getWinningBidWidthHeightPairDips(any())).thenReturn(Pair.create(0, 0));

        bannerView.setBidResponse(mockBidResponse);
        spyEventListener.onAdFailed(new AdException(AdException.INTERNAL_ERROR, "Test"));

        assertFalse(bannerView.isPrimaryAdServerRequestInProgress());
        verify(spyEventListener, times(1)).onPrebidSdkWin();
    }

    @Test
    public void onAdOpened_NotifyAdClickedListener() {
        final BannerEventListener bannerEventListener = getBannerEventListener();
        bannerEventListener.onAdClicked();

        verify(mockBannerListener, times(1)).onAdClicked(bannerView);
    }

    @Test
    public void onAdClosed_NotifyAdClosedListener() {
        final BannerEventListener bannerEventListener = getBannerEventListener();
        bannerEventListener.onAdClosed();

        verify(mockBannerListener, times(1)).onAdClosed(bannerView);
    }
    //endregion ================= BannerEventListener test

    private void changePrimaryAdServerRequestStatus(boolean isLoading) {
        try {
            WhiteBox.field(BannerView.class, "isPrimaryAdServerRequestInProgress").set(bannerView, isLoading);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void whenLoadAd_CallBidLoaderLoad() {
        bannerView.loadAd();
        verify(mockBidLoader).load();
    }

    @Test
    public void whenDisplayViewOnAdLoaded_CallBannerListenerOnAdLoaded()
        throws IllegalAccessException {
        getDisplayViewListener().onAdLoaded();
        verify(mockBannerListener).onAdLoaded(bannerView);
    }

    @Test
    public void whenDisplayViewOnAdDisplayed_CallBannerListenerOnAdDisplayedAndTrackImpression()
        throws IllegalAccessException {
        getDisplayViewListener().onAdDisplayed();
        verify(mockBannerListener).onAdDisplayed(bannerView);
        verify(mockEventHandler).trackImpression();
    }

    @Test
    public void whenDisplayViewOnAdFailed_CallBannerListenerOnAdFailed()
        throws IllegalAccessException {
        getDisplayViewListener().onAdFailed(new AdException(AdException.INTERNAL_ERROR, ""));
        verify(mockBannerListener).onAdFailed(eq(bannerView), any(AdException.class));
    }

    @Test
    public void whenDisplayViewOnAdOpened_CallBannerListenerOnAdClicked()
        throws IllegalAccessException {
        getDisplayViewListener().onAdClicked();
        verify(mockBannerListener).onAdClicked(bannerView);
    }

    @Test
    public void whenDisplayViewOnAdClosed_CallBannerListenerOnAdClosed()
        throws IllegalAccessException {
        getDisplayViewListener().onAdClosed();
        verify(mockBannerListener).onAdClosed(bannerView);
    }

    @Test
    public void whenDisplayVideoOnVideoCompleted_CallBannerVideoListenerOnVideoCompleted()
            throws IllegalAccessException {
        getDisplayVideoListener().onVideoCompleted();
        verify(mockBannerVideoListener).onVideoCompleted(bannerView);
    }

    @Test
    public void whenDisplayVideoOnVideoPaused_CallBannerVideoListenerOnVideoPaused()
            throws IllegalAccessException {
        getDisplayVideoListener().onVideoPaused();
        verify(mockBannerVideoListener).onVideoPaused(bannerView);
    }

    @Test
    public void whenDisplayVideoOnVideoResumed_CallBannerVideoListenerOnVideoResumed()
            throws IllegalAccessException {
        getDisplayVideoListener().onVideoResumed();
        verify(mockBannerVideoListener).onVideoResumed(bannerView);
    }

    @Test
    public void whenDisplayVideoOnVideoUnMuted_CallBannerVideoListenerOnVideoUnMuted()
            throws IllegalAccessException {
        getDisplayVideoListener().onVideoUnMuted();
        verify(mockBannerVideoListener).onVideoUnMuted(bannerView);
    }

    @Test
    public void whenDisplayVideoOnVideoMuted_CallBannerVideoListenerOnVideoMuted()
            throws IllegalAccessException {
        getDisplayVideoListener().onVideoMuted();
        verify(mockBannerVideoListener).onVideoMuted(bannerView);
    }

    @Test
    public void whenStopRefresh_BidLoaderCancelRefresh() {
        bannerView.stopRefresh();

        verify(mockBidLoader, times(1)).cancelRefresh();
    }

    @Test
    public void addUpdateRemoveClearContextData_EqualsGetContextDataDictionary() {
        Map<String, Set<String>> expectedMap = new HashMap<>();
        HashSet<String> value1 = new HashSet<>();
        value1.add("value1");
        HashSet<String> value2 = new HashSet<>();
        value2.add("value2");
        expectedMap.put("key1", value1);
        expectedMap.put("key2", value2);

        // add
        bannerView.addContextData("key1", "value1");
        bannerView.addContextData("key2", "value2");

        assertEquals(expectedMap, bannerView.getContextDataDictionary());

        // update
        HashSet<String> updateSet = new HashSet<>();
        updateSet.add("value3");
        bannerView.updateContextData("key1", updateSet);
        expectedMap.replace("key1", updateSet);

        assertEquals(expectedMap, bannerView.getContextDataDictionary());

        // remove
        bannerView.removeContextData("key1");
        expectedMap.remove("key1");
        assertEquals(expectedMap, bannerView.getContextDataDictionary());

        // clear
        bannerView.clearContextData();
        assertTrue(bannerView.getContextDataDictionary().isEmpty());
    }

    @Test
    public void addRemoveContextKeywords_EqualsGetContextKeyWordsSet() {
        HashSet<String> expectedSet = new HashSet<>();
        expectedSet.add("key1");
        expectedSet.add("key2");

        // add
        bannerView.addContextKeyword("key1");
        bannerView.addContextKeyword("key2");

        assertEquals(expectedSet, bannerView.getContextKeywordsSet());

        // remove
        bannerView.removeContextKeyword("key2");
        expectedSet.remove("key2");
        assertEquals(expectedSet, bannerView.getContextKeywordsSet());

        // clear
        bannerView.clearContextKeywords();
        assertTrue(bannerView.getContextKeywordsSet().isEmpty());

        // add all
        bannerView.addContextKeywords(expectedSet);
        assertEquals(expectedSet, bannerView.getContextKeywordsSet());
    }

    @Test
    public void setAdPosition_EqualsGetAdPosition() {
        bannerView.setAdPosition(null);
        assertEquals(BannerAdPosition.UNDEFINED, bannerView.getAdPosition());

        bannerView.setAdPosition(BannerAdPosition.FOOTER);
        assertEquals(BannerAdPosition.FOOTER, bannerView.getAdPosition());

        bannerView.setAdPosition(BannerAdPosition.HEADER);
        assertEquals(BannerAdPosition.HEADER, bannerView.getAdPosition());

        bannerView.setAdPosition(BannerAdPosition.SIDEBAR);
        assertEquals(BannerAdPosition.SIDEBAR, bannerView.getAdPosition());

        bannerView.setAdPosition(BannerAdPosition.UNKNOWN);
        assertEquals(BannerAdPosition.UNKNOWN, bannerView.getAdPosition());
    }

    @Test
    public void setPbAdSlot_EqualsGetPbAdSlot() {
        final String expected = "12345";
        bannerView.setPbAdSlot(expected);
        assertEquals(expected, bannerView.getPbAdSlot());
    }

    private BidRequesterListener getBidRequesterListener() {
        try {
            return (BidRequesterListener) WhiteBox.field(BannerView.class, "bidRequesterListener").get(bannerView);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private DisplayViewListener getDisplayViewListener() {
        try {
            return (DisplayViewListener) WhiteBox.field(BannerView.class, "displayViewListener").get(bannerView);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private DisplayVideoListener getDisplayVideoListener() {
        try {
            return (DisplayVideoListener) WhiteBox.field(BannerView.class, "displayVideoListener").get(bannerView);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BannerEventListener getBannerEventListener() {
        try {
            return (BannerEventListener) WhiteBox.field(BannerView.class, "bannerEventListener").get(bannerView);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}