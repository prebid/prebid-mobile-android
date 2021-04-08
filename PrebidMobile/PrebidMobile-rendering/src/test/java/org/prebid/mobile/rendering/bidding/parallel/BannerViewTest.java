package org.prebid.mobile.rendering.bidding.parallel;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import com.apollo.test.utils.WhiteBox;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.DisplayView;
import org.prebid.mobile.rendering.bidding.enums.BannerAdPosition;
import org.prebid.mobile.rendering.bidding.enums.VideoPlacementType;
import org.prebid.mobile.rendering.bidding.interfaces.BannerEventHandler;
import org.prebid.mobile.rendering.bidding.interfaces.StandaloneBannerEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.BannerEventListener;
import org.prebid.mobile.rendering.bidding.listeners.BannerViewListener;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.prebid.mobile.rendering.utils.broadcast.ScreenStateReceiver;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BannerViewTest {
    private static final String AD_UNIT_ID = "12345678";
    private static final AdSize AD_SIZE = new AdSize(320, 50);

    private BannerView mBannerView;

    private Context mMockContext;
    @Mock
    private BidLoader mMockBidLoader;
    @Mock
    private BannerEventHandler mMockEventHandler;
    @Mock
    private BannerViewListener mMockBannerListener;
    @Mock
    private DisplayView mMockDisplayView;
    @Mock
    private ScreenStateReceiver mMockScreenStateReceiver;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        mMockContext = Robolectric.buildActivity(Activity.class).create().get();

        when(mMockEventHandler.getAdSizeArray()).thenReturn(new AdSize[]{AD_SIZE});
        mBannerView = new BannerView(mMockContext, AD_UNIT_ID, mMockEventHandler);
        WhiteBox.field(BannerView.class, "mBidLoader").set(mBannerView, mMockBidLoader);
        WhiteBox.field(BannerView.class, "mDisplayView").set(mBannerView, mMockDisplayView);
        WhiteBox.field(BannerView.class, "mDisplayView").set(mBannerView, mMockDisplayView);
        WhiteBox.field(BannerView.class, "mScreenStateReceiver").set(mBannerView, mMockScreenStateReceiver);
        mBannerView.setBannerListener(mMockBannerListener);

        assertEquals(BannerAdPosition.UNDEFINED.getValue(), mBannerView.getAdPosition().getValue());
    }

    @Test
    public void createOXBBannerViewWithAttributes_InstanceCreated() {
        AttributeSet attributes = Robolectric.buildAttributeSet().build();
        BannerView bannerView = new BannerView(mMockContext, attributes);

        assertNotNull(bannerView);
    }

    @Test
    public void createOXBBannerViewNoEventHandler_InstanceCreatedAndBidLoaderIsNotNullAndStandaloneEventHandlerProvided()
    throws IllegalAccessException {
        BannerView bannerView = new BannerView(mMockContext, AD_UNIT_ID, AD_SIZE);

        Object eventHandler = WhiteBox.field(BannerView.class, "mEventHandler").get(bannerView);
        BidLoader bidLoader = ((BidLoader) WhiteBox.field(BannerView.class, "mBidLoader").get(bannerView));

        assertNotNull(bannerView);
        assertTrue(eventHandler instanceof StandaloneBannerEventHandler);
        assertNotNull(bidLoader);
    }

    @Test
    public void loadAdWithNullBidLoader_NoExceptionIsThrown() throws IllegalAccessException {
        WhiteBox.field(BannerView.class, "mBidLoader").set(mBannerView, null);

        mBannerView.loadAd();
    }

    @Test
    public void loadAdWithPrimaryAdServerRequestInProgress_DoNothing() {
        changePrimaryAdServerRequestStatus(true);
        mBannerView.loadAd();

        verifyZeroInteractions(mMockBidLoader);
    }

    @Test
    public void loadAd_NativeWithEmptyOrNullNativeStylesCreative_DoNothing()
    throws IllegalAccessException {
        NativeAdConfiguration nativeAdConfiguration = new NativeAdConfiguration();
        BannerView ppmBannerView = new BannerView(mMockContext, AD_UNIT_ID, AD_SIZE);
        WhiteBox.field(BannerView.class, "mBidLoader").set(ppmBannerView, mMockBidLoader);

        nativeAdConfiguration.setNativeStylesCreative("");
        ppmBannerView.setNativeAdConfiguration(nativeAdConfiguration);

        ppmBannerView.loadAd();

        nativeAdConfiguration.setNativeStylesCreative(null);
        ppmBannerView.loadAd();

        verifyZeroInteractions(mMockBidLoader);
    }

    @Test
    public void loadAdWithValidBidLoaderAndPrimaryAdRequestNotInProgress_ExecuteBidLoad() {
        mBannerView.loadAd();

        verify(mMockBidLoader, times(1)).load();
    }

    @Test
    public void destroy_DestroyEventHandlerAndBidLoader() {
        mBannerView.destroy();

        verify(mMockEventHandler).destroy();
        verify(mMockBidLoader).destroy();
        verify(mMockScreenStateReceiver).unregister();
    }

    @Test
    public void setAutoRefreshDelayInSec_EqualsGetAutoRefreshDelayInMs() {
        final int interval = 15;
        mBannerView.setAutoRefreshDelay(interval);

        assertEquals(interval * 1000, mBannerView.getAutoRefreshDelayInMs());
    }

    @Test
    public void setRefreshIntervalInSecSmallerThanZero_ExpectDefaultRefresh() {
        final int interval = -1;
        mBannerView.setAutoRefreshDelay(interval);

        assertEquals(60000, mBannerView.getAutoRefreshDelayInMs());
    }

    @Test
    public void addAdditionalSizes_GetterAndAdUnitConfigContainSetValueAndInitialSize() {
        final AdSize adSize = new AdSize(10, 20);
        final Set<AdSize> expectedSet = new HashSet<>();
        expectedSet.add(AD_SIZE); // from eventHandler
        expectedSet.add(adSize);

        mBannerView.addAdditionalSizes(adSize);

        assertEquals(expectedSet, mBannerView.getAdditionalSizes());
    }

    @Test
    public void setVideoPlacementType_AdUnitIdentifierTypeIsVASTAndVideoPlacementIsUpdated()
    throws IllegalAccessException {
        final VideoPlacementType videoPlacement = VideoPlacementType.IN_BANNER;
        AdConfiguration mockAdConfiguration = mock(AdConfiguration.class);
        WhiteBox.field(BannerView.class, "mAdUnitConfig").set(mBannerView, mockAdConfiguration);

        mBannerView.setVideoPlacementType(videoPlacement);

        verify(mockAdConfiguration, times(1)).setPlacementType(eq(VideoPlacementType.mapToPlacementType(videoPlacement)));
        verify(mockAdConfiguration, times(1)).setAdUnitIdentifierType(eq(AdConfiguration.AdUnitIdentifierType.VAST));
    }

    @Test
    public void setAdVideoPlacement_EqualsGetVideoPlacement() {
        mBannerView.setVideoPlacementType(null);
        assertNull(mBannerView.getVideoPlacementType());

        mBannerView.setVideoPlacementType(VideoPlacementType.IN_ARTICLE);
        assertEquals(VideoPlacementType.IN_ARTICLE, mBannerView.getVideoPlacementType());

        mBannerView.setVideoPlacementType(VideoPlacementType.IN_BANNER);
        assertEquals(VideoPlacementType.IN_BANNER, mBannerView.getVideoPlacementType());

        mBannerView.setVideoPlacementType(VideoPlacementType.IN_FEED);
        assertEquals(VideoPlacementType.IN_FEED, mBannerView.getVideoPlacementType());
    }

    @Test
    public void whenSetNativeAdConfiguration_ConfigAssignedToAdConfiguration() {
        AdConfiguration mockConfiguration = mock(AdConfiguration.class);
        WhiteBox.setInternalState(mBannerView, "mAdUnitConfig", mockConfiguration);
        mBannerView.setNativeAdConfiguration(mock(NativeAdConfiguration.class));
        verify(mockConfiguration).setNativeAdConfiguration(any(NativeAdConfiguration.class));
    }

    //region ======================= BidRequestListener tests
    @Test
    public void onFetchComplete_AssignWinningBidMarkPrimaryServerRequestInProgressRequestAdWithBid() {
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final Bid mockBid = mock(Bid.class);

        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);

        BidRequesterListener listener = getBidRequesterListener();
        listener.onFetchCompleted(mockBidResponse);

        Bid winningBid = mBannerView.getWinnerBid();

        assertEquals(winningBid, mockBid);
        verify(mMockEventHandler, times(1)).requestAdWithBid(eq(mockBid));
        assertTrue(mBannerView.isPrimaryAdServerRequestInProgress());
    }

    @Test
    public void onError_NullifyWinnerBidAndRequestAdWitNullBid() {
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final Bid mockBid = mock(Bid.class);

        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);
        mBannerView.setBidResponse(mockBidResponse);

        BidRequesterListener listener = getBidRequesterListener();
        listener.onError(any());

        assertNull(mBannerView.getWinnerBid());
        verify(mMockEventHandler, times(1)).requestAdWithBid(eq(null));
    }
    //endregion ======================= BidRequestListener tests

    //region ================= BannerEventListener tests
    @Test
    public void onOxbSdkWinAndWinnerBidIsNull_AdRequestStatusIsFinishedNotifyErrorListener() {
        changePrimaryAdServerRequestStatus(true);
        final BannerEventListener bannerEventListener = getBannerEventListener();
        mBannerView.setBidResponse(null);

        bannerEventListener.onOXBSdkWin();

        verify(mMockBannerListener, times(1)).onAdFailed(eq(mBannerView), any(AdException.class));
        assertFalse(mBannerView.isPrimaryAdServerRequestInProgress());
    }

    @Test
    public void onOxbSdkWin_AdRequestStatusIsFinishedDisplayAdView() {
        changePrimaryAdServerRequestStatus(true);
        final BannerEventListener bannerEventListener = getBannerEventListener();
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final Bid mockBid = mock(Bid.class);

        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);
        when(mockBidResponse.getWinningBidWidthHeightPairDips(any())).thenReturn(Pair.create(0, 0));

        mBannerView.setBidResponse(mockBidResponse);

        bannerEventListener.onOXBSdkWin();

        assertFalse(mBannerView.isPrimaryAdServerRequestInProgress());
    }

    @Test
    public void onAdServerWin_AdRequestStatusIsFinishedDisplayAdViewAndNotifyAdReceivedListener() {
        changePrimaryAdServerRequestStatus(true);

        final View mockView = mock(View.class);
        // final OXBBannerView spyBannerView = spy(mOXBBannerView);
        final BannerEventListener bannerEventListener = getBannerEventListener();

        bannerEventListener.onAdServerWin(mockView);

        assertFalse(mBannerView.isPrimaryAdServerRequestInProgress());
        verify(mMockBannerListener, times(1)).onAdLoaded(mBannerView);
        // verify(spyBannerView).displayAdServerView(mockView);
    }

    @Test
    public void onFailedAndNoWinnerBid_AdRequestStatusIsFinishedNotifyErrorListener() {
        changePrimaryAdServerRequestStatus(true);
        mBannerView.setBidResponse(null);

        final AdException exception = new AdException(AdException.INTERNAL_ERROR, "Test");
        final BannerEventListener bannerEventListener = getBannerEventListener();

        bannerEventListener.onAdFailed(exception);

        verify(mMockBannerListener, times(1)).onAdFailed(mBannerView, exception);
    }

    @Test
    public void onFailedAndWithWinnerBid_AdRequestStatusIsFinishedNotifyOxbSdkWin() {
        changePrimaryAdServerRequestStatus(true);

        final BidResponse mockBidResponse = mock(BidResponse.class);
        final Bid mockBid = mock(Bid.class);
        final BannerEventListener spyEventListener = spy(getBannerEventListener());
        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);
        when(mockBidResponse.getWinningBidWidthHeightPairDips(any())).thenReturn(Pair.create(0, 0));

        mBannerView.setBidResponse(mockBidResponse);
        spyEventListener.onAdFailed(new AdException(AdException.INTERNAL_ERROR, "Test"));

        assertFalse(mBannerView.isPrimaryAdServerRequestInProgress());
        verify(spyEventListener, times(1)).onOXBSdkWin();
    }

    @Test
    public void onAdOpened_NotifyAdClickedListener() {
        final BannerEventListener bannerEventListener = getBannerEventListener();
        bannerEventListener.onAdClicked();

        verify(mMockBannerListener, times(1)).onAdClicked(mBannerView);
    }

    @Test
    public void onAdClosed_NotifyAdClosedListener() {
        final BannerEventListener bannerEventListener = getBannerEventListener();
        bannerEventListener.onAdClosed();

        verify(mMockBannerListener, times(1)).onAdClosed(mBannerView);
    }
    //endregion ================= BannerEventListener test

    private void changePrimaryAdServerRequestStatus(boolean isLoading) {
        try {
            WhiteBox.field(BannerView.class, "mIsPrimaryAdServerRequestInProgress").set(mBannerView, isLoading);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void whenLoadAd_CallBidLoaderLoad() {
        mBannerView.loadAd();
        verify(mMockBidLoader).load();
    }

    @Test
    public void whenDisplayViewOnAdLoaded_CallBannerListenerOnAdLoaded()
    throws IllegalAccessException {
        getDisplayViewListener().onAdLoaded();
        verify(mMockBannerListener).onAdLoaded(mBannerView);
    }

    @Test
    public void whenDisplayViewOnAdDisplayed_CallBannerListenerOnAdDisplayedAndTrackImpression()
    throws IllegalAccessException {
        getDisplayViewListener().onAdDisplayed();
        verify(mMockBannerListener).onAdDisplayed(mBannerView);
        verify(mMockEventHandler).trackImpression();
    }

    @Test
    public void whenDisplayViewOnAdFailed_CallBannerListenerOnAdFailed()
    throws IllegalAccessException {
        getDisplayViewListener().onAdFailed(new AdException(AdException.INTERNAL_ERROR, ""));
        verify(mMockBannerListener).onAdFailed(eq(mBannerView), any(AdException.class));
    }

    @Test
    public void whenDisplayViewOnAdOpened_CallBannerListenerOnAdClicked()
    throws IllegalAccessException {
        getDisplayViewListener().onAdClicked();
        verify(mMockBannerListener).onAdClicked(mBannerView);
    }

    @Test
    public void whenDisplayViewOnAdClosed_CallBannerListenerOnAdClosed()
    throws IllegalAccessException {
        getDisplayViewListener().onAdClosed();
        verify(mMockBannerListener).onAdClosed(mBannerView);
    }

    @Test
    public void whenStopRefresh_BidLoaderCancelRefresh() {
        mBannerView.stopRefresh();

        verify(mMockBidLoader, times(1)).cancelRefresh();
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
        mBannerView.addContextData("key1", "value1");
        mBannerView.addContextData("key2", "value2");

        assertEquals(expectedMap, mBannerView.getContextDataDictionary());

        // update
        HashSet<String> updateSet = new HashSet<>();
        updateSet.add("value3");
        mBannerView.updateContextData("key1", updateSet);
        expectedMap.replace("key1", updateSet);

        assertEquals(expectedMap, mBannerView.getContextDataDictionary());

        // remove
        mBannerView.removeContextData("key1");
        expectedMap.remove("key1");
        assertEquals(expectedMap, mBannerView.getContextDataDictionary());

        // clear
        mBannerView.clearContextData();
        assertTrue(mBannerView.getContextDataDictionary().isEmpty());
    }

    @Test
    public void addRemoveContextKeywords_EqualsGetContextKeyWordsSet() {
        HashSet<String> expectedSet = new HashSet<>();
        expectedSet.add("key1");
        expectedSet.add("key2");

        // add
        mBannerView.addContextKeyword("key1");
        mBannerView.addContextKeyword("key2");

        assertEquals(expectedSet, mBannerView.getContextKeywordsSet());

        // remove
        mBannerView.removeContextKeyword("key2");
        expectedSet.remove("key2");
        assertEquals(expectedSet, mBannerView.getContextKeywordsSet());

        // clear
        mBannerView.clearContextKeywords();
        assertTrue(mBannerView.getContextKeywordsSet().isEmpty());

        // add all
        mBannerView.addContextKeywords(expectedSet);
        assertEquals(expectedSet, mBannerView.getContextKeywordsSet());
    }

    @Test
    public void setAdPosition_EqualsGetAdPosition() {
        mBannerView.setAdPosition(null);
        assertEquals(BannerAdPosition.UNDEFINED, mBannerView.getAdPosition());

        mBannerView.setAdPosition(BannerAdPosition.FOOTER);
        assertEquals(BannerAdPosition.FOOTER, mBannerView.getAdPosition());

        mBannerView.setAdPosition(BannerAdPosition.HEADER);
        assertEquals(BannerAdPosition.HEADER, mBannerView.getAdPosition());

        mBannerView.setAdPosition(BannerAdPosition.SIDEBAR);
        assertEquals(BannerAdPosition.SIDEBAR, mBannerView.getAdPosition());

        mBannerView.setAdPosition(BannerAdPosition.UNKNOWN);
        assertEquals(BannerAdPosition.UNKNOWN, mBannerView.getAdPosition());
    }

    private BidRequesterListener getBidRequesterListener() {
        try {
            return (BidRequesterListener) WhiteBox.field(BannerView.class, "mBidRequesterListener").get(mBannerView);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private DisplayViewListener getDisplayViewListener() {
        try {
            return (DisplayViewListener) WhiteBox.field(BannerView.class, "mDisplayViewListener").get(mBannerView);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BannerEventListener getBannerEventListener() {
        try {
            return (BannerEventListener) WhiteBox.field(BannerView.class, "mBannerEventListener").get(mBannerView);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}