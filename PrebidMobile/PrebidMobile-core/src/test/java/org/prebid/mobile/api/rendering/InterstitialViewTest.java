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
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.core.R;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialViewListener;
import org.prebid.mobile.rendering.models.internal.InternalFriendlyObstruction;
import org.prebid.mobile.rendering.video.VideoCreativeView;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.AdViewManagerListener;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void viewReadyForImmediateDisplay_VideoCreative_EnableVideoPlayerClick() {
        VideoCreativeView mockVideoCreativeView = mock(VideoCreativeView.class);

        AdViewManagerListener adViewManagerListener = WhiteBox.getInternalState(
            spyBidInterstitialView,
            "onAdViewManagerListener"
        );
        adViewManagerListener.viewReadyForImmediateDisplay(mockVideoCreativeView);

        verify(mockVideoCreativeView).enableVideoPlayerClick();
    }

    @Test
    public void refreshControlInsets_DoesNotResetMarginsToDefault() throws AdException {
        // A plain (non-spy) instance is used here and below: addView() on a Mockito spy of a
        // real View does not actually attach the child under Robolectric, which made the
        // previous version of this test pass vacuously (findViewById returned null,
        // refreshControlInsets() silently no-op'd, and the untouched margins trivially matched).
        // Robolectric also reports zero window insets, so this only proves refreshControlInsets()
        // preserves the view's original margins instead of resetting them to a hardcoded default.
        InterstitialView view = new InterstitialView(Robolectric.buildActivity(Activity.class).create().get());
        View soundView = new View(view.getContext());
        soundView.setId(R.id.iv_sound_interstitial);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        params.topMargin = 7;
        params.rightMargin = 9;
        view.addView(soundView, params);

        view.refreshControlInsets();

        FrameLayout.LayoutParams result = (FrameLayout.LayoutParams) soundView.getLayoutParams();
        assertEquals(7, result.topMargin);
        assertEquals(9, result.rightMargin);
    }

    @Test
    public void refreshControlInsets_CalledRepeatedly_DoesNotAccumulateMargins() throws AdException {
        InterstitialView view = new InterstitialView(Robolectric.buildActivity(Activity.class).create().get());
        View soundView = new View(view.getContext());
        soundView.setId(R.id.iv_sound_interstitial);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        params.topMargin = 7;
        params.rightMargin = 9;
        view.addView(soundView, params);

        view.refreshControlInsets();
        view.refreshControlInsets();
        view.refreshControlInsets();

        FrameLayout.LayoutParams result = (FrameLayout.LayoutParams) soundView.getLayoutParams();
        assertEquals(7, result.topMargin);
        assertEquals(9, result.rightMargin);
    }

    @Test
    public void formInterstitialObstructionsArray_IncludesSoundButton() throws AdException {
        InterstitialView view = new InterstitialView(Robolectric.buildActivity(Activity.class).create().get());

        View closeView = new View(view.getContext());
        closeView.setId(R.id.iv_close_interstitial);
        view.addView(closeView, new FrameLayout.LayoutParams(100, 100));

        View skipView = new View(view.getContext());
        skipView.setId(R.id.iv_skip);
        view.addView(skipView, new FrameLayout.LayoutParams(100, 100));

        View soundView = new View(view.getContext());
        soundView.setId(R.id.iv_sound_interstitial);
        view.addView(soundView, new FrameLayout.LayoutParams(100, 100));

        InternalFriendlyObstruction[] obstructions = view.formInterstitialObstructionsArray();

        boolean containsSoundView = false;
        for (InternalFriendlyObstruction obstruction : obstructions) {
            if (obstruction != null && obstruction.getView() == soundView) {
                containsSoundView = true;
                break;
            }
        }
        assertTrue(containsSoundView);
    }

}
