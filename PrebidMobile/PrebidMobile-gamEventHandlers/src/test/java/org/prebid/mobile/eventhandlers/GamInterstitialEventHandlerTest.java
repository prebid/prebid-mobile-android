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

package org.prebid.mobile.eventhandlers;

import android.app.Activity;
import android.os.Handler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.bidding.listeners.InterstitialEventListener;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class GamInterstitialEventHandlerTest {
    private static final String GAM_AD_UNIT_ID = "12345678";

    private GamInterstitialEventHandler eventHandler;

    @Mock private InterstitialEventListener mockEventListener;
    @Mock private Handler mockAppEventHandler;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Activity activity = Robolectric.buildActivity(Activity.class).get();

        eventHandler = new GamInterstitialEventHandler(activity, GAM_AD_UNIT_ID);
        eventHandler.setInterstitialEventListener(mockEventListener);
    }

    @Test
    public void onAppEventWithValidNameAndExpectedAppEvent_HandleAppEvent() {
        changeExpectingAppEventStatus(true);

        eventHandler.onEvent(AdEvent.APP_EVENT_RECEIVED);

        verify(mockEventListener, times(1)).onPrebidSdkWin();
        assertFalse(getExpectingAppEventStatus());
    }

    @Test
    public void onGamAdClosed_NotifyEventCloseListener() {
        eventHandler.onEvent(AdEvent.CLOSED);

        verify(mockEventListener, times(1)).onAdClosed();
    }

    @Test
    public void onGamAdFailedToLoad_NotifyEventErrorListener() {
        final int wantedNumberOfInvocations = 10;

        for (int i = 0; i < wantedNumberOfInvocations; i++) {
            final AdEvent adEvent = AdEvent.FAILED;
            adEvent.setErrorCode(i);
            eventHandler.onEvent(adEvent);
        }
        verify(mockEventListener, times(wantedNumberOfInvocations)).onAdFailed(any(AdException.class));
    }

    @Test
    public void onGamAdOpened_NotifyBannerEventDisplayListener() {
        eventHandler.onEvent(AdEvent.DISPLAYED);

        verify(mockEventListener, times(1)).onAdDisplayed();
    }

    @Test
    public void onGamAdLoadedAppEventExpected_ScheduleAppEventHandler() {
        changeExpectingAppEventStatus(true);

        eventHandler.onEvent(AdEvent.LOADED);

        assertNotNull(WhiteBox.getInternalState(eventHandler, "appEventHandler"));
    }

    @Test
    public void onGamAdLoadedAppEventNotExpectedAndRequestInterstitialNotNull_NotifyEventListenerOnAdServerWin()
    throws Exception {
        PublisherInterstitialAdWrapper publisherInterstitialAd = mock(PublisherInterstitialAdWrapper.class);
        WhiteBox.field(GamInterstitialEventHandler.class, "requestInterstitial")
                .set(eventHandler, publisherInterstitialAd);

        eventHandler.onEvent(AdEvent.LOADED);

        verify(mockEventListener, times(1)).onAdServerWin();
    }

    @Test
    public void onAppEventTimeout_NotifyBannerEventOnAdServerWin() throws Exception {
        WhiteBox.method(GamInterstitialEventHandler.class, "handleAppEventTimeout").invoke(eventHandler);

        verify(mockEventListener, times(1)).onAdServerWin();
    }

    @Test
    public void destroy_CancelTimer() throws IllegalAccessException {
        // by default apEventHandler is null if not scheduled
        WhiteBox.field(GamInterstitialEventHandler.class, "appEventHandler").set(eventHandler, mockAppEventHandler);

        eventHandler.destroy();

        verify(mockAppEventHandler, times(1)).removeCallbacksAndMessages(null);
    }

    @Test
    public void requestAdWithDifferentBids_VerifyAdStatus() {
        final Bid mockBid = mock(Bid.class);
        final Prebid mockPrebid = mock(Prebid.class);
        when(mockPrebid.getTargeting()).thenReturn(new HashMap<>());

        when(mockBid.getPrebid()).thenReturn(mockPrebid);
        when(mockBid.getPrice()).thenReturn(0.2);
        eventHandler.requestAdWithBid(mockBid);
        assertTrue(getExpectingAppEventStatus());

        when(mockBid.getPrice()).thenReturn(0.0);
        eventHandler.requestAdWithBid(mockBid);
        assertFalse(getExpectingAppEventStatus());

        eventHandler.requestAdWithBid(null);
        assertFalse(getExpectingAppEventStatus());
    }

    @Test
    public void showWhenEmbeddedInterstitialIsNull_NotifyEventErrorListener() {
        eventHandler.show();

        verify(mockEventListener).onAdFailed(any(AdException.class));
    }

    private void changeExpectingAppEventStatus(boolean status) {
        WhiteBox.setInternalState(eventHandler, "isExpectingAppEvent", status);
    }

    private boolean getExpectingAppEventStatus() {
        return WhiteBox.getInternalState(eventHandler, "isExpectingAppEvent");
    }
}
