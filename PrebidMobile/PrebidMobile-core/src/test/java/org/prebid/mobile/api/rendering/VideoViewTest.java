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
import android.view.View;
import android.widget.FrameLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.models.CreativeVisibilityTracker;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerResult;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.utils.exposure.ViewExposure;
import org.prebid.mobile.rendering.video.VideoCreativeView;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.AdViewManagerListener;
import org.prebid.mobile.rendering.views.video.VideoViewListener;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.prebid.mobile.api.rendering.VideoView.State.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class VideoViewTest {

    private static final VisibilityTrackerResult VISIBLE_RESULT = new VisibilityTrackerResult(
        NativeEventTracker.EventType.IMPRESSION,
        new ViewExposure(),
        true,
        true
    );
    private static final VisibilityTrackerResult INVISIBLE_RESULT = new VisibilityTrackerResult(
        NativeEventTracker.EventType.IMPRESSION,
        new ViewExposure(),
        false,
        true
    );

    private Context context;

    private VideoView videoView;
    private AdViewManagerListener adViewManagerListener;

    @Mock public AdViewManager mockAdViewManager;
    @Mock public VideoViewListener mockVideoViewListener;
    @Mock private CreativeVisibilityTracker mockVisibilityTracker;
    private CreativeVisibilityTracker.VisibilityTrackerListener visibilityTrackerListener;

    @Before
    public void setup() throws AdException {
        MockitoAnnotations.initMocks(this);
        context = Robolectric.buildActivity(Activity.class).create().get();

        videoView = new VideoView(context);

        WhiteBox.setInternalState(videoView, "visibilityTracker", mockVisibilityTracker);
        WhiteBox.setInternalState(videoView, "adViewManager", mockAdViewManager);
        visibilityTrackerListener = WhiteBox.getInternalState(videoView, "visibilityTrackerListener");
        adViewManagerListener = WhiteBox.getInternalState(videoView, "onAdViewManagerListener");

        videoView.setVideoViewListener(mockVideoViewListener);
    }

    @Test
    public void videoViewConstructor_InstanceNotNull() throws Exception {
        VideoView videoView = new VideoView(context);
        VideoView secondVideoView = new VideoView(context, mock(AdUnitConfiguration.class));

        assertNotNull(videoView);
        assertNotNull(secondVideoView);
    }

    @Test
    public void notifyErrorListeners_InvokeOnLoadFailed() {
        AdException adException = new AdException(AdException.INTERNAL_ERROR, AdException.INIT_ERROR);
        videoView.notifyErrorListeners(adException);

        verify(mockVideoViewListener).onLoadFailed(videoView, adException);
    }

    @Test
    public void destroy() {
        videoView.destroy();
    }

    @Test
    public void play_StateNotStarted_AdViewManagerShow() {
        changeVideoViewState(PLAYBACK_NOT_STARTED);

        videoView.play();

        verify(mockAdViewManager).show();
        assertEquals(PLAYING, getVideoViewState());
    }

    @Test
    public void play_StateInvalid_DoNothing() {
        // initial state
        videoView.play();

        changeVideoViewState(PLAYING);
        videoView.play();

        changeVideoViewState(PAUSED_BY_USER);
        videoView.play();

        changeVideoViewState(PAUSED_AUTO);
        videoView.play();

        changeVideoViewState(PLAYBACK_FINISHED);
        videoView.play();

        verifyNoInteractions(mockAdViewManager);
    }

    @Test
    public void loadAd_LoadBidTransaction() {
        final AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        final BidResponse bidResponse = new BidResponse("", new AdUnitConfiguration());

        videoView.loadAd(adUnitConfiguration, bidResponse);

        verify(mockAdViewManager).loadBidTransaction(adUnitConfiguration, bidResponse);
    }

    @Test
    public void loadAd_vastXml_StopVisibilityTrackingChangeStateToUndefined() {
        changeVideoViewState(PAUSED_AUTO);

        videoView.loadAd(new AdUnitConfiguration(), "somexml");
        VideoView.State stateAfterLoad = WhiteBox.getInternalState(videoView, "videoViewState");

        assertEquals(UNDEFINED, stateAfterLoad);
        verify(mockVisibilityTracker).stopVisibilityCheck();
    }

    @Test
    public void mute_InvokeAdViewManager() {
        videoView.mute(true);
        verify(mockAdViewManager).mute();

        videoView.mute(false);
        verify(mockAdViewManager).unmute();
    }

    @Test
    public void setAutoPlayDisabled_StopVisibilityTracker() {
        videoView.setAutoPlay(false);

        verify(mockVisibilityTracker).stopVisibilityCheck();
    }

    @Test
    public void setAutoPlayEnabled_DoNothing() {
        videoView.setAutoPlay(true);

        verifyNoInteractions(mockVisibilityTracker);
    }

    @Test
    public void pause_StatePlaying_InvokeAdViewManagerAndChangeState() {
        changeVideoViewState(PLAYING);
        videoView.pause();

        verify(mockAdViewManager).pause();
        assertEquals(PAUSED_BY_USER, getVideoViewState());
    }

    @Test
    public void pause_StateInvalid_DoNothing() {
        changeVideoViewState(PLAYBACK_FINISHED);
        videoView.pause();

        changeVideoViewState(UNDEFINED);
        videoView.pause();

        changeVideoViewState(PAUSED_AUTO);
        videoView.pause();

        changeVideoViewState(PAUSED_BY_USER);
        videoView.pause();

        changeVideoViewState(PLAYBACK_NOT_STARTED);
        videoView.pause();

        verifyNoInteractions(mockAdViewManager);
    }

    @Test
    public void resume_StatePausedAutoOrManual_InvokeAdViewManagerAndChangeState() {
        changeVideoViewState(PAUSED_AUTO);
        videoView.resume();

        changeVideoViewState(PAUSED_BY_USER);
        videoView.resume();

        verify(mockAdViewManager, times(2)).resume();
        assertEquals(PLAYING, getVideoViewState());
    }

    @Test
    public void resume_StateInvalid_DoNothing() {
        changeVideoViewState(PLAYBACK_NOT_STARTED);
        videoView.resume();

        changeVideoViewState(UNDEFINED);
        videoView.resume();

        changeVideoViewState(PLAYING);
        videoView.resume();

        changeVideoViewState(PLAYBACK_FINISHED);
        videoView.resume();

        verifyNoInteractions(mockAdViewManager);
    }

    @Test
    public void handleVisibilityChange_visible_validPlayState_ExecutePlay() {

        changeVideoViewState(PLAYBACK_NOT_STARTED);
        VisibilityTrackerResult result = new VisibilityTrackerResult(
            NativeEventTracker.EventType.IMPRESSION,
            new ViewExposure(),
            true,
            true
        );

        visibilityTrackerListener.onVisibilityChanged(result);

        verify(mockAdViewManager).show();
        assertEquals(PLAYING, getVideoViewState());
    }

    @Test
    public void handleVisibilityChange_visible_validAutoResumeState_ExecuteResume() {
        changeVideoViewState(PAUSED_AUTO);

        visibilityTrackerListener.onVisibilityChanged(VISIBLE_RESULT);

        verify(mockAdViewManager).resume();
        assertEquals(PLAYING, getVideoViewState());
    }

    @Test
    public void handleVisibilityChange_visible_validAutoPauseState_ExecutePause() {
        changeVideoViewState(PLAYING);

        visibilityTrackerListener.onVisibilityChanged(INVISIBLE_RESULT);

        verify(mockAdViewManager).pause();
        assertEquals(PAUSED_AUTO, getVideoViewState());
    }

    @Test
    public void handleVisibilityChange_visible_invalidState_DoNothing() {
        changeVideoViewState(PAUSED_BY_USER);
        visibilityTrackerListener.onVisibilityChanged(INVISIBLE_RESULT);
        visibilityTrackerListener.onVisibilityChanged(VISIBLE_RESULT);

        changeVideoViewState(UNDEFINED);
        visibilityTrackerListener.onVisibilityChanged(INVISIBLE_RESULT);
        visibilityTrackerListener.onVisibilityChanged(VISIBLE_RESULT);

        changeVideoViewState(PLAYBACK_FINISHED);
        visibilityTrackerListener.onVisibilityChanged(INVISIBLE_RESULT);
        visibilityTrackerListener.onVisibilityChanged(VISIBLE_RESULT);

        verifyNoInteractions(mockAdViewManager);
    }

    @Test
    public void handleWindowFocusChange_autoPlayEnabled_DoNothing() {
        changeVideoViewState(PLAYING);
        videoView.setAutoPlay(true);
        videoView.handleWindowFocusChange(false);

        changeVideoViewState(PAUSED_AUTO);
        videoView.handleWindowFocusChange(true);

        verifyNoInteractions(mockAdViewManager);
    }

    @Test
    public void handleWindowFocusChange_invisible_autoPlayDisabled_stateValid_ExecutePause() {
        videoView.setAutoPlay(false);
        changeVideoViewState(PLAYING);

        videoView.handleWindowFocusChange(false);

        verify(mockAdViewManager).pause();
        assertEquals(PAUSED_AUTO, getVideoViewState());
    }

    @Test
    public void handleWindowFocusChange_visible_autoPlayDisabled_stateValid_ExecuteResume() {
        videoView.setAutoPlay(false);
        changeVideoViewState(PAUSED_AUTO);

        videoView.handleWindowFocusChange(true);

        verify(mockAdViewManager).resume();
        assertEquals(PLAYING, getVideoViewState());
    }

    @Test
    public void handleWindowFocusChange_autoPlayDisabled_stateInValid_DoNothing() {
        changeVideoViewState(PAUSED_BY_USER);
        videoView.handleWindowFocusChange(true);
        videoView.handleWindowFocusChange(false);

        changeVideoViewState(UNDEFINED);
        videoView.handleWindowFocusChange(true);
        videoView.handleWindowFocusChange(false);

        changeVideoViewState(PLAYBACK_FINISHED);
        videoView.handleWindowFocusChange(true);
        videoView.handleWindowFocusChange(false);

        verifyNoInteractions(mockAdViewManager);
    }

    // region =============== AdViewManagerListener
    @Test
    public void adLoaded_autoPlayEnabled_NotifyListener_changeState() {

        adViewManagerListener.adLoaded(null);

        verify(mockVideoViewListener).onLoaded(videoView, null);
        assertEquals(PLAYBACK_NOT_STARTED, getVideoViewState());
    }

    @Test
    public void adLoaded_autoPlayDisabled_NotifyListener_changeState() {
        videoView.setAutoPlay(false);
        reset(mockVisibilityTracker);

        adViewManagerListener.adLoaded(null);

        verify(mockVideoViewListener).onLoaded(videoView, null);
        assertEquals(PLAYBACK_NOT_STARTED, getVideoViewState());
        verifyNoInteractions(mockVisibilityTracker);
    }

    @Test
    public void viewReadyForImmediateDisplay_notShowingEndCard_ShowVideoCreative() {
        final VideoCreativeView mockVideoCreativeView = mock(VideoCreativeView.class);
        when(mockAdViewManager.isNotShowingEndCard()).thenReturn(true);

        videoView.setVideoPlayerClick(true);

        adViewManagerListener.viewReadyForImmediateDisplay(mockVideoCreativeView);

        verify(mockVideoViewListener).onDisplayed(eq(videoView));
        verify(mockVideoCreativeView).enableVideoPlayerClick();
        verify(mockVideoCreativeView).showVolumeControls();
    }

    @Test
    public void viewReadyForImmediateDisplay_hasEndCard_ShowEndCard_noListenerInvocation() {
        final View mockView = mock(View.class);
        reset(mockView);
        when(mockAdViewManager.isNotShowingEndCard()).thenReturn(false);
        when(mockAdViewManager.hasEndCard()).thenReturn(true);

        adViewManagerListener.viewReadyForImmediateDisplay(mockView);

        verify(mockView, times(2)).setLayoutParams(any(FrameLayout.LayoutParams.class));
        verifyNoInteractions(mockVideoViewListener);
    }

    @Test
    public void failedToLoad_NotifyListener() {
        final AdException expectedException = new AdException(AdException.INTERNAL_ERROR, "message");

        adViewManagerListener.failedToLoad(expectedException);

        verify(mockVideoViewListener).onLoadFailed(videoView, expectedException);
    }

    @Test
    public void videoCreativePlaybackFinished_noEndCard__StopVisibilityTracking_changeState_notifyListener_addWatchAgain() {
        when(mockAdViewManager.isNotShowingEndCard()).thenReturn(true);

        adViewManagerListener.videoCreativePlaybackFinished();

        verify(mockVisibilityTracker).stopVisibilityCheck();
        assertEquals(PLAYBACK_FINISHED, getVideoViewState());
        verify(mockVideoViewListener).onPlayBackCompleted(videoView);
        verify(mockAdViewManager).addObstructions(any());
    }

    @Test
    public void videoCreativePlaybackFinished_hasEndCard__StopVisibilityTracking_changeState_notifyListener() {
        when(mockAdViewManager.isNotShowingEndCard()).thenReturn(false);

        adViewManagerListener.videoCreativePlaybackFinished();

        verify(mockVisibilityTracker).stopVisibilityCheck();
        assertEquals(PLAYBACK_FINISHED, getVideoViewState());
        verify(mockVideoViewListener).onPlayBackCompleted(videoView);
        verify(mockAdViewManager, times(0)).addObstructions(any());
    }

    @Test
    public void creativeClicked_NotifyListener() {
        adViewManagerListener.creativeClicked("test");
        verify(mockVideoViewListener).onClickThroughOpened(videoView);
    }

    @Test
    public void creativeMuted_NotifyListener() {
        adViewManagerListener.creativeMuted();
        verify(mockVideoViewListener).onVideoMuted();
    }

    @Test
    public void creativeUnMuted_NotifyListener() {
        adViewManagerListener.creativeUnMuted();
        verify(mockVideoViewListener).onVideoUnMuted();
    }

    @Test
    public void creativePaused_NotifyListener() {
        adViewManagerListener.creativePaused();
        verify(mockVideoViewListener).onPlaybackPaused();
    }

    @Test
    public void creativeResumed_NotifyListener() {
        adViewManagerListener.creativeResumed();
        verify(mockVideoViewListener).onPlaybackResumed();
    }
    // endregion =============== AdViewManagerListener

    private void changeVideoViewState(VideoView.State state) {
        WhiteBox.setInternalState(videoView, "videoViewState", state);
    }

    private VideoView.State getVideoViewState() {
        return WhiteBox.getInternalState(videoView, "videoViewState");
    }

    // TODO: 2/8/21 Remove or refactor in scope of expand-on-click task
    // @Test
    // public void showFullScreen() throws Exception {
    //     AdViewManager mockAdViewManager = getMockAdViewManager();
    //     when(mockAdViewManager.canShowFullScreen()).thenReturn(true);
    //
    //     VideoView spyVideoAdView = spy(videoAdView);
    //
    //     when(spyVideoAdView.getChildAt(anyInt())).thenReturn(mockVideoCreativeView);
    //
    //     WhiteBox.method(VideoView.class, "showFullScreen").invoke(spyVideoAdView);
    //     verify(spyVideoAdView).createDialog(mockContext, mockVideoCreativeView);
    // }

    // @Test
    // public void isFullScreen() throws IllegalAccessException, InvocationTargetException {
    //     boolean fullScreen = ((boolean) WhiteBox.method(VideoView.class, "isFullScreen").invoke(videoAdView));
    //     assertFalse(fullScreen);
    //
    //     VideoDialog mock = mock(VideoDialog.class);
    //     when(mock.isShowing()).thenReturn(true);
    //     WhiteBox.field(VideoView.class, "videoDialog").set(videoAdView, mock);
    //     fullScreen = ((boolean) WhiteBox.method(VideoView.class, "isFullScreen").invoke(videoAdView));
    //     assertTrue(fullScreen);
    // }

    // @Test
    // public void whenClickthroughBrowserClosed_CallCallback() {
    //
    //     managerDelegate.onClickThroughClosed();
    //     verify(mockVideoViewListener).onClickThroughClosed(any(VideoView.class));
    // }
    //
    // @Test
    // public void whenInterstitialAdClosed_CallCallback() throws IllegalAccessException {
    //     getMockAdViewManager();
    //
    //     managerDelegate.onInterstitialAdClosed();
    //     verify(mockAdViewManager).resetTransactionState();
    //     verify(mockVideoViewListener).onInterstitialClosed(any(VideoView.class));
    // }
    //
    // @Test
    // public void whenShowFullScreen_CallCallback() throws Exception {
    //     getMockAdViewManager();
    //
    //     managerDelegate.onAdClicked();
    //     verify(mockAdViewManager).canShowFullScreen();
    //     verify(mockVideoViewListener).onInterstitialOpened(any(VideoView.class));
    // }
    //
    // @Test
    // public void whenShowWatchAgain_WatchAgainButtonSetup() throws IllegalAccessException {
    //     getMockAdViewManager();
    //
    //     managerDelegate.onVideoCompleted();
    //
    //     verify(mockAdViewManager).addObstructions(any(InternalFriendlyObstruction.class));
    //     assertNotNull(videoAdView.getChildAt(0));
    // }
    //
    // @Test
    // public void whenCollapse_CallCallback() {
    //     managerDelegate.onVideoDialogClosed();
    //     verify(mockVideoViewListener).onInterstitialClosed(any(VideoView.class));
    // }

    // private VideoCreative getSimpleVideoCreative() throws IllegalAccessException {
    //     VideoCreative simpleVideoCreative = mock(VideoCreative.class);
    //     AdConfiguration adConfiguration = new AdConfiguration();
    //     adConfiguration.setBuiltInVideo(true);
    //
    //     when(simpleVideoCreative.getCreativeModel())
    //         .thenReturn(new VideoCreativeModel(mock(TrackingManager.class), mock(OmEventTracker.class), adConfiguration));
    //     WhiteBox.field(VideoView.class, "currentCreativeShown").set(videoView, simpleVideoCreative);
    //     return simpleVideoCreative;
    // }
}