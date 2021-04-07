package com.openx.apollo.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.openx.apollo.R;

public class VolumeControlView extends ImageView {
    private VolumeState mVolumeState = VolumeState.MUTED;
    private VolumeControlListener mVolumeControlListener;

    public VolumeControlView(Context context, VolumeState initialState) {
        super(context);
        updateVolumeState(initialState);
        init();
    }

    public VolumeControlView(Context context,
                             @Nullable
                                 AttributeSet attrs) {
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
        mVolumeControlListener = volumeControlListener;
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
            if (mVolumeState == VolumeState.MUTED) {
                unMute();
            }
            else {
                mute();
            }
        });
    }

    private void updateVolumeState(VolumeState volumeState) {
        mVolumeState = volumeState;

        updateIcon(mVolumeState);

        if (mVolumeControlListener != null) {
            mVolumeControlListener.onStateChange(mVolumeState);
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
