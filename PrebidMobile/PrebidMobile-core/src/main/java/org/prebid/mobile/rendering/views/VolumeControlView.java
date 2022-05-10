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

package org.prebid.mobile.rendering.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import org.prebid.mobile.core.R;


public class VolumeControlView extends ImageView {

    private VolumeState volumeState = VolumeState.MUTED;
    private VolumeControlListener volumeControlListener;

    public VolumeControlView(
            Context context,
            VolumeState initialState
    ) {
        super(context);
        updateVolumeState(initialState);
        init();
    }

    public VolumeControlView(
            Context context,
            @Nullable AttributeSet attrs
    ) {
        super(context, attrs);
        init();
    }

    public VolumeControlView(Context context,
                             @Nullable
                                 AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setVolumeControlListener(VolumeControlListener volumeControlListener) {
        this.volumeControlListener = volumeControlListener;
    }

    public void mute() {
        updateVolumeState(VolumeState.MUTED);
    }

    public void unMute() {
        updateVolumeState(VolumeState.UN_MUTED);
    }

    public void updateIcon(VolumeState volumeState) {
        if (volumeState == VolumeState.MUTED) {
            setImageResource(R.drawable.ic_volume_off);
        }
        else {
            setImageResource(R.drawable.ic_volume_on);
        }
    }

    private void init() {
        setOnClickListener(view -> {
            if (volumeState == VolumeState.MUTED) {
                unMute();
            } else {
                mute();
            }
        });
    }

    private void updateVolumeState(VolumeState volumeState) {
        this.volumeState = volumeState;

        updateIcon(this.volumeState);

        if (volumeControlListener != null) {
            volumeControlListener.onStateChange(this.volumeState);
        }
    }

    public enum VolumeState {
        MUTED,
        UN_MUTED
    }

    public interface VolumeControlListener {

        void onStateChange(VolumeState volumeState);
    }
}
