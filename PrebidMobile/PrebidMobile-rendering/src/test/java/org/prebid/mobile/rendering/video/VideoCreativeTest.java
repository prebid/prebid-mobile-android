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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.listeners.CreativeViewListener;
import org.prebid.mobile.rendering.models.AbstractCreative;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class VideoCreativeTest {

    private Context mContext;
    @Mock
    private VideoCreativeModel mMockModel;
    @Mock
    private OmAdSessionManager mMockOmAdSessionManager;
    @Mock
    private InterstitialManager mMockInterstitialManager;
    @Mock
    private VideoCreativeView mMockVideoCreativeView;

    private VideoCreative mVideoCreative;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mContext = Robolectric.buildActivity(Activity.class).create().get();

        AdConfiguration mockConfig = mock(AdConfiguration.class);
        when(mMockModel.getAdConfiguration()).thenReturn(mockConfig);

        mVideoCreative = new VideoCreative(mContext, mMockModel, mMockOmAdSessionManager, mMockInterstitialManager);
        mVideoCreative.mVideoCreativeView = mMockVideoCreativeView;
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void displayTest() throws Exception {
        VideoCreative spyVideoCreative = spy(mVideoCreative);

        spyVideoCreative.display();

        verify(mVideoCreative.mVideoCreativeView).start(anyFloat());
    }

    @Test
    public void initNativeAdSessionManagerSuccessTest() throws Exception {
        mVideoCreative.mVideoCreativeView = new VideoCreativeView(mContext, mVideoCreative);
        VideoCreative spyVideoCreative = spy(mVideoCreative);

        spyVideoCreative.createOmAdSession();

        verify(mMockOmAdSessionManager).initVideoAdSession(any(), any());
    }

    @Test
    public void initNativeAdSessionManagerFailureTest() {
        mVideoCreative.mVideoCreativeView = null;
        VideoCreative spyVideoCreative = Mockito.spy(mVideoCreative);

        spyVideoCreative.createOmAdSession();

        verify(mMockOmAdSessionManager, never()).registerAdView(any());
        verify(mMockOmAdSessionManager, never()).startAdSession();
    }

    @Test
    public void trackAdVideoEventTest() throws Exception {
        CreativeViewListener mockCreativeViewListener = mock(CreativeViewListener.class);
        VideoPlayerView mockView = mock(VideoPlayerView.class);
        mVideoCreative.setCreativeViewListener(mockCreativeViewListener);
        when(mockView.getDuration()).thenReturn(0);
        when(mockView.getVolume()).thenReturn(0f);
        when(mMockVideoCreativeView.hasVideoStarted()).thenReturn(true);
        when(mMockVideoCreativeView.isPlaying()).thenReturn(true);
        when(mMockVideoCreativeView.getVideoPlayerView()).thenReturn(mockView);

        mVideoCreative.complete();
        verify(mMockModel).trackVideoEvent(VideoAdEvent.Event.AD_COMPLETE);
        mockCreativeViewListener.creativeDidComplete(mVideoCreative);

        mVideoCreative.onVolumeChanged(1);
        verify(mMockOmAdSessionManager).trackVolumeChange(1);

        mVideoCreative.onPlayerStateChanged(InternalPlayerState.NORMAL);
        verify(mMockModel).trackPlayerStateChange(InternalPlayerState.NORMAL);

        mVideoCreative.skip();
        verify(mMockModel).trackVideoEvent(VideoAdEvent.Event.AD_SKIP);
        mockCreativeViewListener.creativeDidComplete(mVideoCreative);

        mVideoCreative.onEvent(VideoAdEvent.Event.AD_RESUME);
        verify(mMockModel).trackVideoEvent(VideoAdEvent.Event.AD_RESUME);

        mVideoCreative.onEvent(VideoAdEvent.Event.AD_PAUSE);
        verify(mMockModel).trackVideoEvent(VideoAdEvent.Event.AD_PAUSE);

        mVideoCreative.onEvent(VideoAdEvent.Event.AD_UNMUTE);
        verify(mMockModel).trackVideoEvent(VideoAdEvent.Event.AD_UNMUTE);

        mVideoCreative.onEvent(VideoAdEvent.Event.AD_MUTE);
        verify(mMockModel).trackVideoEvent(VideoAdEvent.Event.AD_MUTE);

        mVideoCreative.onEvent(VideoAdEvent.Event.AD_FULLSCREEN);
        verify(mMockModel).trackVideoEvent(VideoAdEvent.Event.AD_FULLSCREEN);

        mVideoCreative.onEvent(VideoAdEvent.Event.AD_EXITFULLSCREEN);
        verify(mMockModel).trackVideoEvent(VideoAdEvent.Event.AD_EXITFULLSCREEN);

        mVideoCreative.onEvent(VideoAdEvent.Event.AD_START);
        verify(mMockModel).trackVideoEvent(VideoAdEvent.Event.AD_START);

        mVideoCreative.mVideoCreativeView = null;
        mVideoCreative.onEvent(VideoAdEvent.Event.AD_START);
        verify(mMockModel, atLeastOnce()).trackVideoEvent(VideoAdEvent.Event.AD_START);
    }

    @Test
    public void destroyTest() throws Exception {
        VideoDownloadTask mockVideoDownloadTask = mock(VideoDownloadTask.class);
        WhiteBox.field(VideoCreative.class, "mVideoDownloadTask").set(mVideoCreative, mockVideoDownloadTask);

        mVideoCreative.destroy();

        verify(mMockVideoCreativeView).destroy();
        verify(mockVideoDownloadTask).cancel(true);
    }

    @Test
    public void loadTest() throws Exception {
        VideoCreativeModel mockModel = mock(VideoCreativeModel.class);
        when(mockModel.getMediaUrl()).thenReturn("/video.mp4");
        AdConfiguration mockAdConfig = mock(AdConfiguration.class);
        when(mockModel.getAdConfiguration()).thenReturn(mockAdConfig);
        WhiteBox.field(VideoCreative.class, "mModel").set(mVideoCreative, mockModel);

        mVideoCreative.load();
        assertNotNull(WhiteBox.getInternalState(mVideoCreative, "mVideoDownloadTask"));
    }

    @Test
    public void getVideoDurationTest() {
        long duration = 15 * 1000;
        when(mMockModel.getMediaDuration()).thenReturn(duration);
        assertEquals(15000, mVideoCreative.getMediaDuration());
    }

    @Test
    public void mute_volumeZero_DoNothing() {
        when(mMockVideoCreativeView.getVolume()).thenReturn(0f);

        mVideoCreative.mute();

        verify(mMockVideoCreativeView, never()).mute();
    }

    @Test
    public void mute_volumeNotZero_MuteVideoCreativeView() {
        when(mMockVideoCreativeView.getVolume()).thenReturn(1f);

        mVideoCreative.mute();

        verify(mMockVideoCreativeView).mute();
    }

    @Test
    public void unmute_volumeZero_UnMuteVideoCreativeView() {
        when(mMockVideoCreativeView.getVolume()).thenReturn(0f);

        mVideoCreative.unmute();

        verify(mMockVideoCreativeView).unMute();
    }

    @Test
    public void unmute_volumeNotZero_DoNothing() {
        when(mMockVideoCreativeView.getVolume()).thenReturn(1f);

        mVideoCreative.unmute();

        verify(mMockVideoCreativeView, never()).unMute();
    }

    @Test
    public void getCreativeModelTest() {
        assertEquals(mMockModel, mVideoCreative.getCreativeModel());
    }

    @Test
    public void videoAdLoaded_TrackEvent() {
        mVideoCreative.trackAdLoaded();
        verify(mMockModel).trackNonSkippableStandaloneVideoLoaded(false);
    }

    @Test
    public void whenOnCallToAction_CreativeViewListenerCreativeWasClicked() {
        CreativeViewListener mockListener = mock(CreativeViewListener.class);
        when(mMockVideoCreativeView.getCallToActionUrl()).thenReturn("url");
        mVideoCreative.setCreativeViewListener(mockListener);

        mVideoCreative.onEvent(VideoAdEvent.Event.AD_CLICK);

        verify(mockListener).creativeWasClicked(any(AbstractCreative.class), eq("url"));
    }

    @Test
    public void whenOnVideoInterstitialClosed_DestroyCreativeViewAndCallCreativeDidComplete() {
        CreativeViewListener mockListener = mock(CreativeViewListener.class);

        mVideoCreative.setCreativeViewListener(mockListener);

        mVideoCreative.onVideoInterstitialClosed();
        verify(mMockVideoCreativeView).destroy();
        verify(mockListener).creativeDidComplete(any(AbstractCreative.class));
    }
}