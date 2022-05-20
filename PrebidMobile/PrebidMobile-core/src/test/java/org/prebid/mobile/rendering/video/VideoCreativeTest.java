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
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.rendering.listeners.CreativeViewListener;
import org.prebid.mobile.rendering.models.AbstractCreative;
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
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class VideoCreativeTest {

    private Context context;
    @Mock private VideoCreativeModel mockModel;
    @Mock private OmAdSessionManager mockOmAdSessionManager;
    @Mock private InterstitialManager mockInterstitialManager;
    @Mock private VideoCreativeView mockVideoCreativeView;

    private VideoCreative videoCreative;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        context = Robolectric.buildActivity(Activity.class).create().get();

        AdUnitConfiguration mockConfig = mock(AdUnitConfiguration.class);
        when(mockModel.getAdConfiguration()).thenReturn(mockConfig);

        videoCreative = new VideoCreative(context, mockModel, mockOmAdSessionManager, mockInterstitialManager);
        videoCreative.videoCreativeView = mockVideoCreativeView;
    }

    @After
    public void tearDown() throws Exception {
        videoCreative.destroy();
    }

    @Test
    public void displayTest() throws Exception {
        VideoCreative spyVideoCreative = spy(videoCreative);

        spyVideoCreative.display();

        verify(videoCreative.videoCreativeView).start(anyFloat());
    }

    @Test
    public void initNativeAdSessionManagerSuccessTest() throws Exception {
        videoCreative.videoCreativeView = new VideoCreativeView(context, videoCreative);
        VideoCreative spyVideoCreative = spy(videoCreative);

        spyVideoCreative.createOmAdSession();

        verify(mockOmAdSessionManager).initVideoAdSession(any(), any());
    }

    @Test
    public void initNativeAdSessionManagerFailureTest() {
        videoCreative.videoCreativeView = null;
        VideoCreative spyVideoCreative = Mockito.spy(videoCreative);

        spyVideoCreative.createOmAdSession();

        verify(mockOmAdSessionManager, never()).registerAdView(any());
        verify(mockOmAdSessionManager, never()).startAdSession();
    }

    @Test
    public void trackAdVideoEventTest() throws Exception {
        CreativeViewListener mockCreativeViewListener = mock(CreativeViewListener.class);
        VideoPlayerView mockView = mock(VideoPlayerView.class);
        videoCreative.setCreativeViewListener(mockCreativeViewListener);
        when(mockView.getDuration()).thenReturn(0);
        when(mockView.getVolume()).thenReturn(0f);
        when(mockVideoCreativeView.hasVideoStarted()).thenReturn(true);
        when(mockVideoCreativeView.isPlaying()).thenReturn(true);
        when(mockVideoCreativeView.getVideoPlayerView()).thenReturn(mockView);

        videoCreative.complete();
        verify(mockModel).trackVideoEvent(VideoAdEvent.Event.AD_COMPLETE);
        mockCreativeViewListener.creativeDidComplete(videoCreative);

        videoCreative.onVolumeChanged(1);
        verify(mockOmAdSessionManager).trackVolumeChange(1);

        videoCreative.onPlayerStateChanged(InternalPlayerState.NORMAL);
        verify(mockModel).trackPlayerStateChange(InternalPlayerState.NORMAL);

        videoCreative.skip();
        verify(mockModel).trackVideoEvent(VideoAdEvent.Event.AD_SKIP);
        mockCreativeViewListener.creativeDidComplete(videoCreative);

        videoCreative.onEvent(VideoAdEvent.Event.AD_RESUME);
        verify(mockModel).trackVideoEvent(VideoAdEvent.Event.AD_RESUME);

        videoCreative.onEvent(VideoAdEvent.Event.AD_PAUSE);
        verify(mockModel).trackVideoEvent(VideoAdEvent.Event.AD_PAUSE);

        videoCreative.onEvent(VideoAdEvent.Event.AD_UNMUTE);
        verify(mockModel).trackVideoEvent(VideoAdEvent.Event.AD_UNMUTE);

        videoCreative.onEvent(VideoAdEvent.Event.AD_MUTE);
        verify(mockModel).trackVideoEvent(VideoAdEvent.Event.AD_MUTE);

        videoCreative.onEvent(VideoAdEvent.Event.AD_FULLSCREEN);
        verify(mockModel).trackVideoEvent(VideoAdEvent.Event.AD_FULLSCREEN);

        videoCreative.onEvent(VideoAdEvent.Event.AD_EXITFULLSCREEN);
        verify(mockModel).trackVideoEvent(VideoAdEvent.Event.AD_EXITFULLSCREEN);

        videoCreative.onEvent(VideoAdEvent.Event.AD_START);
        verify(mockModel).trackVideoEvent(VideoAdEvent.Event.AD_START);

        videoCreative.videoCreativeView = null;
        videoCreative.onEvent(VideoAdEvent.Event.AD_START);
        verify(mockModel, atLeastOnce()).trackVideoEvent(VideoAdEvent.Event.AD_START);
    }

    @Test
    public void destroyTest() throws Exception {
        VideoDownloadTask mockVideoDownloadTask = mock(VideoDownloadTask.class);
        WhiteBox.field(VideoCreative.class, "videoDownloadTask").set(videoCreative, mockVideoDownloadTask);

        videoCreative.destroy();

        verify(mockVideoCreativeView).destroy();
        verify(mockVideoDownloadTask).cancel(true);
    }

    @Test
    public void loadTest() throws Exception {
        VideoCreativeModel mockModel = mock(VideoCreativeModel.class);
        when(mockModel.getMediaUrl()).thenReturn("/video.mp4");
        AdUnitConfiguration mockAdConfig = mock(AdUnitConfiguration.class);
        when(mockModel.getAdConfiguration()).thenReturn(mockAdConfig);
        WhiteBox.field(VideoCreative.class, "model").set(videoCreative, mockModel);

        videoCreative.load();
        assertNotNull(WhiteBox.getInternalState(videoCreative, "videoDownloadTask"));
    }

    @Test
    public void getVideoDurationTest() {
        long duration = 15 * 1000;
        when(mockModel.getMediaDuration()).thenReturn(duration);
        assertEquals(15000, videoCreative.getMediaDuration());
    }

    @Test
    public void mute_volumeZero_DoNothing() {
        when(mockVideoCreativeView.getVolume()).thenReturn(0f);

        videoCreative.mute();

        verify(mockVideoCreativeView, never()).mute();
    }

    @Test
    public void mute_volumeNotZero_MuteVideoCreativeView() {
        when(mockVideoCreativeView.getVolume()).thenReturn(1f);

        videoCreative.mute();

        verify(mockVideoCreativeView).mute();
    }

    @Test
    public void unmute_volumeZero_UnMuteVideoCreativeView() {
        when(mockVideoCreativeView.getVolume()).thenReturn(0f);

        videoCreative.unmute();

        verify(mockVideoCreativeView).unMute();
    }

    @Test
    public void unmute_volumeNotZero_DoNothing() {
        when(mockVideoCreativeView.getVolume()).thenReturn(1f);

        videoCreative.unmute();

        verify(mockVideoCreativeView, never()).unMute();
    }

    @Test
    public void getCreativeModelTest() {
        assertEquals(mockModel, videoCreative.getCreativeModel());
    }

    @Test
    public void videoAdLoaded_TrackEvent() {
        videoCreative.trackAdLoaded();
        verify(mockModel).trackNonSkippableStandaloneVideoLoaded(false);
    }

    @Test
    public void whenOnCallToAction_CreativeViewListenerCreativeWasClicked() {
        CreativeViewListener mockListener = mock(CreativeViewListener.class);
        when(mockVideoCreativeView.getCallToActionUrl()).thenReturn("url");
        videoCreative.setCreativeViewListener(mockListener);

        videoCreative.onEvent(VideoAdEvent.Event.AD_CLICK);

        verify(mockListener).creativeWasClicked(any(AbstractCreative.class), eq("url"));
    }

    @Test
    public void whenOnVideoInterstitialClosed_DestroyCreativeViewAndCallCreativeDidComplete() {
        CreativeViewListener mockListener = mock(CreativeViewListener.class);

        videoCreative.setCreativeViewListener(mockListener);

        videoCreative.onVideoInterstitialClosed();
        verify(mockVideoCreativeView).destroy();
        verify(mockListener).creativeDidComplete(any(AbstractCreative.class));
    }

    @Test
    public void whenAdConfigurationMuted_MuteCreative() throws AdException {
        VideoCreativeModel mockModel = mock(VideoCreativeModel.class);
        VideoCreative videoCreative = spy(new VideoCreative(
            context,
            mockModel,
            mockOmAdSessionManager,
            mockInterstitialManager
        ));
        AdUnitConfiguration configuration = new AdUnitConfiguration();
        configuration.setIsMuted(true);

        when(mockModel.getAdConfiguration()).thenReturn(configuration);
        VideoCreativeView videoCreativeView = mock(VideoCreativeView.class);
        Reflection.setVariableTo(videoCreative, "videoCreativeView", videoCreativeView);

        videoCreative.display();

        verify(videoCreativeView).setStartIsMutedProperty(true);
        verify(videoCreative, never()).mute();
        verify(videoCreative, never()).unmute();
    }

    @Test
    public void whenAdConfigurationUnMuted_UnMuteCreative() throws AdException {
        VideoCreativeModel mockModel = mock(VideoCreativeModel.class);
        VideoCreative videoCreative = spy(new VideoCreative(context,
                mockModel,
                mockOmAdSessionManager,
                mockInterstitialManager
        ));
        AdUnitConfiguration configuration = new AdUnitConfiguration();
        configuration.setIsMuted(false);

        VideoCreativeView videoCreativeView = mock(VideoCreativeView.class);
        when(mockModel.getAdConfiguration()).thenReturn(configuration);
        Reflection.setVariableTo(videoCreative, "videoCreativeView", videoCreativeView);

        videoCreative.display();

        verify(videoCreativeView).setStartIsMutedProperty(false);
        verify(videoCreative, never()).unmute();
        verify(videoCreative, never()).mute();
    }

}