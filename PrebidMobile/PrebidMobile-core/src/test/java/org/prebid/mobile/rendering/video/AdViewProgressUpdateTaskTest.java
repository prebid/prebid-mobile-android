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

package org.prebid.mobile.rendering.video;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardManager;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedClosingRules;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedCompletionRules;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedExt;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdViewProgressUpdateTaskTest {

    private AdViewProgressUpdateTask adViewProgressTask;
    private VideoCreative mockVideoCreative;
    private View mockCreativeView;
    private final int duration = 50;

    @Before
    public void setup() throws AdException {
        mockVideoCreative = mock(VideoCreative.class);
        mockCreativeView = mock(VideoCreativeView.class);

        when(mockVideoCreative.getCreativeView()).thenReturn(mockCreativeView);

        adViewProgressTask = spy(new AdViewProgressUpdateTask(mockVideoCreative, duration, new AdUnitConfiguration()));
    }

    @Test
    public void onPostExecuteTest() {
        adViewProgressTask.onPostExecute(null);
        verify(adViewProgressTask).cancel(true);
    }

    @Test
    public void onProgressUpdateTest() {
        assertFalse(adViewProgressTask.getFirstQuartile());
        adViewProgressTask.onProgressUpdate((long) 25);
        verify(mockVideoCreative).onEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
        assertTrue(adViewProgressTask.getFirstQuartile());

        reset(mockVideoCreative);
        assertFalse(adViewProgressTask.getMidpoint());
        adViewProgressTask.onProgressUpdate((long) 50);
        verify(mockVideoCreative).onEvent(VideoAdEvent.Event.AD_MIDPOINT);
        assertTrue(adViewProgressTask.getMidpoint());

        reset(mockVideoCreative);
        assertFalse(adViewProgressTask.getThirdQuartile());
        adViewProgressTask.onProgressUpdate((long) 75);
        verify(mockVideoCreative).onEvent(VideoAdEvent.Event.AD_THIRDQUARTILE);
        assertTrue(adViewProgressTask.getThirdQuartile());
    }

    @Test
    public void getCurrentPositionTest() {
        assertEquals(0, adViewProgressTask.getCurrentPosition());
    }

    @Test
    public void setVastVideoDurationTest() throws IllegalAccessException, NoSuchFieldException {
        Field vastDurationField = WhiteBox.field(AdViewProgressUpdateTask.class, "vastTagDuration");
        assertEquals((long) -1, vastDurationField.get(adViewProgressTask));

        adViewProgressTask.setVastTagDuration(10);
        assertEquals((long) 10, vastDurationField.get(adViewProgressTask));
    }

    @Test
    public void whenNoQuartilePassed_NoEventsTracked() {
        assertFalse(adViewProgressTask.getFirstQuartile());
        assertFalse(adViewProgressTask.getMidpoint());
        assertFalse(adViewProgressTask.getThirdQuartile());
        adViewProgressTask.onProgressUpdate((long) 24);

        assertFalse(adViewProgressTask.getFirstQuartile());
        assertFalse(adViewProgressTask.getMidpoint());
        assertFalse(adViewProgressTask.getThirdQuartile());
        verify(mockVideoCreative, never()).onEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
        verify(mockVideoCreative, never()).onEvent(VideoAdEvent.Event.AD_MIDPOINT);
        verify(mockVideoCreative, never()).onEvent(VideoAdEvent.Event.AD_THIRDQUARTILE);
    }

    @Test
    public void whenFirstQuartilePassedAndNoEventsTracked_TrackOnlyFirstQuartile() {
        assertFalse(adViewProgressTask.getFirstQuartile());
        assertFalse(adViewProgressTask.getMidpoint());
        assertFalse(adViewProgressTask.getThirdQuartile());

        adViewProgressTask.onProgressUpdate((long) 49);

        assertTrue(adViewProgressTask.getFirstQuartile());
        assertFalse(adViewProgressTask.getMidpoint());
        assertFalse(adViewProgressTask.getThirdQuartile());
        verify(mockVideoCreative).onEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
        verify(mockVideoCreative, never()).onEvent(VideoAdEvent.Event.AD_MIDPOINT);
        verify(mockVideoCreative, never()).onEvent(VideoAdEvent.Event.AD_THIRDQUARTILE);
    }

    @Test
    public void whenMidpointPassedAndNoEventsTracked_TrackFirstQuartileAndMidpoint() {
        assertFalse(adViewProgressTask.getFirstQuartile());
        assertFalse(adViewProgressTask.getMidpoint());
        assertFalse(adViewProgressTask.getThirdQuartile());

        adViewProgressTask.onProgressUpdate((long) 74);

        assertTrue(adViewProgressTask.getFirstQuartile());
        assertTrue(adViewProgressTask.getMidpoint());
        assertFalse(adViewProgressTask.getThirdQuartile());
        verify(mockVideoCreative).onEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
        verify(mockVideoCreative).onEvent(VideoAdEvent.Event.AD_MIDPOINT);
        verify(mockVideoCreative, never()).onEvent(VideoAdEvent.Event.AD_THIRDQUARTILE);
    }

    @Test
    public void whenThirdQuartilePassedAndNoEventsTracked_TrackAllEvents() {
        assertFalse(adViewProgressTask.getFirstQuartile());
        assertFalse(adViewProgressTask.getMidpoint());
        assertFalse(adViewProgressTask.getThirdQuartile());

        adViewProgressTask.onProgressUpdate((long) 99);

        assertTrue(adViewProgressTask.getFirstQuartile());
        assertTrue(adViewProgressTask.getMidpoint());
        assertTrue(adViewProgressTask.getThirdQuartile());
        verify(mockVideoCreative).onEvent(VideoAdEvent.Event.AD_FIRSTQUARTILE);
        verify(mockVideoCreative).onEvent(VideoAdEvent.Event.AD_MIDPOINT);
        verify(mockVideoCreative).onEvent(VideoAdEvent.Event.AD_THIRDQUARTILE);
    }


    @Test
    public void getVideoLengthPercentageForReward_notRewarded() {
        int videoDuration = 10_000;
        AdUnitConfiguration config = new AdUnitConfiguration();
        config.setRewarded(false);

        Integer result = AdViewProgressUpdateTask.getVideoLengthPercentageForReward(videoDuration, config);

        assertNull(result);
    }

    @Test
    public void getVideoLengthPercentageForReward_withEndCard() {
        int videoDuration = 10_000;
        AdUnitConfiguration config = new AdUnitConfiguration();
        config.setRewarded(true);
        config.setHasEndCard(true);

        Integer result = AdViewProgressUpdateTask.getVideoLengthPercentageForReward(videoDuration, config);

        assertNull(result);
    }

    @Test
    public void getVideoLengthPercentageForReward_1() {
        testReward(10_000, null, RewardedCompletionRules.PlaybackEvent.START, 1);
    }

    @Test
    public void getVideoLengthPercentageForReward_2() {
        testReward(10_000, null, RewardedCompletionRules.PlaybackEvent.FIRST_QUARTILE, 25);
    }

    @Test
    public void getVideoLengthPercentageForReward_3() {
        testReward(10_000, null, RewardedCompletionRules.PlaybackEvent.MIDPOINT, 50);
    }

    @Test
    public void getVideoLengthPercentageForReward_4() {
        testReward(10_000, null, RewardedCompletionRules.PlaybackEvent.THIRD_QUARTILE, 75);
    }

    @Test
    public void getVideoLengthPercentageForReward_5() {
        testReward(10_000, null, RewardedCompletionRules.PlaybackEvent.COMPLETE, 100);
    }

    @Test
    public void getVideoLengthPercentageForReward_time() {
        testReward(10_000, 7, null, 70);
    }

    @Test
    public void getVideoLengthPercentageForReward_wrongTime_1() {
        testReward(10_000, 25, null, 100);
    }

    @Test
    public void getVideoLengthPercentageForReward_wrongTime_2() {
        testReward(10_000, -1, null, 100);
    }

    private void testReward(int videoDuration, Integer videoTime, RewardedCompletionRules.PlaybackEvent event, Integer expected) {
        AdUnitConfiguration config = new AdUnitConfiguration();
        config.setRewarded(true);
        config.setHasEndCard(false);

        RewardedExt rewardedExt = new RewardedExt(null, new RewardedCompletionRules(null, videoTime, null, null, event, null), new RewardedClosingRules());
        RewardManager rewardManager = new RewardManager();
        rewardManager.setRewardedExt(rewardedExt);
        config.setRewardManager(rewardManager);

        Integer result = AdViewProgressUpdateTask.getVideoLengthPercentageForReward(videoDuration, config);

        assertEquals(expected, result);
    }

}