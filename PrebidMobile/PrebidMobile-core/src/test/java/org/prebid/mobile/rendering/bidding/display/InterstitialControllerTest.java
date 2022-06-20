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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.InterstitialView;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialViewListener;
import org.prebid.mobile.rendering.models.AdDetails;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class InterstitialControllerTest {
    private InterstitialController interstitialController;

    @Mock private InterstitialControllerListener mockListener;
    @Mock private InterstitialView mockInterstitialView;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        interstitialController = new InterstitialController(Robolectric.buildActivity(Activity.class).create().get(),
                mockListener
        );
        WhiteBox.setInternalState(interstitialController, "bidInterstitialView", mockInterstitialView);
    }

    @Test
    public void loadAd_ExecuteInterstitialViewLoadAd() {
        final AdUnitConfiguration mockAdUnitConfiguration = mock(AdUnitConfiguration.class);
        final BidResponse mockBidResponse = mock(BidResponse.class);

        interstitialController.loadAd(mockAdUnitConfiguration, mockBidResponse);

        verify(mockInterstitialView, times(1)).loadAd(eq(mockAdUnitConfiguration), eq(mockBidResponse));
    }

    @Test
    public void showInterstitialType_ExecuteInterstitialViewShowInterstitialFromRoot() {
        WhiteBox.setInternalState(interstitialController, "adUnitIdentifierType", AdFormat.INTERSTITIAL);
        interstitialController.show();

        verify(mockInterstitialView, times(1)).showAsInterstitialFromRoot();
    }

    @Test
    public void showVastType_ExecuteInterstitialViewShowVideoAsInterstitial() {
        WhiteBox.setInternalState(interstitialController, "adUnitIdentifierType", AdFormat.VAST);
        interstitialController.show();

        verify(mockInterstitialView, times(1)).showVideoAsInterstitial();
    }

    @Test
    public void showNullType_DoNothing() {
        reset(mockInterstitialView);

        interstitialController.show();

        verifyNoMoreInteractions(mockInterstitialView);
    }

    @Test
    public void destroy_ExecuteInterstitialViewDestroy() {
        interstitialController.destroy();

        verify(mockInterstitialView, times(1)).destroy();
    }

    //region ===================== InterstitialViewListener tests
    @Test
    public void adDidLoad_NotifyInterstitialReadyForDisplay() {
        getInterstitialViewListener().onAdLoaded(mock(InterstitialView.class), mock(AdDetails.class));

        verify(mockListener, times(1)).onInterstitialReadyForDisplay();
    }

    @Test
    public void adDidFailLoad_NotifyInterstitialFailedToLoad() {
        getInterstitialViewListener().onAdFailed(mock(InterstitialView.class), mock(AdException.class));

        verify(mockListener, times(1)).onInterstitialFailedToLoad(any());
    }

    @Test
    public void adDidDisplay_NotifyInterstitialDisplayed() {
        getInterstitialViewListener().onAdDisplayed(mock(InterstitialView.class));

        verify(mockListener, times(1)).onInterstitialDisplayed();
    }

    @Test
    public void adWasClicked_NotifyInterstitialClicked() {
        getInterstitialViewListener().onAdClicked(mock(InterstitialView.class));

        verify(mockListener, times(1)).onInterstitialClicked();
    }

    @Test
    public void adInterstitialDidClose_NotifyInterstitialDidClose() {
        getInterstitialViewListener().onAdClosed(mock(InterstitialView.class));

        verify(mockListener, times(1)).onInterstitialClosed();
    }
    //endregion ===================== InterstitialViewListener tests

    private InterstitialViewListener getInterstitialViewListener() {
        return (InterstitialViewListener) WhiteBox.getInternalState(interstitialController, "interstitialViewListener");
    }
}