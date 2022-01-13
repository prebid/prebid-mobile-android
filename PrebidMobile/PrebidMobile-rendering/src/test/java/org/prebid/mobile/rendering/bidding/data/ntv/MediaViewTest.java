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

package org.prebid.mobile.rendering.bidding.data.ntv;

import android.app.Activity;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.display.VideoView;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.listeners.MediaViewListener;
import org.prebid.mobile.rendering.views.video.VideoViewListener;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class MediaViewTest {

    private MediaView mMediaView;
    private VideoViewListener mVideoViewListener;

    @Mock
    private VideoView mMockVideoView;
    @Mock
    private MediaViewListener mMockListener;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Context context = Robolectric.buildActivity(Activity.class).create().get();
        mMediaView = new MediaView(context);
        mMediaView.setMediaViewListener(mMockListener);

        mVideoViewListener = WhiteBox.getInternalState(mMediaView, "mVideoViewListener");
        WhiteBox.setInternalState(mMediaView, "mVideoView", mMockVideoView);
        assertNotNull(mMediaView);
    }

    // region ================= VideoViewListener tests

    @Test
    public void onLoaded_VideoLoadingFinished() {
        mVideoViewListener.onLoaded(mMockVideoView, null);

        verify(mMockListener).onVideoLoadingFinished(mMediaView);
    }

    @Test
    public void onLoadFailed_OnFailure() {
        final AdException expectedException = new AdException(AdException.INTERNAL_ERROR, "test");
        mVideoViewListener.onLoadFailed(mMockVideoView, expectedException);

        verify(mMockListener).onFailure(expectedException);
    }

    @Test
    public void onDisplayed_OnMediaPlaybackStarted() {
        mVideoViewListener.onDisplayed(mMockVideoView);

        verify(mMockListener).onMediaPlaybackStarted(mMediaView);
    }

    @Test
    public void onPlaybackCompleted_onMediaPlaybackFinished() {
        mVideoViewListener.onPlayBackCompleted(mMockVideoView);

        verify(mMockListener).onMediaPlaybackFinished(mMediaView);
    }

    @Test
    public void onPlaybackPaused_onMediaPlaybackPaused() {
        mVideoViewListener.onPlaybackPaused();

        verify(mMockListener).onMediaPlaybackPaused(mMediaView);
    }

    @Test
    public void onPlaybackResumed_onMediaPlaybackResumed() {
        mVideoViewListener.onPlaybackResumed();

        verify(mMockListener).onMediaPlaybackResumed(mMediaView);
    }

    @Test
    public void onVideoMuted_onMediaPlaybackMuted() {
        mVideoViewListener.onVideoMuted();

        verify(mMockListener).onMediaPlaybackMuted(mMediaView);

    }

    @Test
    public void onVideoUnMuted_onMediaPlaybackUnMuted() {
        mVideoViewListener.onVideoUnMuted();

        verify(mMockListener).onMediaPlaybackUnMuted(mMediaView);
    }

    // endregion ================= VideoViewListener tests

    @Test
    public void loadMedia_VideoViewLoadAd() {
        mMediaView.loadMedia(new MediaData());

        verify(mMockVideoView).loadAd(any(), anyString());
    }

    @Test
    public void setAutoPlay_VideoViewChangeAutoPlayState() {
        mMediaView.setAutoPlay(true);
        verify(mMockVideoView).setAutoPlay(true);

        mMediaView.setAutoPlay(false);
        verify(mMockVideoView).setAutoPlay(false);
    }

    @Test
    public void play_InvokeVideoView() {
        mMediaView.play();

        verify(mMockVideoView).play();
    }

    @Test
    public void pause_InvokeVideoView() {
        mMediaView.pause();

        verify(mMockVideoView).pause();
    }

    @Test
    public void resume_InvokeVideoView() {
        mMediaView.resume();

        verify(mMockVideoView).resume();
    }

    @Test
    public void mute_InvokeVideoView() {
        mMediaView.mute();

        verify(mMockVideoView).mute(true);
    }

    @Test
    public void unMute_InvokeVideoView() {
        mMediaView.unMute();

        verify(mMockVideoView).mute(false);
    }

    @Test
    public void destroy_Cleanup() {
        mMediaView.destroy();

        verify(mMockVideoView).destroy();
    }
}