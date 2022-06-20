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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialViewListener;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class InterstitialViewTest {

    private InterstitialView spyBidInterstitialView;
    @Mock private AdViewManager mockAdViewManager;

    @Before
    public void setup() throws AdException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);

        Context context = Robolectric.buildActivity(Activity.class).create().get();

        spyBidInterstitialView = spy(new InterstitialView(context));

        when(mockAdViewManager.getAdConfiguration()).thenReturn(mock(AdUnitConfiguration.class));
        WhiteBox.field(InterstitialView.class, "adViewManager").set(spyBidInterstitialView, mockAdViewManager);
    }

    @Test
    public void loadAd_ExecuteBidTransactionLoad() {
        AdUnitConfiguration mockAdUnitConfiguration = mock(AdUnitConfiguration.class);
        BidResponse mockBidResponse = mock(BidResponse.class);

        spyBidInterstitialView.loadAd(mockAdUnitConfiguration, mockBidResponse);

        verify(mockAdViewManager, times(1)).loadBidTransaction(eq(mockAdUnitConfiguration), eq(mockBidResponse));
    }

    @Test
    public void setInterstitialViewListener_ExecuteAddEventListener() {
        final InterstitialViewListener mockInterstitialViewListener = mock(InterstitialViewListener.class);

        spyBidInterstitialView.setInterstitialViewListener(mockInterstitialViewListener);

        verify(spyBidInterstitialView, times(1)).setInterstitialViewListener(eq(mockInterstitialViewListener));
    }

}