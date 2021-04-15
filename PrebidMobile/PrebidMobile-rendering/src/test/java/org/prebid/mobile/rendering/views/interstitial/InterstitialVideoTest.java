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
import org.prebid.mobile.rendering.bidding.display.InterstitialView;
import org.prebid.mobile.rendering.models.AbstractCreative;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.prebid.mobile.rendering.video.VideoCreativeModel;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.AdViewManagerListener;
import org.prebid.mobile.test.utils.WhiteBox;

import java.util.Timer;
import java.util.TimerTask;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InterstitialVideoTest {
    @Mock
    private InterstitialView mMockAdView;
    @Mock
    private Handler mMockHandler;
    @Mock
    private InterstitialManager mMockInterstitialManager;
    @Mock
    private AdConfiguration mMockAdConfiguration;

    private InterstitialVideo mSpyInterstitialVideo;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mMockInterstitialManager.getInterstitialDisplayProperties()).thenReturn(mock(InterstitialDisplayPropertiesInternal.class));
        when(mMockAdView.getMediaOffset()).thenReturn(-1L);

        mSpyInterstitialVideo = Mockito.spy(new InterstitialVideo(null, mMockAdView, mMockInterstitialManager, mMockAdConfiguration));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(mMockHandler).post(any(Runnable.class));
        WhiteBox.setInternalState(mSpyInterstitialVideo, "mHandler", mMockHandler);

        mockMediaDuration(30 * 1000L);

        // ignore, since involves android SDK classes (views). View display is tested in UI tests.
        doNothing().when(mSpyInterstitialVideo).showDurationTimer(anyLong());
    }

    @Test
    public void scheduleShowCloseBtnTask_WithDefinedOffset_TimerIsScheduledWithOffsetValue() {
        mockOffset(7000L);
        mockMediaDuration(30 * 1000L);
        mSpyInterstitialVideo.setShowButtonOnComplete(true);

        mSpyInterstitialVideo.scheduleShowCloseBtnTask(mMockAdView);

        assertTrue(mSpyInterstitialVideo.shouldShowCloseButtonOnComplete());
        verify(mSpyInterstitialVideo, times(1)).scheduleTimer(eq(7000L));
    }

    @Test
    public void scheduleShowCloseBtnTask_ForShortVideo_NoTimerScheduled() {
        // Short video
        mockMediaDuration(2L * 1000);
        mSpyInterstitialVideo.setShowButtonOnComplete(false);

        mSpyInterstitialVideo.scheduleShowCloseBtnTask(mMockAdView);

        assertTrue(mSpyInterstitialVideo.shouldShowCloseButtonOnComplete());
        verify(mSpyInterstitialVideo, never()).scheduleTimer(anyLong());
    }

    @Test
    public void scheduleShowCloseBtnTask_ForZeroDuration_NoTimerScheduledAndShowCloseButtonOnCompleteTrue() {
        // Short video
        mockMediaDuration(0);
        mSpyInterstitialVideo.setShowButtonOnComplete(false);

        mSpyInterstitialVideo.scheduleShowCloseBtnTask(mMockAdView);

        assertTrue(mSpyInterstitialVideo.shouldShowCloseButtonOnComplete());
        verify(mSpyInterstitialVideo, never()).scheduleTimer(anyLong());
    }

    @Test
    public void whenGetMediaOffsetValue_ShowCloseButtonAfterPeriod() {
        mockOffset(7000L);

        mSpyInterstitialVideo.scheduleShowCloseBtnTask(mMockAdView);

        verify(mSpyInterstitialVideo).scheduleTimer(7000L);
    }

    @Test
    public void videoPausedTest() {
        mSpyInterstitialVideo.pauseVideo();
        assertTrue(mSpyInterstitialVideo.isVideoPaused());
        mSpyInterstitialVideo.resumeVideo();
        assertFalse(mSpyInterstitialVideo.isVideoPaused());
    }

    @Test
    public void scheduleShowCloseBtnAfterPauseTest() throws IllegalAccessException {
        Timer mockTimer = mock(Timer.class);
        TimerTask mockTimerTask = mock(TimerTask.class);
        WhiteBox.field(InterstitialVideo.class, "mCurrentTimerTask").set(mSpyInterstitialVideo, mockTimerTask);
        WhiteBox.field(InterstitialVideo.class, "mTimer").set(mSpyInterstitialVideo, mockTimer);

        mSpyInterstitialVideo.pauseVideo();

        verify(mockTimer, times(1)).cancel();
        verify(mockTimer, times(1)).purge();
        verify(mockTimerTask, times(1)).cancel();
        verify(mSpyInterstitialVideo, never()).scheduleTimer(anyLong());
    }

    @Test
    public void scheduleShowCloseBtnAfterResumeTest() {
        mSpyInterstitialVideo.setRemainingTimeInMs(5000);
        mSpyInterstitialVideo.setShowButtonOnComplete(false);

        mSpyInterstitialVideo.resumeVideo();

        assertFalse(mSpyInterstitialVideo.shouldShowCloseButtonOnComplete());
        verify(mSpyInterstitialVideo, times(1)).scheduleTimer(5 * 1000L);
    }

    @Test
    public void queueUIThreadTaskTest() {
        Runnable mockRunnable = mock(Runnable.class);
        mSpyInterstitialVideo.queueUIThreadTask(mockRunnable);
        verify(mMockHandler).post(eq(mockRunnable));
    }

    @Test
    public void closeTest() {
        mSpyInterstitialVideo.close();
        verify(mMockInterstitialManager).interstitialAdClosed();
    }

    @Test
    public void removeViewsTest() throws IllegalAccessException {
        FrameLayout mockContainer = mock(FrameLayout.class);
        WhiteBox.field(InterstitialVideo.class, "mAdViewContainer").set(mSpyInterstitialVideo, mockContainer);

        mSpyInterstitialVideo.removeViews();
        verify(mockContainer).removeAllViews();
    }

    @Test
    public void handleCloseClickTest() {
        mSpyInterstitialVideo.handleCloseClick();

        verify(mSpyInterstitialVideo, atLeastOnce()).close();
    }

    @Test
    public void whenAllOffsetsPresent_UseSscOffset() throws Exception {
        Context context = mock(Context.class);
        AdViewManager adViewManager = new AdViewManager(context, mock(AdViewManagerListener.class), mMockAdView, mMockInterstitialManager);

        AdConfiguration adConfiguration = adViewManager.getAdConfiguration();
        adConfiguration.setVideoSkipOffset(10000);

        AbstractCreative mockCreative = mock(AbstractCreative.class);
        VideoCreativeModel mockModel = mock(VideoCreativeModel.class);
        when(mockModel.getSkipOffset()).thenReturn(20000L);
        when(mockCreative.getCreativeModel()).thenReturn(mockModel);

        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(adViewManager, mockCreative);
        when(mMockAdView.getMediaOffset()).thenReturn(adViewManager.getSkipOffset());

        mSpyInterstitialVideo.scheduleShowCloseBtnTask(mMockAdView);
        verify(mSpyInterstitialVideo).scheduleTimer(10L * 1000);
    }

    @Test
    public void whenVastAndSscOffsetPresent_UseSscOffset() throws Exception {
        Context context = mock(Context.class);
        AdViewManager adViewManager = new AdViewManager(context, mock(AdViewManagerListener.class), mMockAdView, mMockInterstitialManager);

        AdConfiguration adConfiguration = adViewManager.getAdConfiguration();
        adConfiguration.setVideoSkipOffset(10000);

        AbstractCreative mockCreative = mock(AbstractCreative.class);
        VideoCreativeModel mockModel = mock(VideoCreativeModel.class);
        when(mockModel.getSkipOffset()).thenReturn(20000L);
        when(mockCreative.getCreativeModel()).thenReturn(mockModel);

        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(adViewManager, mockCreative);
        when(mMockAdView.getMediaOffset()).thenReturn(adViewManager.getSkipOffset());

        mSpyInterstitialVideo.scheduleShowCloseBtnTask(mMockAdView);
        verify(mSpyInterstitialVideo).scheduleTimer(10L * 1000);
    }

    @Test
    public void whenRemainingTimePresent_UseRemainingTime() throws Exception {
        Context context = mock(Context.class);
        AdViewManager adViewManager = new AdViewManager(context, mock(AdViewManagerListener.class), mMockAdView, mock(InterstitialManager.class));

        AdConfiguration adConfiguration = adViewManager.getAdConfiguration();
        adConfiguration.setVideoSkipOffset(10);

        AbstractCreative mockCreative = mock(AbstractCreative.class);
        VideoCreativeModel mockModel = mock(VideoCreativeModel.class);
        when(mockModel.getSkipOffset()).thenReturn(20L);
        when(mockCreative.getCreativeModel()).thenReturn(mockModel);

        WhiteBox.field(AdViewManager.class, "mCurrentCreative").set(adViewManager, mockCreative);
        when(mMockAdView.getMediaOffset()).thenReturn(adViewManager.getSkipOffset());

        mSpyInterstitialVideo.setRemainingTimeInMs(3000);

        mSpyInterstitialVideo.scheduleShowCloseBtnTask(mMockAdView, 3000);
        verify(mSpyInterstitialVideo).scheduleTimer(3L * 1000);
    }

    @Test
    public void whenNoOffsetPresent_UseDefaultOffset() {
        mSpyInterstitialVideo.scheduleShowCloseBtnTask(mMockAdView);
        verify(mSpyInterstitialVideo).scheduleTimer(2L * 1000);
    }

    private void mockMediaDuration(long duration) {
        when(mMockAdView.getMediaDuration()).thenReturn(duration);
    }

    private void mockOffset(long value) {
        when(mMockAdView.getMediaOffset()).thenReturn(value);
    }
}