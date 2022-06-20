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
import android.content.Context;
import android.os.Handler;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.bidding.listeners.BannerEventListener;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class GamBannerEventHandlerTest {

    private static final String GAM_AD_UNIT_ID = "12345678";
    private static final AdSize GAM_AD_SIZE = new AdSize(350, 50);

    private GamBannerEventHandler bannerEventHandler;

    @Mock private BannerEventListener mockBannerEventListener;
    @Mock private Handler mockAppEventHandler;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Context context = Robolectric.buildActivity(Activity.class).get();

        bannerEventHandler = new GamBannerEventHandler(context, GAM_AD_UNIT_ID, GAM_AD_SIZE);
        bannerEventHandler.setBannerEventListener(mockBannerEventListener);
    }

    @Test
    public void onAppEventWithValidNameAndExpectedAppEvent_HandleAppEvent() {
        changeExpectingAppEventStatus(true);

        bannerEventHandler.onEvent(AdEvent.APP_EVENT_RECEIVED);

        verify(mockBannerEventListener, times(1)).onPrebidSdkWin();
        assertFalse(getExpectingAppEventStatus());
    }

    @Test
    public void onGamAdClosed_NotifyBannerEventCloseListener() {
        bannerEventHandler.onEvent(AdEvent.CLOSED);

        verify(mockBannerEventListener, times(1)).onAdClosed();
    }

    @Test
    public void onGamAdFailedToLoad_NotifyBannerEventErrorListener() {
        final int wantedNumberOfInvocations = 10;

        for (int i = 0; i < wantedNumberOfInvocations; i++) {
            final AdEvent adEvent = AdEvent.FAILED;
            adEvent.setErrorCode(i);
            bannerEventHandler.onEvent(adEvent);
        }
        verify(mockBannerEventListener, times(wantedNumberOfInvocations)).onAdFailed(any(AdException.class));
    }

    @Test
    public void onGamAdOpened_NotifyBannerEventClickedListener() {
        bannerEventHandler.onEvent(AdEvent.CLICKED);

        verify(mockBannerEventListener, times(1)).onAdClicked();
    }

    @Test
    public void onGamAdLoadedAppEventExpected_ScheduleAppEventHandler() {
        changeExpectingAppEventStatus(true);

        bannerEventHandler.onEvent(AdEvent.LOADED);

        assertNotNull(WhiteBox.getInternalState(bannerEventHandler, "appEventHandler"));
    }

    @Test
    public void onGamAdLoadedAppEventNotExpectedRequestBannerNotNull_NotifyBannerEventOnAdServerWin()
    throws Exception {
        // can't mock a final view class
        final PublisherAdViewWrapper mockPublisherAdView = mock(PublisherAdViewWrapper.class);
        final View mockView = mock(View.class);
        when(mockPublisherAdView.getView()).thenReturn(mockView);

        WhiteBox.field(GamBannerEventHandler.class, "requestBanner").set(bannerEventHandler, mockPublisherAdView);

        bannerEventHandler.onEvent(AdEvent.LOADED);

        verify(mockBannerEventListener, times(1)).onAdServerWin(eq(mockView));
    }

    @Test
    public void onAppEventTimeout_NotifyBannerEventOnAdServerWin() throws Exception {
        WhiteBox.method(GamBannerEventHandler.class, "handleAppEventTimeout").invoke(bannerEventHandler);

        verify(mockBannerEventListener, times(1)).onAdServerWin(any());
    }

    @Test
    public void destroy_CancelTimer() throws IllegalAccessException {
        // by default apEventHandler is null if not scheduled
        WhiteBox.field(GamBannerEventHandler.class, "appEventHandler").set(bannerEventHandler, mockAppEventHandler);

        bannerEventHandler.destroy();

        verify(mockAppEventHandler, times(1)).removeCallbacksAndMessages(null);
    }

    @Test
    public void requestAdWithDifferentBids_VerifyAdStatus() {
        final Bid mockBid = mock(Bid.class);
        final Prebid mockPrebid = mock(Prebid.class);
        when(mockPrebid.getTargeting()).thenReturn(new HashMap<>());

        when(mockBid.getPrebid()).thenReturn(mockPrebid);
        when(mockBid.getPrice()).thenReturn(0.2);
        bannerEventHandler.requestAdWithBid(mockBid);
        assertTrue(getExpectingAppEventStatus());

        when(mockBid.getPrice()).thenReturn(0.0);
        bannerEventHandler.requestAdWithBid(mockBid);
        assertFalse(getExpectingAppEventStatus());

        bannerEventHandler.requestAdWithBid(null);
        assertFalse(getExpectingAppEventStatus());
    }

    @Test
    public void convertGamAdSize_ReturnPrebidSizesArray() {
        AdSize[] prebidSizes = GamBannerEventHandler.convertGamAdSize(com.google.android.gms.ads.AdSize.FLUID,
                com.google.android.gms.ads.AdSize.BANNER);
        assertEquals(com.google.android.gms.ads.AdSize.FLUID.getWidth(), prebidSizes[0].getWidth());
        assertEquals(com.google.android.gms.ads.AdSize.FLUID.getHeight(), prebidSizes[0].getHeight());
        assertEquals(com.google.android.gms.ads.AdSize.BANNER.getWidth(), prebidSizes[1].getWidth());
        assertEquals(com.google.android.gms.ads.AdSize.BANNER.getHeight(), prebidSizes[1].getHeight());
    }

    @Test
    public void convertGamAdSizeAndNullPassed_ReturnEmptyPrebidSizesArray() {
        AdSize[] prebidSizes = GamBannerEventHandler.convertGamAdSize(null);
        assertEquals(0, prebidSizes.length);
    }

    private void changeExpectingAppEventStatus(boolean status) {
        WhiteBox.setInternalState(bannerEventHandler, "isExpectingAppEvent", status);
    }

    private boolean getExpectingAppEventStatus() {
        return WhiteBox.getInternalState(bannerEventHandler, "isExpectingAppEvent");
    }
}