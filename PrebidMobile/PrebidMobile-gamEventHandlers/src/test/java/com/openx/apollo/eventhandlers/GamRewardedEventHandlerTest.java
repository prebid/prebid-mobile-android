package com.openx.apollo.eventhandlers;

import android.app.Activity;
import android.os.Handler;

import com.apollo.test.utils.WhiteBox;
import com.openx.apollo.bidding.data.bid.Bid;
import com.openx.apollo.bidding.data.bid.Prebid;
import com.openx.apollo.bidding.listeners.RewardedVideoEventListener;
import com.openx.apollo.errors.AdException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class GamRewardedEventHandlerTest {
    private static final String GAM_AD_UNIT_ID = "12345678";

    private GamRewardedEventHandler mEventHandler;
    private Activity mActivity;

    @Mock
    private RewardedVideoEventListener mMockEventListener;
    @Mock
    private Handler mMockAppEventHandler;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        mActivity = Robolectric.buildActivity(Activity.class).create().get();

        mEventHandler = new GamRewardedEventHandler(mActivity, GAM_AD_UNIT_ID);
        mEventHandler.setRewardedEventListener(mMockEventListener);
    }

    @Test
    public void onAdMetadataChangedWithMetadataContainsAdEvent_HandleAppEvent() throws Exception {
        changeExpectingAppEventStatus(true);

        GamRewardedEventHandler spyEventHandler = spy(mEventHandler);

        spyEventHandler.onEvent(AdEvent.APP_EVENT_RECEIVED);

        final boolean isExpectingAppEvent = ((boolean) WhiteBox.field(GamRewardedEventHandler.class, "mIsExpectingAppEvent").get(spyEventHandler));

        verify(mMockEventListener, times(1)).onOXBSdkWin();
        assertFalse(isExpectingAppEvent);
    }

    @Test
    public void onGamAdClosed_NotifyEventCloseListener() {
        mEventHandler.onEvent(AdEvent.CLOSED);

        verify(mMockEventListener, times(1)).onAdClosed();
    }

    @Test
    public void onGamAdFailedToLoad_NotifyEventErrorListener() {
        final int wantedNumberOfInvocations = 10;

        for (int i = 0; i < wantedNumberOfInvocations; i++) {
            final AdEvent adEvent = AdEvent.FAILED;
            adEvent.setErrorCode(i);
            mEventHandler.onEvent(adEvent);
        }
        verify(mMockEventListener, times(wantedNumberOfInvocations)).onAdFailed(any(AdException.class));
    }

    @Test
    public void onGamAdOpened_NotifyBannerEventDisplayListener() {
        mEventHandler.onEvent(AdEvent.DISPLAYED);

        verify(mMockEventListener, times(1)).onAdDisplayed();
    }

    @Test
    public void onGamAdLoadedAppEventExpected_ScheduleAppEventHandler() {
        changeExpectingAppEventStatus(true);

        mEventHandler.onEvent(AdEvent.LOADED);

        assertNotNull(WhiteBox.getInternalState(mEventHandler, "mAppEventHandler"));
    }

    @Test
    public void onGamAdLoadedAppEventNotExpectedAndRequestInterstitialNotNull_NotifyEventListenerOnAdServerWin()
    throws Exception {
        RewardedAdWrapper publisherInterstitialAd = mock(RewardedAdWrapper.class);
        WhiteBox.field(GamRewardedEventHandler.class, "mRewardedAd")
                .set(mEventHandler, publisherInterstitialAd);

        mEventHandler.onEvent(AdEvent.LOADED);

        verify(mMockEventListener, times(1)).onAdServerWin(any());
    }

    @Test
    public void onAppEventTimeout_NotifyBannerEventOnAdServerWin() throws Exception {
        WhiteBox.method(GamRewardedEventHandler.class, "handleAppEventTimeout").invoke(mEventHandler);

        verify(mMockEventListener, times(1)).onAdServerWin(any());
    }

    @Test
    public void destroy_CancelTimer() throws IllegalAccessException {
        // by default apEventHandler is null if not scheduled
        WhiteBox.field(GamRewardedEventHandler.class, "mAppEventHandler")
                .set(mEventHandler, mMockAppEventHandler);

        mEventHandler.destroy();

        verify(mMockAppEventHandler, times(1)).removeCallbacksAndMessages(null);
    }

    @Test
    public void requestAdWithDifferentBids_VerifyAdStatus() {
        final Bid mockBid = mock(Bid.class);
        final Prebid mockPrebid = mock(Prebid.class);
        when(mockPrebid.getTargeting()).thenReturn(new HashMap<>());

        when(mockBid.getPrebid()).thenReturn(mockPrebid);
        when(mockBid.getPrice()).thenReturn(0.2);
        mEventHandler.requestAdWithBid(mockBid);
        assertTrue(getExpectingAppEventStatus());

        when(mockBid.getPrice()).thenReturn(0.0);
        mEventHandler.requestAdWithBid(mockBid);
        assertFalse(getExpectingAppEventStatus());

        mEventHandler.requestAdWithBid(null);
        assertFalse(getExpectingAppEventStatus());
    }

    @Test
    public void showWhenEmbeddedInterstitialIsNull_NotifyEventErrorListener() {
        mEventHandler.show();

        verify(mMockEventListener).onAdFailed(any(AdException.class));
    }

    private void changeExpectingAppEventStatus(boolean status) {
        WhiteBox.setInternalState(mEventHandler, "mIsExpectingAppEvent", status);
    }

    private boolean getExpectingAppEventStatus() {
        return WhiteBox.getInternalState(mEventHandler, "mIsExpectingAppEvent");
    }
}