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
import android.view.View;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdDetails;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.AdViewManagerListener;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class DisplayViewTest {

    private DisplayView mDisplayView;
    private Context mContext;
    private AdConfiguration mAdUnitConfiguration;
    @Mock
    private BidResponse mBidResponse;
    @Mock
    private DisplayViewListener mMockDisplayViewListener;
    @Mock
    private AdViewManager mMockAdViewManager;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(DisplayViewTest.this);

        mContext = Robolectric.buildActivity(Activity.class).create().get();

        mAdUnitConfiguration = new AdConfiguration();
        mAdUnitConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);

        BidResponse mockResponse = mock(BidResponse.class);
        Bid mockBid = mock(Bid.class);
        when(mockBid.getAdm()).thenReturn("adm");
        when(mockResponse.getWinningBid()).thenReturn(mockBid);

        mDisplayView = new DisplayView(mContext, mMockDisplayViewListener, mAdUnitConfiguration, mockResponse);
        reset(mMockDisplayViewListener);
    }

    @Test
    public void whenDisplayAd_LoadBidTransaction() {
        Assert.assertNotNull(WhiteBox.getInternalState(mDisplayView, "mAdViewManager"));
    }

    @Test
    public void whenAdViewManagerListenerAdLoaded_NotifyListenerOnAdLoaded()
    throws IllegalAccessException {
        AdViewManagerListener adViewManagerListener = getAdViewManagerListener();
        adViewManagerListener.adLoaded(mock(AdDetails.class));
        verify(mMockDisplayViewListener).onAdLoaded();
    }

    @Test
    public void whenAdViewManagerListenerViewReadyForImmediateDisplay_NotifyListenerOnAdDisplayed()
    throws IllegalAccessException {
        AdViewManagerListener adViewManagerListener = getAdViewManagerListener();
        adViewManagerListener.viewReadyForImmediateDisplay(mock(View.class));
        verify(mMockDisplayViewListener).onAdDisplayed();
    }

    @Test
    public void whenAdViewManagerListenerFailedToLoad_NotifyListenerOnAdFailed()
    throws IllegalAccessException {
        AdViewManagerListener adViewManagerListener = getAdViewManagerListener();
        adViewManagerListener.failedToLoad(new AdException(AdException.INTERNAL_ERROR, "Test"));
        verify(mMockDisplayViewListener).onAdFailed(any(AdException.class));
    }

    @Test
    public void whenAdViewManagerListenerCreativeWasClicked_NotifyListenerOnAdClicked()
    throws IllegalAccessException {
        AdViewManagerListener adViewManagerListener = getAdViewManagerListener();
        adViewManagerListener.creativeClicked("");
        verify(mMockDisplayViewListener).onAdClicked();
    }

    @Test
    public void whenAdViewManagerListenerCreativeInterstitialDidClose_NotifyListenerOnAdClosed()
    throws IllegalAccessException {
        AdViewManagerListener adViewManagerListener = getAdViewManagerListener();
        adViewManagerListener.creativeInterstitialClosed();
        verify(mMockDisplayViewListener).onAdClosed();
    }

    @Test
    public void whenAdViewManagerListenerCreativeDidCollapse_NotifyListenerOnAdClosed()
    throws IllegalAccessException {
        AdViewManagerListener adViewManagerListener = getAdViewManagerListener();
        adViewManagerListener.creativeCollapsed();
        verify(mMockDisplayViewListener).onAdClosed();
    }

    private AdViewManagerListener getAdViewManagerListener() throws IllegalAccessException {
        return (AdViewManagerListener) WhiteBox.field(DisplayView.class, "mAdViewManagerListener").get(mDisplayView);
    }
}