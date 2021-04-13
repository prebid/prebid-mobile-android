package org.prebid.mobile.eventhandlers;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.bidding.listeners.InterstitialEventListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class GamInterstitialEventHandlerTest {
    private static final String GAM_AD_UNIT_ID = "12345678";

    private GamInterstitialEventHandler mEventHandler;
    private Context mContext;

    @Mock
    private InterstitialEventListener mMockEventListener;
    @Mock
    private Handler mMockAppEventHandler;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        mContext = Robolectric.buildActivity(Activity.class).create().get();

        mEventHandler = new GamInterstitialEventHandler(mContext, GAM_AD_UNIT_ID);
        mEventHandler.setInterstitialEventListener(mMockEventListener);
    }

    @Test
    public void onAppEventWithValidNameAndExpectedAppEvent_HandleAppEvent() {
        changeExpectingAppEventStatus(true);

        mEventHandler.onEvent(AdEvent.APP_EVENT_RECEIVED);

        verify(mMockEventListener, times(1)).onOXBSdkWin();
        assertFalse(getExpectingAppEventStatus());
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
        PublisherInterstitialAdWrapper publisherInterstitialAd = mock(PublisherInterstitialAdWrapper.class);
        WhiteBox.field(GamInterstitialEventHandler.class, "mRequestInterstitial")
                .set(mEventHandler, publisherInterstitialAd);

        mEventHandler.onEvent(AdEvent.LOADED);

        verify(mMockEventListener, times(1)).onAdServerWin();
    }

    @Test
    public void onAppEventTimeout_NotifyBannerEventOnAdServerWin() throws Exception {
        WhiteBox.method(GamInterstitialEventHandler.class, "handleAppEventTimeout").invoke(mEventHandler);

        verify(mMockEventListener, times(1)).onAdServerWin();
    }

    @Test
    public void destroy_CancelTimer() throws IllegalAccessException {
        // by default apEventHandler is null if not scheduled
        WhiteBox.field(GamInterstitialEventHandler.class, "mAppEventHandler")
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
