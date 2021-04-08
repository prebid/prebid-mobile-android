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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DeviceVolumeObserverTest {

    private static final boolean IRRELEVANT_SELF_CHANGE = true;

    @Mock
    Handler mMockHandler;
    @Mock
    Context mMockContext;
    @Mock
    ContentResolver mMockContentResolver;
    @Mock
    DeviceVolumeObserver.DeviceVolumeListener mMockDeviceVolumeListener;
    @Mock
    AudioManager mMockAudioManager;

    private DeviceVolumeObserver mSpyDeviceVolumeObserver;

    @Before
    public void setupMocks() {
        MockitoAnnotations.initMocks(this);

        when(mMockContext.getSystemService(Context.AUDIO_SERVICE)).thenReturn(mMockAudioManager);
        when(mMockContext.getContentResolver()).thenReturn(mMockContentResolver);

        mSpyDeviceVolumeObserver = spy(new DeviceVolumeObserver(mMockContext, mMockHandler, mMockDeviceVolumeListener));
    }

    @Test
    public void start_NotifyListenerWithCurrentDeviceVolume() {
        when(mMockContext.getContentResolver()).thenReturn(mMockContentResolver);
        when(mMockAudioManager.getStreamVolume(STREAM_MUSIC)).thenReturn(100);
        when(mMockAudioManager.getStreamMaxVolume(STREAM_MUSIC)).thenReturn(100);
        when(mSpyDeviceVolumeObserver.convertDeviceVolume(100, 100)).thenReturn(1f);

        mSpyDeviceVolumeObserver.start();

        verify(mMockAudioManager, times(1)).getStreamVolume(STREAM_MUSIC);
        verify(mMockAudioManager, times(1)).getStreamMaxVolume(STREAM_MUSIC);
        verify(mSpyDeviceVolumeObserver, times(1)).convertDeviceVolume(100, 100);
        verify(mMockDeviceVolumeListener, times(1)).onDeviceVolumeChanged(1f);
    }

    @Test
    public void start_RegisterContentObserver() {
        when(mMockContext.getContentResolver()).thenReturn(mMockContentResolver);
        when(mMockAudioManager.getStreamVolume(STREAM_MUSIC)).thenReturn(100);
        when(mMockAudioManager.getStreamMaxVolume(STREAM_MUSIC)).thenReturn(100);
        when(mSpyDeviceVolumeObserver.convertDeviceVolume(100, 100)).thenReturn(1f);

        mSpyDeviceVolumeObserver.start();

        verify(mMockContext, times(1)).getContentResolver();
    }

    @Test
    public void stop_UnregisterContentObserver() {
        when(mMockAudioManager.getStreamVolume(STREAM_MUSIC)).thenReturn(100);
        when(mMockAudioManager.getStreamMaxVolume(STREAM_MUSIC)).thenReturn(100);
        when(mSpyDeviceVolumeObserver.convertDeviceVolume(100, 100)).thenReturn(1f);

        mSpyDeviceVolumeObserver.stop();

        verify(mMockContext, times(1)).getContentResolver();
    }

    @Test
    public void onChangeWithDeviceVolumeChanged_NotifyListener() {
        when(mMockAudioManager.getStreamVolume(STREAM_MUSIC)).thenReturn(100, 50);
        when(mMockAudioManager.getStreamMaxVolume(STREAM_MUSIC)).thenReturn(100, 100);
        when(mSpyDeviceVolumeObserver.convertDeviceVolume(100, 100)).thenReturn(1f);
        when(mSpyDeviceVolumeObserver.convertDeviceVolume(50, 100)).thenReturn(0.5f);

        mSpyDeviceVolumeObserver.onChange(IRRELEVANT_SELF_CHANGE);
        mSpyDeviceVolumeObserver.onChange(IRRELEVANT_SELF_CHANGE);

        verify(mMockAudioManager, times(2)).getStreamVolume(STREAM_MUSIC);
        verify(mMockAudioManager, times(2)).getStreamMaxVolume(STREAM_MUSIC);
        verify(mSpyDeviceVolumeObserver, times(1)).convertDeviceVolume(100, 100);
        verify(mMockDeviceVolumeListener, times(1)).onDeviceVolumeChanged(1f);
        verify(mSpyDeviceVolumeObserver, times(1)).convertDeviceVolume(50, 100);
        verify(mMockDeviceVolumeListener, times(1)).onDeviceVolumeChanged(0.5f);
    }

    @Test
    public void onChangeWithDeviceVolumeNotChanged_NoNotifyListener() {
        when(mMockAudioManager.getStreamVolume(STREAM_MUSIC)).thenReturn(100);
        when(mMockAudioManager.getStreamMaxVolume(STREAM_MUSIC)).thenReturn(100);
        when(mSpyDeviceVolumeObserver.convertDeviceVolume(100, 100)).thenReturn(1f);

        mSpyDeviceVolumeObserver.onChange(IRRELEVANT_SELF_CHANGE);
        mSpyDeviceVolumeObserver.onChange(IRRELEVANT_SELF_CHANGE);

        verify(mMockAudioManager, times(2)).getStreamVolume(STREAM_MUSIC);
        verify(mMockAudioManager, times(2)).getStreamMaxVolume(STREAM_MUSIC);
        verify(mSpyDeviceVolumeObserver, times(2)).convertDeviceVolume(100, 100);
        verify(mMockDeviceVolumeListener, times(1)).onDeviceVolumeChanged(1f);
    }
}