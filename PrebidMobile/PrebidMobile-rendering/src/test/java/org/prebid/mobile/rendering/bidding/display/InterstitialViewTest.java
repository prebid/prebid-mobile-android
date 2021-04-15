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

package org.prebid.mobile.rendering.bidding.display;

import android.app.Activity;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialViewListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class InterstitialViewTest {
    private InterstitialView mSpyBidInterstitialView;
    @Mock
    private AdViewManager mMockAdViewManager;

    @Before
    public void setup() throws AdException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);

        Context context = Robolectric.buildActivity(Activity.class).create().get();

        mSpyBidInterstitialView = spy(new InterstitialView(context));

        when(mMockAdViewManager.getAdConfiguration()).thenReturn(mock(AdConfiguration.class));
        WhiteBox.field(InterstitialView.class, "mAdViewManager").set(mSpyBidInterstitialView, mMockAdViewManager);
    }

    @Test
    public void loadAd_ExecuteBidTransactionLoad() {
        AdConfiguration mockAdUnitConfiguration = mock(AdConfiguration.class);
        BidResponse mockBidResponse = mock(BidResponse.class);

        mSpyBidInterstitialView.loadAd(mockAdUnitConfiguration, mockBidResponse);

        verify(mMockAdViewManager, times(1)).loadBidTransaction(eq(mockAdUnitConfiguration), eq(mockBidResponse));
    }

    @Test
    public void setInterstitialViewListener_ExecuteAddEventListener() {
        final InterstitialViewListener mockInterstitialViewListener = mock(InterstitialViewListener.class);

        mSpyBidInterstitialView.setInterstitialViewListener(mockInterstitialViewListener);

        verify(mSpyBidInterstitialView, times(1)).setInterstitialViewListener(eq(mockInterstitialViewListener));
    }
}