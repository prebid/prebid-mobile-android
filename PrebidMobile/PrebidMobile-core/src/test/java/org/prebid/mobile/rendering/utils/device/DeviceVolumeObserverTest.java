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

package org.prebid.mobile.rendering.utils.device;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static android.media.AudioManager.STREAM_MUSIC;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class DeviceVolumeObserverTest {

    private static final boolean IRRELEVANT_SELF_CHANGE = true;

    @Mock
    Handler mockHandler;
    @Mock
    Context mockContext;
    @Mock
    ContentResolver mockContentResolver;
    @Mock
    DeviceVolumeObserver.DeviceVolumeListener mockDeviceVolumeListener;
    @Mock
    AudioManager mockAudioManager;

    private DeviceVolumeObserver spyDeviceVolumeObserver;

    @Before
    public void setupMocks() {
        MockitoAnnotations.initMocks(this);

        when(mockContext.getSystemService(Context.AUDIO_SERVICE)).thenReturn(mockAudioManager);
        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);

        spyDeviceVolumeObserver = spy(new DeviceVolumeObserver(mockContext, mockHandler, mockDeviceVolumeListener));
    }

    @Test
    public void start_NotifyListenerWithCurrentDeviceVolume() {
        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);
        when(mockAudioManager.getStreamVolume(STREAM_MUSIC)).thenReturn(100);
        when(mockAudioManager.getStreamMaxVolume(STREAM_MUSIC)).thenReturn(100);
        when(spyDeviceVolumeObserver.convertDeviceVolume(100, 100)).thenReturn(1f);

        spyDeviceVolumeObserver.start();

        verify(mockAudioManager, times(1)).getStreamVolume(STREAM_MUSIC);
        verify(mockAudioManager, times(1)).getStreamMaxVolume(STREAM_MUSIC);
        verify(spyDeviceVolumeObserver, times(1)).convertDeviceVolume(100, 100);
        verify(mockDeviceVolumeListener, times(1)).onDeviceVolumeChanged(1f);
    }

    @Test
    public void start_RegisterContentObserver() {
        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);
        when(mockAudioManager.getStreamVolume(STREAM_MUSIC)).thenReturn(100);
        when(mockAudioManager.getStreamMaxVolume(STREAM_MUSIC)).thenReturn(100);
        when(spyDeviceVolumeObserver.convertDeviceVolume(100, 100)).thenReturn(1f);

        spyDeviceVolumeObserver.start();

        verify(mockContext, times(1)).getContentResolver();
    }

    @Test
    public void stop_UnregisterContentObserver() {
        when(mockAudioManager.getStreamVolume(STREAM_MUSIC)).thenReturn(100);
        when(mockAudioManager.getStreamMaxVolume(STREAM_MUSIC)).thenReturn(100);
        when(spyDeviceVolumeObserver.convertDeviceVolume(100, 100)).thenReturn(1f);

        spyDeviceVolumeObserver.stop();

        verify(mockContext, times(1)).getContentResolver();
    }

    @Test
    public void onChangeWithDeviceVolumeChanged_NotifyListener() {
        when(mockAudioManager.getStreamVolume(STREAM_MUSIC)).thenReturn(100, 50);
        when(mockAudioManager.getStreamMaxVolume(STREAM_MUSIC)).thenReturn(100, 100);
        when(spyDeviceVolumeObserver.convertDeviceVolume(100, 100)).thenReturn(1f);
        when(spyDeviceVolumeObserver.convertDeviceVolume(50, 100)).thenReturn(0.5f);

        spyDeviceVolumeObserver.onChange(IRRELEVANT_SELF_CHANGE);
        spyDeviceVolumeObserver.onChange(IRRELEVANT_SELF_CHANGE);

        verify(mockAudioManager, times(2)).getStreamVolume(STREAM_MUSIC);
        verify(mockAudioManager, times(2)).getStreamMaxVolume(STREAM_MUSIC);
        verify(spyDeviceVolumeObserver, times(1)).convertDeviceVolume(100, 100);
        verify(mockDeviceVolumeListener, times(1)).onDeviceVolumeChanged(1f);
        verify(spyDeviceVolumeObserver, times(1)).convertDeviceVolume(50, 100);
        verify(mockDeviceVolumeListener, times(1)).onDeviceVolumeChanged(0.5f);
    }

    @Test
    public void onChangeWithDeviceVolumeNotChanged_NoNotifyListener() {
        when(mockAudioManager.getStreamVolume(STREAM_MUSIC)).thenReturn(100);
        when(mockAudioManager.getStreamMaxVolume(STREAM_MUSIC)).thenReturn(100);
        when(spyDeviceVolumeObserver.convertDeviceVolume(100, 100)).thenReturn(1f);

        spyDeviceVolumeObserver.onChange(IRRELEVANT_SELF_CHANGE);
        spyDeviceVolumeObserver.onChange(IRRELEVANT_SELF_CHANGE);

        verify(mockAudioManager, times(2)).getStreamVolume(STREAM_MUSIC);
        verify(mockAudioManager, times(2)).getStreamMaxVolume(STREAM_MUSIC);
        verify(spyDeviceVolumeObserver, times(2)).convertDeviceVolume(100, 100);
        verify(mockDeviceVolumeListener, times(1)).onDeviceVolumeChanged(1f);
    }
}