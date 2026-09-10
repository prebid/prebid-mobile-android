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

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class ExoPlayerViewTest {
    private ExoPlayerView exoPlayerView;

    @Mock
    VideoCreative mockVideoCreative;
    @Mock
    ExoPlayer mockExoPlayer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Context context = Robolectric.buildActivity(Activity.class).create().get();

        exoPlayerView = spy(new ExoPlayerView(context, mockVideoCreative));
        WhiteBox.field(ExoPlayerView.class, "player").set(exoPlayerView, mockExoPlayer);

        reset(mockVideoCreative, mockExoPlayer);
    }

    @Test
    public void setValidVolume_TrackEventAndChangePlayerVolume() {
        exoPlayerView.setVolume(1);

        verify(mockExoPlayer, times(1)).setVolume(1);
        verify(mockVideoCreative, times(1)).onVolumeChanged(1);
    }

    @Test
    public void setInvalidVolume_NoEventAndNoVolumeChange() {
        exoPlayerView.setVolume(-1);

        verifyNoMoreInteractions(mockExoPlayer);
        verifyNoMoreInteractions(mockVideoCreative);
    }

    @Test
    public void mute() {
        exoPlayerView.mute();
        verify(exoPlayerView, times(1)).setVolume(0);
    }

    @Test
    public void isPlaying() {
        exoPlayerView.isPlaying();
        verify(mockExoPlayer).isPlaying();
        when(exoPlayerView.isPlaying()).thenReturn(false);
        boolean playing = exoPlayerView.isPlaying();
        assertFalse(playing);
        doNothing().when(exoPlayerView).start(anyInt());
        exoPlayerView.start(anyInt());
        when(exoPlayerView.isPlaying()).thenReturn(true);
        assertTrue(exoPlayerView.isPlaying());
    }

    @Test
    public void unMute() {
        exoPlayerView.unMute();
        verify(exoPlayerView, times(1)).setVolume(1);
    }

    @Test
    public void startWithNullUri_NoPlayerInteractions() {
        exoPlayerView.setVideoUri(null);
        exoPlayerView.start(anyInt());

        verifyNoInteractions(mockExoPlayer);
        verifyNoInteractions(mockVideoCreative);
    }

    @Test
    public void startWithValidUri_TrackEventPrepareAndVideoPlayer() {
        exoPlayerView.setVideoUri(Uri.EMPTY);
        exoPlayerView.start(anyInt());

        verify(mockVideoCreative).onEvent(VideoAdEvent.Event.AD_CREATIVEVIEW);
        verify(mockVideoCreative).onEvent(VideoAdEvent.Event.AD_START);
        verify(mockExoPlayer).setMediaSource(any(MediaSource.class), anyBoolean());
        verify(mockExoPlayer).prepare();
    }

    @Test
    public void setVastVideoDuration() {
        exoPlayerView.setVastVideoDuration(1000L);
        verify(exoPlayerView).setVastVideoDuration(1000L);
    }

    @Test
    public void getCurrentPosition() {
        exoPlayerView.getCurrentPosition();
        verify(mockExoPlayer).getContentPosition();
    }

    @Test
    public void setVideoURI() {
        exoPlayerView.setVideoUri(mock(Uri.class));
        verify(exoPlayerView).setVideoUri(any(Uri.class));
    }

    @Test
    public void getDuration() {
        exoPlayerView.getDuration();
        verify(mockExoPlayer).getDuration();
    }

    @Test
    public void getVolume() {
        when(exoPlayerView.getVolume()).thenReturn(1.0f);
        float volume = exoPlayerView.getVolume();
        assertEquals(1.0f, volume, 0.0f);
        exoPlayerView.setVolume(0.5f);
        verify(exoPlayerView).setVolume(0.5f);
        when(exoPlayerView.getVolume()).thenReturn(0.5f);
        assertEquals(0.5f, exoPlayerView.getVolume(), 0f);
    }

    @Test
    public void pause() {
        exoPlayerView.pause();
        verify(mockExoPlayer).stop();
    }

    @Test
    public void destroy() throws IllegalAccessException {
        AdViewProgressUpdateTask mockAdViewProgressUpdateTask = mock(AdViewProgressUpdateTask.class);
        WhiteBox.field(ExoPlayerView.class, "adViewProgressUpdateTask").set(exoPlayerView, mockAdViewProgressUpdateTask);
        exoPlayerView.destroy();
        verify(mockAdViewProgressUpdateTask).cancel(true);
        verify(exoPlayerView, times(1)).destroy();
        verify(mockExoPlayer).removeListener(any(Player.Listener.class));
        verify(exoPlayerView).setPlayer(null);
        verify(mockExoPlayer).release();
    }

    @Test
    public void forceStop() {
        exoPlayerView.forceStop();
        verify(mockExoPlayer).stop();
        verify(exoPlayerView, times(1)).destroy();
        verify(mockVideoCreative).onDisplayCompleted();
    }

    @Test
    public void resumeWithValidUri_PreparePlayer() {
        exoPlayerView.setVideoUri(Uri.EMPTY);
        exoPlayerView.resume();

        verify(mockExoPlayer).setMediaSource(any(MediaSource.class), anyBoolean());
        verify(mockExoPlayer).prepare();
    }

    @Test
    public void resumeWithInvalidUri_DoNothing() {
        exoPlayerView.setVideoUri(null);
        exoPlayerView.resume();

        verifyNoInteractions(mockExoPlayer);
    }
}
