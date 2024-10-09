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

import android.content.Context;
import android.os.Handler;
import android.widget.FrameLayout;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.rendering.InterstitialView;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardManager;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedClosingRules;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedCompletionRules;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardedExt;
import org.prebid.mobile.rendering.models.AbstractCreative;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.prebid.mobile.rendering.video.VideoCreativeModel;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.AdViewManagerListener;
import org.prebid.mobile.test.utils.WhiteBox;

import java.util.Timer;
import java.util.TimerTask;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InterstitialVideoTest {
    @Mock private InterstitialView mockAdView;
    @Mock private Handler mockHandler;
    @Mock private InterstitialManager mockInterstitialManager;
    @Mock private AdUnitConfiguration mockAdConfiguration;

    private InterstitialVideo spyInterstitialVideo;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mockInterstitialManager.getInterstitialDisplayProperties()).thenReturn(mock(
                InterstitialDisplayPropertiesInternal.class));
        when(mockAdView.getMediaOffset()).thenReturn(-1L);

        spyInterstitialVideo = Mockito.spy(new InterstitialVideo(null,
                mockAdView,
                mockInterstitialManager,
                mockAdConfiguration
        ));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(mockHandler).post(any(Runnable.class));
        WhiteBox.setInternalState(spyInterstitialVideo, "handler", mockHandler);

        mockMediaDuration(30 * 1000L);

        // ignore, since involves android SDK classes (views). View display is tested in UI tests.
        doNothing().when(spyInterstitialVideo).showDurationTimer(anyLong());
    }

    @Test
    public void scheduleShowCloseBtnTask_WithDefinedOffset_TimerIsScheduledWithOffsetValue() {
        mockOffset(7000L);
        mockMediaDuration(30 * 1000L);
        spyInterstitialVideo.setShowButtonOnComplete(true);

        spyInterstitialVideo.scheduleShowCloseBtnTask(mockAdView);

        assertTrue(spyInterstitialVideo.shouldShowCloseButtonOnComplete());
        verify(spyInterstitialVideo, times(1)).scheduleAllTimers(eq(7000L));
    }

    @Test
    public void scheduleShowCloseBtnTask_ForShortVideo_NoTimerScheduled() {
        // Short video
        mockMediaDuration(2L * 1000);
        spyInterstitialVideo.setShowButtonOnComplete(false);

        spyInterstitialVideo.scheduleShowCloseBtnTask(mockAdView);

        assertTrue(spyInterstitialVideo.shouldShowCloseButtonOnComplete());
        verify(spyInterstitialVideo, never()).scheduleAllTimers(anyLong());
    }

    @Test
    public void scheduleShowCloseBtnTask_ForZeroDuration_NoTimerScheduledAndShowCloseButtonOnCompleteTrue() {
        // Short video
        mockMediaDuration(0);
        spyInterstitialVideo.setShowButtonOnComplete(false);

        spyInterstitialVideo.scheduleShowCloseBtnTask(mockAdView);

        assertTrue(spyInterstitialVideo.shouldShowCloseButtonOnComplete());
        verify(spyInterstitialVideo, never()).scheduleAllTimers(anyLong());
    }

    @Test
    public void whenGetMediaOffsetValue_ShowCloseButtonAfterPeriod() {
        mockOffset(7000L);

        spyInterstitialVideo.scheduleShowCloseBtnTask(mockAdView);

        verify(spyInterstitialVideo).scheduleAllTimers(7000L);
    }

    @Test
    public void videoPausedTest() {
        spyInterstitialVideo.pauseVideo();
        assertTrue(spyInterstitialVideo.isVideoPaused());
        spyInterstitialVideo.resumeVideo();
        assertFalse(spyInterstitialVideo.isVideoPaused());
    }

    @Test
    public void scheduleShowCloseBtnAfterPauseTest() throws IllegalAccessException {
        Timer mockTimer = mock(Timer.class);
        TimerTask mockTimerTask = mock(TimerTask.class);
        WhiteBox.field(InterstitialVideo.class, "showCloseButtonTask").set(spyInterstitialVideo, mockTimerTask);
        WhiteBox.field(InterstitialVideo.class, "timer").set(spyInterstitialVideo, mockTimer);

        spyInterstitialVideo.pauseVideo();

        verify(mockTimer, times(1)).cancel();
        verify(mockTimer, times(1)).purge();
        verify(mockTimerTask, times(1)).cancel();
        verify(spyInterstitialVideo, never()).scheduleAllTimers(anyLong());
    }

    @Test
    public void scheduleShowCloseBtnAfterResumeTest() {
        spyInterstitialVideo.setRemainingTimeInMs(5000);
        spyInterstitialVideo.setShowButtonOnComplete(false);

        spyInterstitialVideo.resumeVideo();

        assertFalse(spyInterstitialVideo.shouldShowCloseButtonOnComplete());
        verify(spyInterstitialVideo, times(1)).scheduleAllTimers(5 * 1000L);
    }

    @Test
    public void queueUIThreadTaskTest() {
        Runnable mockRunnable = mock(Runnable.class);
        spyInterstitialVideo.queueUIThreadTask(mockRunnable);
        verify(mockHandler).post(eq(mockRunnable));
    }

    @Test
    public void closeTest() {
        spyInterstitialVideo.close();
        verify(mockInterstitialManager).interstitialAdClosed();
    }

    @Test
    public void removeViewsTest() throws IllegalAccessException {
        FrameLayout mockContainer = mock(FrameLayout.class);
        WhiteBox.field(InterstitialVideo.class, "adViewContainer").set(spyInterstitialVideo, mockContainer);

        spyInterstitialVideo.removeViews();
        verify(mockContainer).removeAllViews();
    }

    @Test
    public void handleCloseClickTest() {
        spyInterstitialVideo.handleCloseClick();

        verify(spyInterstitialVideo, atLeastOnce()).close();
    }

    @Test
    public void whenAllOffsetsPresent_UseSscOffset() throws Exception {
        Context context = mock(Context.class);
        AdViewManager adViewManager = new AdViewManager(context,
                mock(AdViewManagerListener.class),
                mockAdView,
                mockInterstitialManager
        );

        AdUnitConfiguration adConfiguration = adViewManager.getAdConfiguration();
        adConfiguration.setVideoSkipOffset(10000);

        AbstractCreative mockCreative = mock(AbstractCreative.class);
        VideoCreativeModel mockModel = mock(VideoCreativeModel.class);
        when(mockModel.getSkipOffset()).thenReturn(20000L);
        when(mockCreative.getCreativeModel()).thenReturn(mockModel);

        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, mockCreative);
        when(mockAdView.getMediaOffset()).thenReturn(adViewManager.getSkipOffset());

        spyInterstitialVideo.scheduleShowCloseBtnTask(mockAdView);
        verify(spyInterstitialVideo).scheduleAllTimers(10L * 1000);
    }

    @Test
    public void whenVastAndSscOffsetPresent_UseSscOffset() throws Exception {
        Context context = mock(Context.class);
        AdViewManager adViewManager = new AdViewManager(context,
                mock(AdViewManagerListener.class),
                mockAdView,
                mockInterstitialManager
        );

        AdUnitConfiguration adConfiguration = adViewManager.getAdConfiguration();
        adConfiguration.setVideoSkipOffset(10000);

        AbstractCreative mockCreative = mock(AbstractCreative.class);
        VideoCreativeModel mockModel = mock(VideoCreativeModel.class);
        when(mockModel.getSkipOffset()).thenReturn(20000L);
        when(mockCreative.getCreativeModel()).thenReturn(mockModel);

        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, mockCreative);
        when(mockAdView.getMediaOffset()).thenReturn(adViewManager.getSkipOffset());

        spyInterstitialVideo.scheduleShowCloseBtnTask(mockAdView);
        verify(spyInterstitialVideo).scheduleAllTimers(10L * 1000);
    }

    @Test
    public void whenRemainingTimePresent_UseRemainingTime() throws Exception {
        Context context = mock(Context.class);
        AdViewManager adViewManager = new AdViewManager(
                context,
                mock(AdViewManagerListener.class),
                mockAdView,
                mock(InterstitialManager.class)
        );

        AdUnitConfiguration adConfiguration = adViewManager.getAdConfiguration();
        adConfiguration.setVideoSkipOffset(10);

        AbstractCreative mockCreative = mock(AbstractCreative.class);
        VideoCreativeModel mockModel = mock(VideoCreativeModel.class);
        when(mockModel.getSkipOffset()).thenReturn(20L);
        when(mockCreative.getCreativeModel()).thenReturn(mockModel);

        WhiteBox.field(AdViewManager.class, "currentCreative").set(adViewManager, mockCreative);
        when(mockAdView.getMediaOffset()).thenReturn(adViewManager.getSkipOffset());

        spyInterstitialVideo.setRemainingTimeInMs(3000);

        spyInterstitialVideo.scheduleShowCloseBtnTask(mockAdView, 3000);
        verify(spyInterstitialVideo).scheduleAllTimers(3L * 1000);
    }

    @Test
    public void whenNoOffsetPresent_UseDefaultOffset() {
        spyInterstitialVideo.scheduleShowCloseBtnTask(mockAdView);
        verify(spyInterstitialVideo).scheduleAllTimers(10L * 1000);
    }

    private void mockMediaDuration(long duration) {
        when(mockAdView.getMediaDuration()).thenReturn(duration);
    }

    private void mockOffset(long value) {
        when(mockAdView.getMediaOffset()).thenReturn(value);
    }

    @Test
    public void scheduleShowCloseBtnTask_TestDefaultUseSkipButton() {
        spyInterstitialVideo.scheduleShowButtonTask();

        assertFalse(getUseSkipButton());
    }

    @Test
    public void scheduleShowCloseBtnTask_TestFalseUseSkipButton() {
        spyInterstitialVideo.setHasEndCard(false);

        spyInterstitialVideo.scheduleShowButtonTask();

        assertFalse(getUseSkipButton());
    }

    @Test
    public void scheduleShowCloseBtnTask_TestTrueUseSkipButton() {
        spyInterstitialVideo.setHasEndCard(true);

        spyInterstitialVideo.scheduleShowButtonTask();

        assertTrue(getUseSkipButton());
    }

    private boolean getUseSkipButton() {
        return (boolean) Reflection.getFieldOf(spyInterstitialVideo, "useSkipButton");
    }


    @Test
    public void scheduleShowCloseBtnTask_VideoDurationLessThanSkipDelay_CallScheduleTimeWithVideoLength() {
        int skipDelay = 10_000;
        long videoDuration = 5_000;
        when(spyInterstitialVideo.getDuration(any())).thenReturn(videoDuration);
        when(spyInterstitialVideo.getSkipDelayMs()).thenReturn(skipDelay);

        spyInterstitialVideo.scheduleShowButtonTask();

        verify(spyInterstitialVideo).scheduleAllTimers(videoDuration);
    }

    @Test
    public void scheduleShowCloseBtnTask_VideoDurationBiggerThanSkipDelay_CallScheduleTimeWithSkipDelayLength() {
        int skipDelay = 5_000;
        long videoDuration = 10_000;
        when(spyInterstitialVideo.getDuration(any())).thenReturn(videoDuration);
        when(spyInterstitialVideo.getSkipDelayMs()).thenReturn(skipDelay);

        spyInterstitialVideo.scheduleShowButtonTask();

        verify(spyInterstitialVideo).scheduleAllTimers(skipDelay);
    }


    @Test
    public void rewarded_getTimeToReward_default() {
        RewardedExt rewardedExt = RewardedExt.defaultExt();
        AdUnitConfiguration mockConfig = mockAdConfiguration;

        RewardManager mockRewardManager = mock(RewardManager.class);
        when(mockConfig.getRewardManager()).thenReturn(mockRewardManager);
        when(mockRewardManager.getRewardedExt()).thenReturn(rewardedExt);

        Integer timeToReward = InterstitialVideo.getTimeToReward(10_000, mockConfig);

        assertNull(timeToReward);
    }

    @Test
    public void rewarded_getTimeToReward_time() {
        RewardedExt rewardedExt = mock(RewardedExt.class);
        AdUnitConfiguration mockConfig = mockAdConfiguration;

        RewardManager mockRewardManager = mock(RewardManager.class);
        when(mockConfig.getRewardManager()).thenReturn(mockRewardManager);
        when(mockRewardManager.getRewardedExt()).thenReturn(rewardedExt);

        RewardedCompletionRules mockRules = mock(RewardedCompletionRules.class);
        when(mockRules.getVideoEvent()).thenReturn(null);
        when(mockRules.getVideoTime()).thenReturn(6);
        when(rewardedExt.getCompletionRules()).thenReturn(mockRules);

        Integer timeToReward = InterstitialVideo.getTimeToReward(10_000, mockConfig);

        assertEquals(Integer.valueOf(6_000), timeToReward);
    }

    @Test
    public void rewarded_getTimeToReward_completionEvent_1() {
        testCompletionEvent(RewardedCompletionRules.PlaybackEvent.COMPLETE, 10_000);
    }

    @Test
    public void rewarded_getTimeToReward_completionEvent_2() {
        testCompletionEvent(RewardedCompletionRules.PlaybackEvent.THIRD_QUARTILE, 7_500);
    }

    @Test
    public void rewarded_getTimeToReward_completionEvent_3() {
        testCompletionEvent(RewardedCompletionRules.PlaybackEvent.MIDPOINT, 5_000);
    }

    @Test
    public void rewarded_getTimeToReward_completionEvent_4() {
        testCompletionEvent(RewardedCompletionRules.PlaybackEvent.FIRST_QUARTILE, 2_500);
    }

    @Test
    public void rewarded_getTimeToReward_completionEvent_5() {
        testCompletionEvent(RewardedCompletionRules.PlaybackEvent.START, 0);
    }

    private void testCompletionEvent(RewardedCompletionRules.PlaybackEvent event, int expected) {
        RewardedExt rewardedExt = mock(RewardedExt.class);
        AdUnitConfiguration mockConfig = mockAdConfiguration;
        RewardManager mockRewardManager = mock(RewardManager.class);
        when(mockConfig.getRewardManager()).thenReturn(mockRewardManager);
        when(mockRewardManager.getRewardedExt()).thenReturn(rewardedExt);
        when(rewardedExt.getCompletionRules()).thenReturn(new RewardedCompletionRules(11, 12, 13, "1", event, "2"));

        Integer timeToReward = InterstitialVideo.getTimeToReward(10_000, mockConfig);

        assertEquals(Integer.valueOf(expected), timeToReward);
    }


    @Test
    public void rewarded_getDelayToShowCloseButton_1() {
        int videoDuration = 10_000;
        int rewardTimeSeconds = 5;
        int postRewardTimeSeconds = 0;
        int expected = 5_000;
        testCloseButtonDelay(videoDuration, rewardTimeSeconds, postRewardTimeSeconds, expected);
    }

    @Test
    public void rewarded_getDelayToShowCloseButton_2() {
        int videoDuration = 10_000;
        int rewardTimeSeconds = 15;
        int postRewardTimeSeconds = 0;
        int expected = 10_000;
        testCloseButtonDelay(videoDuration, rewardTimeSeconds, postRewardTimeSeconds, expected);
    }

    @Test
    public void rewarded_getDelayToShowCloseButton_3() {
        int videoDuration = 10_000;
        int rewardTimeSeconds = 5;
        int postRewardTimeSeconds = 2;
        int expected = 7_000;
        testCloseButtonDelay(videoDuration, rewardTimeSeconds, postRewardTimeSeconds, expected);
    }

    @Test
    public void rewarded_getDelayToShowCloseButton_4() {
        int videoDuration = 10_000;
        int rewardTimeSeconds = 5;
        int postRewardTimeSeconds = 7;
        int expected = 10_000;
        testCloseButtonDelay(videoDuration, rewardTimeSeconds, postRewardTimeSeconds, expected);
    }

    private void testCloseButtonDelay(int videoDuration, int rewardTime, int postRewardTime, int expected) {
        RewardedExt rewardedExt = mock(RewardedExt.class);
        AdUnitConfiguration mockConfig = mockAdConfiguration;
        RewardManager mockRewardManager = mock(RewardManager.class);
        when(mockConfig.getRewardManager()).thenReturn(mockRewardManager);
        when(mockRewardManager.getRewardedExt()).thenReturn(rewardedExt);

        RewardedCompletionRules mockCompletionRules = mock(RewardedCompletionRules.class);
        when(mockCompletionRules.getVideoEvent()).thenReturn(null);
        when(mockCompletionRules.getVideoTime()).thenReturn(rewardTime);
        RewardedClosingRules mockClosingRules = mock(RewardedClosingRules.class);
        when(mockClosingRules.getPostRewardTime()).thenReturn(postRewardTime);

        when(rewardedExt.getCompletionRules()).thenReturn(mockCompletionRules);
        when(rewardedExt.getClosingRules()).thenReturn(mockClosingRules);

        Integer timeToReward = InterstitialVideo.getDelayToShowCloseButton(videoDuration, mockConfig);

        assertEquals(Integer.valueOf(expected), timeToReward);
    }

}