package org.prebid.mobile.rendering.video;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import com.apollo.test.utils.WhiteBox;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ExoPlayerViewTest {
    private ExoPlayerView mExoPlayerView;

    @Mock
    VideoCreative mMockVideoCreative;
    @Mock
    SimpleExoPlayer mMockSimpleExoPlayer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Context context = Robolectric.buildActivity(Activity.class).create().get();

        mExoPlayerView = spy(new ExoPlayerView(context, mMockVideoCreative));
        WhiteBox.field(ExoPlayerView.class, "mPlayer").set(mExoPlayerView, mMockSimpleExoPlayer);

        reset(mMockVideoCreative, mMockSimpleExoPlayer);
    }

    @Test
    public void setValidVolume_TrackEventAndChangePlayerVolume() {
        mExoPlayerView.setVolume(1);

        verify(mMockSimpleExoPlayer, times(1)).setVolume(1);
        verify(mMockVideoCreative, times(1)).onVolumeChanged(1);
    }

    @Test
    public void setInvalidVolume_NoEventAndNoVolumeChange() {
        mExoPlayerView.setVolume(-1);

        verifyNoMoreInteractions(mMockSimpleExoPlayer);
        verifyNoMoreInteractions(mMockVideoCreative);
    }

    @Test
    public void mute() {
        mExoPlayerView.mute();
        verify(mExoPlayerView, times(1)).setVolume(0);
    }

    @Test
    public void isPlaying() {
        mExoPlayerView.isPlaying();
        verify(mMockSimpleExoPlayer).getPlayWhenReady();
        when(mExoPlayerView.isPlaying()).thenReturn(false);
        boolean playing = mExoPlayerView.isPlaying();
        assertFalse(playing);
        doNothing().when(mExoPlayerView).start(anyInt());
        mExoPlayerView.start(anyInt());
        when(mExoPlayerView.isPlaying()).thenReturn(true);
        assertTrue(mExoPlayerView.isPlaying());
    }

    @Test
    public void unMute() {
        mExoPlayerView.unMute();
        verify(mExoPlayerView, times(1)).setVolume(1);
    }

    @Test
    public void startWithNullUri_NoPlayerInteractions() {
        mExoPlayerView.setVideoUri(null);
        mExoPlayerView.start(anyInt());

        verifyZeroInteractions(mMockSimpleExoPlayer);
        verifyZeroInteractions(mMockVideoCreative);
    }

    @Test
    public void startWithValidUri_TrackEventPrepareAndVideoPlayer() {
        mExoPlayerView.setVideoUri(Uri.EMPTY);
        mExoPlayerView.start(anyInt());

        verify(mMockVideoCreative).onEvent(VideoAdEvent.Event.AD_CREATIVEVIEW);
        verify(mMockVideoCreative).onEvent(VideoAdEvent.Event.AD_START);
        verify(mMockSimpleExoPlayer).prepare(any(MediaSource.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void setVastVideoDuration() {
        mExoPlayerView.setVastVideoDuration(1000l);
        verify(mExoPlayerView).setVastVideoDuration(1000l);
    }

    @Test
    public void getCurrentPosition() {
        mExoPlayerView.getCurrentPosition();
        verify(mMockSimpleExoPlayer).getContentPosition();
    }

    @Test
    public void setVideoURI() {
        mExoPlayerView.setVideoUri(mock(Uri.class));
        verify(mExoPlayerView).setVideoUri(any(Uri.class));
    }

    @Test
    public void getDuration() {
        mExoPlayerView.getDuration();
        verify(mMockSimpleExoPlayer).getDuration();
    }

    @Test
    public void getVolume() {
        when(mExoPlayerView.getVolume()).thenReturn(1.0f);
        float volume = mExoPlayerView.getVolume();
        assertEquals(1.0f, volume, 0.0f);
        mExoPlayerView.setVolume(0.5f);
        verify(mExoPlayerView).setVolume(0.5f);
        when(mExoPlayerView.getVolume()).thenReturn(0.5f);
        assertEquals(0.5f, mExoPlayerView.getVolume(), 0f);
    }

    @Test
    public void pause() {
        mExoPlayerView.pause();
        verify(mMockSimpleExoPlayer).stop();
    }

    @Test
    public void destroy() throws IllegalAccessException {
        AdViewProgressUpdateTask mockAdViewProgressUpdateTask = mock(AdViewProgressUpdateTask.class);
        WhiteBox.field(ExoPlayerView.class, "mAdViewProgressUpdateTask").set(mExoPlayerView, mockAdViewProgressUpdateTask);
        mExoPlayerView.destroy();
        verify(mockAdViewProgressUpdateTask).cancel(true);
        verify(mExoPlayerView, times(1)).destroy();
        verify(mMockSimpleExoPlayer).removeListener(any(Player.EventListener.class));
        verify(mExoPlayerView).setPlayer(null);
        verify(mMockSimpleExoPlayer).release();
    }

    @Test
    public void forceStop() {
        mExoPlayerView.forceStop();
        verify(mMockSimpleExoPlayer).stop();
        verify(mExoPlayerView, times(1)).destroy();
        verify(mMockVideoCreative).onDisplayCompleted();
    }

    @Test
    public void resumeWithValidUri_PreparePlayer() {
        mExoPlayerView.setVideoUri(Uri.EMPTY);
        mExoPlayerView.resume();

        verify(mMockSimpleExoPlayer).prepare(any(MediaSource.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void resumeWithInvalidUri_DoNothing() {
        mExoPlayerView.setVideoUri(null);
        mExoPlayerView.resume();

        verifyZeroInteractions(mMockSimpleExoPlayer);
    }
}