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

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import androidx.annotation.VisibleForTesting;

import static android.content.Context.AUDIO_SERVICE;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.provider.Settings.System.CONTENT_URI;

public class DeviceVolumeObserver extends ContentObserver {

    private final Context mApplicationContext;
    private final AudioManager mAudioManager;
    private final DeviceVolumeListener mDeviceVolumeListener;

    private Float mStoredDeviceVolume;

    public DeviceVolumeObserver(final Context applicationContext,
                                final Handler handler,
                                final DeviceVolumeListener deviceVolumeListener) {
        super(handler);

        mApplicationContext = applicationContext;
        mAudioManager = (AudioManager) applicationContext.getSystemService(AUDIO_SERVICE);
        mDeviceVolumeListener = deviceVolumeListener;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        final Float currentDeviceVolume = getDeviceVolume();
        if (hasDeviceVolumeChanged(currentDeviceVolume)) {
            mStoredDeviceVolume = currentDeviceVolume;
            notifyDeviceVolumeListener();
        }
    }

    public void start() {
        mStoredDeviceVolume = getDeviceVolume();
        notifyDeviceVolumeListener();

        mApplicationContext.getContentResolver().registerContentObserver(CONTENT_URI, true, this);
    }

    public void stop() {
        mApplicationContext.getContentResolver().unregisterContentObserver(this);
    }

    @VisibleForTesting
    Float convertDeviceVolume(final int deviceVolume, final int maxVolume) {
        if (maxVolume < 0 || deviceVolume < 0) {
            return null;
        }

        float volume = ((float) deviceVolume) / maxVolume;
        if (volume > 1.0f) {
            volume = 1.0f;
        }

        return volume * 100.0f;
    }

    private Float getDeviceVolume() {
        final int deviceVolume = mAudioManager.getStreamVolume(STREAM_MUSIC);
        final int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_MUSIC);
        return convertDeviceVolume(deviceVolume, maxVolume);
    }

    private boolean hasDeviceVolumeChanged(final Float currentDeviceVolume) {
        return currentDeviceVolume == null || !currentDeviceVolume.equals(mStoredDeviceVolume);
    }

    private void notifyDeviceVolumeListener() {
        mDeviceVolumeListener.onDeviceVolumeChanged(mStoredDeviceVolume);
    }

    public interface DeviceVolumeListener {
        void onDeviceVolumeChanged(final Float deviceVolume);
    }
}
