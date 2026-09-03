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

package org.prebid.mobile.rendering.views.interstitial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.api.data.Position;
import org.prebid.mobile.api.rendering.InterstitialView;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/**
 * Covers the sound-vs-close/skip control overlap: {@code lyt_sound} and {@code lyt_close}/
 * {@code lyt_skip} both default to the TOP_RIGHT corner of the ad container, so the sound
 * control must be nudged out of the way whenever it would otherwise land on top of (or
 * underneath) the close/skip control.
 */
@RunWith(RobolectricTestRunner.class)
public class InterstitialVideoSoundViewTest {

    private InterstitialVideo createInterstitialVideo(InterstitialDisplayPropertiesInternal properties) {
        Context context = Robolectric.buildActivity(Activity.class).get();
        InterstitialView mockAdView = mock(InterstitialView.class);
        InterstitialManager mockInterstitialManager = mock(InterstitialManager.class);
        AdUnitConfiguration mockAdConfiguration = mock(AdUnitConfiguration.class);
        when(mockInterstitialManager.getInterstitialDisplayProperties()).thenReturn(properties);

        return new InterstitialVideo(context, mockAdView, mockInterstitialManager, mockAdConfiguration);
    }

    @Test
    public void createSoundView_CloseButtonAtDefaultTopRight_OffsetsSoundAwayFromCloseButton() {
        InterstitialDisplayPropertiesInternal properties = new InterstitialDisplayPropertiesInternal();
        InterstitialVideo interstitialVideo = createInterstitialVideo(properties);

        View soundView = interstitialVideo.createSoundView(Robolectric.buildActivity(Activity.class).get());

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) soundView.getLayoutParams();
        assertTrue(
            "Sound button shares the default TOP_RIGHT corner with the close button, "
                + "so it must be pushed clear of it",
            params.rightMargin > 0
        );
    }

    @Test
    public void createSoundView_CloseButtonAtTopLeft_DoesNotOffsetSoundButton() {
        InterstitialDisplayPropertiesInternal properties = new InterstitialDisplayPropertiesInternal();
        properties.closeButtonPosition = Position.TOP_LEFT;
        InterstitialVideo interstitialVideo = createInterstitialVideo(properties);

        View soundView = interstitialVideo.createSoundView(Robolectric.buildActivity(Activity.class).get());

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) soundView.getLayoutParams();
        assertEquals(
            "Close button sits in the opposite (TOP_LEFT) corner, so no offset is needed",
            0,
            params.rightMargin
        );
    }

    @Test
    public void createSoundView_EndCardWithSkipButtonAtDefaultTopRight_OffsetsSoundAwayFromSkipButton() {
        InterstitialDisplayPropertiesInternal properties = new InterstitialDisplayPropertiesInternal();
        InterstitialVideo interstitialVideo = createInterstitialVideo(properties);
        WhiteBox.setInternalState(interstitialVideo, "useSkipButton", true);

        View soundView = interstitialVideo.createSoundView(Robolectric.buildActivity(Activity.class).get());

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) soundView.getLayoutParams();
        assertTrue(
            "Sound button shares the default TOP_RIGHT corner with the skip button, "
                + "so it must be pushed clear of it",
            params.rightMargin > 0
        );
    }
}
