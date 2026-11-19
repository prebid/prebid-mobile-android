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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.os.CountDownTimer;
import android.widget.FrameLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/** A rewarded VAST without ClickThrough has no "Learn more" view; without a companion it has no end card either. */
@RunWith(RobolectricTestRunner.class)
public class InterstitialVideoRewardedCountdownTest {

    private static final long DURATION_MS = 5_000;

    private FrameLayout adViewContainer;
    private InterstitialVideo interstitialVideo;

    @Before
    public void setUp() {
        Activity activity = Robolectric.buildActivity(Activity.class).get();
        adViewContainer = new FrameLayout(activity);

        InterstitialManager interstitialManager = mock(InterstitialManager.class);
        when(interstitialManager.getInterstitialDisplayProperties()).thenReturn(new InterstitialDisplayPropertiesInternal());
        AdUnitConfiguration config = mock(AdUnitConfiguration.class);
        when(config.isRewarded()).thenReturn(true);

        interstitialVideo = new InterstitialVideo(activity, adViewContainer, interstitialManager, config);
    }

    @After
    public void tearDown() {
        interstitialVideo.pauseVideo();
    }

    @Test
    public void countdownFinishes_WithoutLearnMoreAndEndCard_RemovesCountdown() {
        interstitialVideo.showDurationTimer(DURATION_MS);
        assertEquals(1, adViewContainer.getChildCount());

        CountDownTimer timer = WhiteBox.getInternalState(interstitialVideo, "countDownTimer");
        shadowOf(timer).invokeFinish();

        assertEquals(0, adViewContainer.getChildCount());
    }

    @Test
    public void pauseVideo_WithoutLearnMoreAndEndCard_DoesNotThrow() {
        interstitialVideo.showDurationTimer(DURATION_MS);

        interstitialVideo.pauseVideo();
    }
}
