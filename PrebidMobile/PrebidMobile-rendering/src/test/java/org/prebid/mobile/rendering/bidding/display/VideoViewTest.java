package org.prebid.mobile.rendering.bidding.display;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.apollo.test.utils.WhiteBox;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.CreativeVisibilityTracker;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerResult;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.utils.exposure.ViewExposure;
import org.prebid.mobile.rendering.video.VideoCreativeView;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.AdViewManagerListener;
import org.prebid.mobile.rendering.views.video.VideoViewListener;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.prebid.mobile.rendering.bidding.display.VideoView.State.PAUSED_AUTO;
import static org.prebid.mobile.rendering.bidding.display.VideoView.State.PAUSED_BY_USER;
import static org.prebid.mobile.rendering.bidding.display.VideoView.State.PLAYBACK_FINISHED;
import static org.prebid.mobile.rendering.bidding.display.VideoView.State.PLAYBACK_NOT_STARTED;
import static org.prebid.mobile.rendering.bidding.display.VideoView.State.PLAYING;
import static org.prebid.mobile.rendering.bidding.display.VideoView.State.UNDEFINED;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class VideoViewTest {
    private static final VisibilityTrackerResult VISIBLE_RESULT = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                                              new ViewExposure(),
                                                                                              true,
                                                                                              true);
    private static final VisibilityTrackerResult INVISIBLE_RESULT = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                                                new ViewExposure(),
                                                                                                false,
                                                                                                true);

    private Context mContext;

    private VideoView mVideoView;
    private AdViewManagerListener mAdViewManagerListener;

    @Mock
    public AdViewManager mMockAdViewManager;
    @Mock
    public VideoViewListener mMockVideoViewListener;
    @Mock
    private CreativeVisibilityTracker mMockVisibilityTracker;
    private CreativeVisibilityTracker.VisibilityTrackerListener mVisibilityTrackerListener;

    @Before
    public void setup() throws AdException {
        MockitoAnnotations.initMocks(this);
        mContext = Robolectric.buildActivity(Activity.class).create().get();

        mVideoView = new VideoView(mContext);

        WhiteBox.setInternalState(mVideoView, "mVisibilityTracker", mMockVisibilityTracker);
        WhiteBox.setInternalState(mVideoView, "mAdViewManager", mMockAdViewManager);
        mVisibilityTrackerListener = WhiteBox.getInternalState(mVideoView, "mVisibilityTrackerListener");
        mAdViewManagerListener = WhiteBox.getInternalState(mVideoView, "mOnAdViewManagerListener");

        mVideoView.setVideoViewListener(mMockVideoViewListener);
    }

    @Test
    public void videoViewConstructor_InstanceNotNull() throws Exception {
        VideoView videoView = new VideoView(mContext);
        VideoView secondVideoView = new VideoView(mContext, mock(AdConfiguration.class));

        assertNotNull(videoView);
        assertNotNull(secondVideoView);
    }

    @Test
    public void notifyErrorListeners_InvokeOnLoadFailed() {
        AdException adException = new AdException(AdException.INTERNAL_ERROR, AdException.INIT_ERROR);
        mVideoView.notifyErrorListeners(adException);

        verify(mMockVideoViewListener).onLoadFailed(mVideoView, adException);
    }

    @Test
    public void destroy() {
        mVideoView.destroy();
    }

    @Test
    public void play_StateNotStarted_AdViewManagerShow() {
        changeVideoViewState(PLAYBACK_NOT_STARTED);

        mVideoView.play();

        verify(mMockAdViewManager).show();
        assertEquals(PLAYING, getVideoViewState());
    }

    @Test
    public void play_StateInvalid_DoNothing() {
        // initial state
        mVideoView.play();

        changeVideoViewState(PLAYING);
        mVideoView.play();

        changeVideoViewState(PAUSED_BY_USER);
        mVideoView.play();

        changeVideoViewState(PAUSED_AUTO);
        mVideoView.play();

        changeVideoViewState(PLAYBACK_FINISHED);
        mVideoView.play();

        verifyZeroInteractions(mMockAdViewManager);
    }

    @Test
    public void loadAd_LoadBidTransaction() {
        final AdConfiguration adUnitConfiguration = new AdConfiguration();
        final BidResponse bidResponse = new BidResponse("");

        mVideoView.loadAd(adUnitConfiguration, bidResponse);

        verify(mMockAdViewManager).loadBidTransaction(adUnitConfiguration, bidResponse);
    }

    @Test
    public void loadAd_vastXml_StopVisibilityTrackingChangeStateToUndefined() {
        changeVideoViewState(PAUSED_AUTO);

        mVideoView.loadAd(new AdConfiguration(), "somexml");
        VideoView.State stateAfterLoad = WhiteBox.getInternalState(mVideoView, "mVideoViewState");

        assertEquals(UNDEFINED, stateAfterLoad);
        verify(mMockVisibilityTracker).stopVisibilityCheck();
    }

    @Test
    public void mute_InvokeAdViewManager() {
        mVideoView.mute(true);
        verify(mMockAdViewManager).mute();

        mVideoView.mute(false);
        verify(mMockAdViewManager).unmute();
    }

    @Test
    public void setAutoPlayDisabled_StopVisibilityTracker() {
        mVideoView.setAutoPlay(false);

        verify(mMockVisibilityTracker).stopVisibilityCheck();
    }

    @Test
    public void setAutoPlayEnabled_DoNothing() {
        mVideoView.setAutoPlay(true);

        verifyZeroInteractions(mMockVisibilityTracker);
    }

    @Test
    public void pause_StatePlaying_InvokeAdViewManagerAndChangeState() {
        changeVideoViewState(PLAYING);
        mVideoView.pause();

        verify(mMockAdViewManager).pause();
        assertEquals(PAUSED_BY_USER, getVideoViewState());
    }

    @Test
    public void pause_StateInvalid_DoNothing() {
        changeVideoViewState(PLAYBACK_FINISHED);
        mVideoView.pause();

        changeVideoViewState(UNDEFINED);
        mVideoView.pause();

        changeVideoViewState(PAUSED_AUTO);
        mVideoView.pause();

        changeVideoViewState(PAUSED_BY_USER);
        mVideoView.pause();

        changeVideoViewState(PLAYBACK_NOT_STARTED);
        mVideoView.pause();

        verifyZeroInteractions(mMockAdViewManager);
    }

    @Test
    public void resume_StatePausedAutoOrManual_InvokeAdViewManagerAndChangeState() {
        changeVideoViewState(PAUSED_AUTO);
        mVideoView.resume();

        changeVideoViewState(PAUSED_BY_USER);
        mVideoView.resume();

        verify(mMockAdViewManager, times(2)).resume();
        assertEquals(PLAYING, getVideoViewState());
    }

    @Test
    public void resume_StateInvalid_DoNothing() {
        changeVideoViewState(PLAYBACK_NOT_STARTED);
        mVideoView.resume();

        changeVideoViewState(UNDEFINED);
        mVideoView.resume();

        changeVideoViewState(PLAYING);
        mVideoView.resume();

        changeVideoViewState(PLAYBACK_FINISHED);
        mVideoView.resume();

        verifyZeroInteractions(mMockAdViewManager);
    }

    @Test
    public void handleVisibilityChange_visible_validPlayState_ExecutePlay() {

        changeVideoViewState(PLAYBACK_NOT_STARTED);
        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                     new ViewExposure(),
                                                                     true,
                                                                     true);

        mVisibilityTrackerListener.onVisibilityChanged(result);

        verify(mMockAdViewManager).show();
        assertEquals(PLAYING, getVideoViewState());
    }

    @Test
    public void handleVisibilityChange_visible_validAutoResumeState_ExecuteResume() {
        changeVideoViewState(PAUSED_AUTO);

        mVisibilityTrackerListener.onVisibilityChanged(VISIBLE_RESULT);

        verify(mMockAdViewManager).resume();
        assertEquals(PLAYING, getVideoViewState());
    }

    @Test
    public void handleVisibilityChange_visible_validAutoPauseState_ExecutePause() {
        changeVideoViewState(PLAYING);

        mVisibilityTrackerListener.onVisibilityChanged(INVISIBLE_RESULT);

        verify(mMockAdViewManager).pause();
        assertEquals(PAUSED_AUTO, getVideoViewState());
    }

    @Test
    public void handleVisibilityChange_visible_invalidState_DoNothing() {
        changeVideoViewState(PAUSED_BY_USER);
        mVisibilityTrackerListener.onVisibilityChanged(INVISIBLE_RESULT);
        mVisibilityTrackerListener.onVisibilityChanged(VISIBLE_RESULT);

        changeVideoViewState(UNDEFINED);
        mVisibilityTrackerListener.onVisibilityChanged(INVISIBLE_RESULT);
        mVisibilityTrackerListener.onVisibilityChanged(VISIBLE_RESULT);

        changeVideoViewState(PLAYBACK_FINISHED);
        mVisibilityTrackerListener.onVisibilityChanged(INVISIBLE_RESULT);
        mVisibilityTrackerListener.onVisibilityChanged(VISIBLE_RESULT);

        verifyZeroInteractions(mMockAdViewManager);
    }

    @Test
    public void handleWindowFocusChange_autoPlayEnabled_DoNothing() {
        changeVideoViewState(PLAYING);
        mVideoView.setAutoPlay(true);
        mVideoView.handleWindowFocusChange(false);

        changeVideoViewState(PAUSED_AUTO);
        mVideoView.handleWindowFocusChange(true);

        verifyZeroInteractions(mMockAdViewManager);
    }

    @Test
    public void handleWindowFocusChange_invisible_autoPlayDisabled_stateValid_ExecutePause() {
        mVideoView.setAutoPlay(false);
        changeVideoViewState(PLAYING);

        mVideoView.handleWindowFocusChange(false);

        verify(mMockAdViewManager).pause();
        assertEquals(PAUSED_AUTO, getVideoViewState());
    }

    @Test
    public void handleWindowFocusChange_visible_autoPlayDisabled_stateValid_ExecuteResume() {
        mVideoView.setAutoPlay(false);
        changeVideoViewState(PAUSED_AUTO);

        mVideoView.handleWindowFocusChange(true);

        verify(mMockAdViewManager).resume();
        assertEquals(PLAYING, getVideoViewState());
    }

    @Test
    public void handleWindowFocusChange_autoPlayDisabled_stateInValid_DoNothing() {
        changeVideoViewState(PAUSED_BY_USER);
        mVideoView.handleWindowFocusChange(true);
        mVideoView.handleWindowFocusChange(false);

        changeVideoViewState(UNDEFINED);
        mVideoView.handleWindowFocusChange(true);
        mVideoView.handleWindowFocusChange(false);

        changeVideoViewState(PLAYBACK_FINISHED);
        mVideoView.handleWindowFocusChange(true);
        mVideoView.handleWindowFocusChange(false);

        verifyZeroInteractions(mMockAdViewManager);
    }

    // region =============== AdViewManagerListener
    @Test
    public void adLoaded_autoPlayEnabled_NotifyListener_changeState() {

        mAdViewManagerListener.adLoaded(null);

        verify(mMockVideoViewListener).onLoaded(mVideoView, null);
        assertEquals(PLAYBACK_NOT_STARTED, getVideoViewState());
    }

    @Test
    public void adLoaded_autoPlayDisabled_NotifyListener_changeState() {
        mVideoView.setAutoPlay(false);
        reset(mMockVisibilityTracker);

        mAdViewManagerListener.adLoaded(null);

        verify(mMockVideoViewListener).onLoaded(mVideoView, null);
        assertEquals(PLAYBACK_NOT_STARTED, getVideoViewState());
        verifyZeroInteractions(mMockVisibilityTracker);
    }

    @Test
    public void viewReadyForImmediateDisplay_notShowingEndCard_ShowVideoCreative() {
        final VideoCreativeView mockVideoCreativeView = mock(VideoCreativeView.class);
        when(mMockAdViewManager.isNotShowingEndCard()).thenReturn(true);

        mVideoView.setVideoPlayerClick(true);

        mAdViewManagerListener.viewReadyForImmediateDisplay(mockVideoCreativeView);

        verify(mMockVideoViewListener).onDisplayed(eq(mVideoView));
        verify(mockVideoCreativeView).enableVideoPlayerClick();
        verify(mockVideoCreativeView).showVolumeControls();
    }

    @Test
    public void viewReadyForImmediateDisplay_hasEndCard_ShowEndCard_noListenerInvocation() {
        final View mockView = mock(View.class);
        reset(mockView);
        when(mMockAdViewManager.isNotShowingEndCard()).thenReturn(false);
        when(mMockAdViewManager.hasEndCard()).thenReturn(true);

        mAdViewManagerListener.viewReadyForImmediateDisplay(mockView);

        verify(mockView, times(2)).setLayoutParams(any(FrameLayout.LayoutParams.class));
        verifyZeroInteractions(mMockVideoViewListener);
    }

    @Test
    public void failedToLoad_NotifyListener() {
        final AdException expectedException = new AdException(AdException.INTERNAL_ERROR, "message");

        mAdViewManagerListener.failedToLoad(expectedException);

        verify(mMockVideoViewListener).onLoadFailed(mVideoView, expectedException);
    }

    @Test
    public void videoCreativePlaybackFinished_noEndCard__StopVisibilityTracking_changeState_notifyListener_addWatchAgain() {
        when(mMockAdViewManager.isNotShowingEndCard()).thenReturn(true);

        mAdViewManagerListener.videoCreativePlaybackFinished();

        verify(mMockVisibilityTracker).stopVisibilityCheck();
        assertEquals(PLAYBACK_FINISHED, getVideoViewState());
        verify(mMockVideoViewListener).onPlayBackCompleted(mVideoView);
        verify(mMockAdViewManager).addObstructions(any());
    }

    @Test
    public void videoCreativePlaybackFinished_hasEndCard__StopVisibilityTracking_changeState_notifyListener() {
        when(mMockAdViewManager.isNotShowingEndCard()).thenReturn(false);

        mAdViewManagerListener.videoCreativePlaybackFinished();

        verify(mMockVisibilityTracker).stopVisibilityCheck();
        assertEquals(PLAYBACK_FINISHED, getVideoViewState());
        verify(mMockVideoViewListener).onPlayBackCompleted(mVideoView);
        verify(mMockAdViewManager, times(0)).addObstructions(any());
    }

    @Test
    public void creativeClicked_NotifyListener() {
        mAdViewManagerListener.creativeClicked("test");
        verify(mMockVideoViewListener).onClickThroughOpened(mVideoView);
    }

    @Test
    public void creativeMuted_NotifyListener() {
        mAdViewManagerListener.creativeMuted();
        verify(mMockVideoViewListener).onVideoMuted();
    }

    @Test
    public void creativeUnMuted_NotifyListener() {
        mAdViewManagerListener.creativeUnMuted();
        verify(mMockVideoViewListener).onVideoUnMuted();
    }

    @Test
    public void creativePaused_NotifyListener() {
        mAdViewManagerListener.creativePaused();
        verify(mMockVideoViewListener).onPlaybackPaused();
    }

    @Test
    public void creativeResumed_NotifyListener() {
        mAdViewManagerListener.creativeResumed();
        verify(mMockVideoViewListener).onPlaybackResumed();
    }
    // endregion =============== AdViewManagerListener

    private void changeVideoViewState(VideoView.State state) {
        WhiteBox.setInternalState(mVideoView, "mVideoViewState", state);
    }

    private VideoView.State getVideoViewState() {
        return WhiteBox.getInternalState(mVideoView, "mVideoViewState");
    }

    // TODO: 2/8/21 Remove or refactor in scope of expand-on-click task
    // @Test
    // public void showFullScreen() throws Exception {
    //     AdViewManager mockAdViewManager = getMockAdViewManager();
    //     when(mockAdViewManager.canShowFullScreen()).thenReturn(true);
    //
    //     VideoView spyVideoAdView = spy(mVideoAdView);
    //
    //     when(spyVideoAdView.getChildAt(anyInt())).thenReturn(mMockVideoCreativeView);
    //
    //     WhiteBox.method(VideoView.class, "showFullScreen").invoke(spyVideoAdView);
    //     verify(spyVideoAdView).createDialog(mMockContext, mMockVideoCreativeView);
    // }

    // @Test
    // public void isFullScreen() throws IllegalAccessException, InvocationTargetException {
    //     boolean fullScreen = ((boolean) WhiteBox.method(VideoView.class, "isFullScreen").invoke(mVideoAdView));
    //     assertFalse(fullScreen);
    //
    //     VideoDialog mock = mock(VideoDialog.class);
    //     when(mock.isShowing()).thenReturn(true);
    //     WhiteBox.field(VideoView.class, "mVideoDialog").set(mVideoAdView, mock);
    //     fullScreen = ((boolean) WhiteBox.method(VideoView.class, "isFullScreen").invoke(mVideoAdView));
    //     assertTrue(fullScreen);
    // }

    // @Test
    // public void whenClickthroughBrowserClosed_CallCallback() {
    //
    //     mManagerDelegate.onClickThroughClosed();
    //     verify(mMockVideoViewListener).onClickThroughClosed(any(VideoView.class));
    // }
    //
    // @Test
    // public void whenInterstitialAdClosed_CallCallback() throws IllegalAccessException {
    //     getMockAdViewManager();
    //
    //     mManagerDelegate.onInterstitialAdClosed();
    //     verify(mMockAdViewManager).resetTransactionState();
    //     verify(mMockVideoViewListener).onInterstitialClosed(any(VideoView.class));
    // }
    //
    // @Test
    // public void whenShowFullScreen_CallCallback() throws Exception {
    //     getMockAdViewManager();
    //
    //     mManagerDelegate.onAdClicked();
    //     verify(mMockAdViewManager).canShowFullScreen();
    //     verify(mMockVideoViewListener).onInterstitialOpened(any(VideoView.class));
    // }
    //
    // @Test
    // public void whenShowWatchAgain_WatchAgainButtonSetup() throws IllegalAccessException {
    //     getMockAdViewManager();
    //
    //     mManagerDelegate.onVideoCompleted();
    //
    //     verify(mMockAdViewManager).addObstructions(any(InternalFriendlyObstruction.class));
    //     assertNotNull(mVideoAdView.getChildAt(0));
    // }
    //
    // @Test
    // public void whenCollapse_CallCallback() {
    //     mManagerDelegate.onVideoDialogClosed();
    //     verify(mMockVideoViewListener).onInterstitialClosed(any(VideoView.class));
    // }

    // private VideoCreative getSimpleVideoCreative() throws IllegalAccessException {
    //     VideoCreative simpleVideoCreative = mock(VideoCreative.class);
    //     AdConfiguration adConfiguration = new AdConfiguration();
    //     adConfiguration.setBuiltInVideo(true);
    //
    //     when(simpleVideoCreative.getCreativeModel())
    //         .thenReturn(new VideoCreativeModel(mock(TrackingManager.class), mock(OmEventTracker.class), adConfiguration));
    //     WhiteBox.field(VideoView.class, "mCurrentCreativeShown").set(mVideoView, simpleVideoCreative);
    //     return simpleVideoCreative;
    // }
}