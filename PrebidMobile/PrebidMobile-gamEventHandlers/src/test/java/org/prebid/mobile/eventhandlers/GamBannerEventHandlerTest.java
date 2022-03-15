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
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.rendering.bidding.listeners.BannerEventListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class GamBannerEventHandlerTest {
    private static final String GAM_AD_UNIT_ID = "12345678";
    private static final AdSize GAM_AD_SIZE = new AdSize(350 ,50);

    private GamBannerEventHandler mBannerEventHandler;

    @Mock private BannerEventListener mMockBannerEventListener;
    @Mock private Handler mMockAppEventHandler;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Context context = Robolectric.buildActivity(Activity.class).get();

        mBannerEventHandler = new GamBannerEventHandler(context, GAM_AD_UNIT_ID, GAM_AD_SIZE);
        mBannerEventHandler.setBannerEventListener(mMockBannerEventListener);
    }

    @Test
    public void onAppEventWithValidNameAndExpectedAppEvent_HandleAppEvent() {
        changeExpectingAppEventStatus(true);

        mBannerEventHandler.onEvent(AdEvent.APP_EVENT_RECEIVED);

        verify(mMockBannerEventListener, times(1)).onPrebidSdkWin();
        assertFalse(getExpectingAppEventStatus());
    }

    @Test
    public void onGamAdClosed_NotifyBannerEventCloseListener() {
        mBannerEventHandler.onEvent(AdEvent.CLOSED);

        verify(mMockBannerEventListener, times(1)).onAdClosed();
    }

    @Test
    public void onGamAdFailedToLoad_NotifyBannerEventErrorListener() {
        final int wantedNumberOfInvocations = 10;

        for (int i = 0; i < wantedNumberOfInvocations; i++) {
            final AdEvent adEvent = AdEvent.FAILED;
            adEvent.setErrorCode(i);
            mBannerEventHandler.onEvent(adEvent);
        }
        verify(mMockBannerEventListener, times(wantedNumberOfInvocations)).onAdFailed(any(AdException.class));
    }

    @Test
    public void onGamAdOpened_NotifyBannerEventClickedListener() {
        mBannerEventHandler.onEvent(AdEvent.CLICKED);

        verify(mMockBannerEventListener, times(1)).onAdClicked();
    }

    @Test
    public void onGamAdLoadedAppEventExpected_ScheduleAppEventHandler() {
        changeExpectingAppEventStatus(true);

        mBannerEventHandler.onEvent(AdEvent.LOADED);

        assertNotNull(WhiteBox.getInternalState(mBannerEventHandler, "mAppEventHandler"));
    }

    @Test
    public void onGamAdLoadedAppEventNotExpectedRequestBannerNotNull_NotifyBannerEventOnAdServerWin()
    throws Exception {
        // can't mock a final view class
        final PublisherAdViewWrapper mockPublisherAdView = mock(PublisherAdViewWrapper.class);
        final View mockView = mock(View.class);
        when(mockPublisherAdView.getView()).thenReturn(mockView);

        WhiteBox.field(GamBannerEventHandler.class, "mRequestBanner").set(mBannerEventHandler, mockPublisherAdView);

        mBannerEventHandler.onEvent(AdEvent.LOADED);

        verify(mMockBannerEventListener, times(1)).onAdServerWin(eq(mockView));
    }

    @Test
    public void onAppEventTimeout_NotifyBannerEventOnAdServerWin() throws Exception {
        WhiteBox.method(GamBannerEventHandler.class, "handleAppEventTimeout").invoke(mBannerEventHandler);

        verify(mMockBannerEventListener, times(1)).onAdServerWin(any());
    }

    @Test
    public void destroy_CancelTimer() throws IllegalAccessException {
        // by default apEventHandler is null if not scheduled
        WhiteBox.field(GamBannerEventHandler.class, "mAppEventHandler").set(mBannerEventHandler, mMockAppEventHandler);

        mBannerEventHandler.destroy();

        verify(mMockAppEventHandler, times(1)).removeCallbacksAndMessages(null);
    }

    @Test
    public void requestAdWithDifferentBids_VerifyAdStatus() {
        final Bid mockBid = mock(Bid.class);
        final Prebid mockPrebid = mock(Prebid.class);
        when(mockPrebid.getTargeting()).thenReturn(new HashMap<>());

        when(mockBid.getPrebid()).thenReturn(mockPrebid);
        when(mockBid.getPrice()).thenReturn(0.2);
        mBannerEventHandler.requestAdWithBid(mockBid);
        assertTrue(getExpectingAppEventStatus());

        when(mockBid.getPrice()).thenReturn(0.0);
        mBannerEventHandler.requestAdWithBid(mockBid);
        assertFalse(getExpectingAppEventStatus());

        mBannerEventHandler.requestAdWithBid(null);
        assertFalse(getExpectingAppEventStatus());
    }

    @Test
    public void convertGamAdSize_ReturnPrebidSizesArray() {
        AdSize[] prebidSizes = GamBannerEventHandler.convertGamAdSize(com.google.android.gms.ads.AdSize.FLUID,
                                                                      com.google.android.gms.ads.AdSize.BANNER);
        assertEquals(com.google.android.gms.ads.AdSize.FLUID.getWidth(), prebidSizes[0].width);
        assertEquals(com.google.android.gms.ads.AdSize.FLUID.getHeight(), prebidSizes[0].height);
        assertEquals(com.google.android.gms.ads.AdSize.BANNER.getWidth(), prebidSizes[1].width);
        assertEquals(com.google.android.gms.ads.AdSize.BANNER.getHeight(), prebidSizes[1].height);
    }

    @Test
    public void convertGamAdSizeAndNullPassed_ReturnEmptyPrebidSizesArray() {
        AdSize[] prebidSizes = GamBannerEventHandler.convertGamAdSize(null);
        assertEquals(0, prebidSizes.length);
    }

    private void changeExpectingAppEventStatus(boolean status) {
        WhiteBox.setInternalState(mBannerEventHandler, "mIsExpectingAppEvent", status);
    }

    private boolean getExpectingAppEventStatus() {
        return WhiteBox.getInternalState(mBannerEventHandler, "mIsExpectingAppEvent");
    }
}