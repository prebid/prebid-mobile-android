package com.openx.apollo.video;

import android.content.Context;

import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AbstractCreative;
import com.openx.apollo.models.CreativeModel;
import com.openx.apollo.session.manager.OmAdSessionManager;
import com.openx.apollo.video.vast.VASTInterface;
import com.openx.apollo.views.interstitial.InterstitialManager;

public abstract class VideoCreativeProtocol extends AbstractCreative implements VASTInterface {

    public VideoCreativeProtocol(Context context, CreativeModel model, OmAdSessionManager omAdSessionManager, InterstitialManager interstitialManager)
    throws AdException {
        super(context, model, omAdSessionManager, interstitialManager);
    }

    @Override
    public void display() {
        //This eliminates start()
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void expand() {

    }

    @Override
    public void fullScreen() {

    }

    @Override
    public void collapse() {

    }

    @Override
    public void exitFullScreen() {

    }

    @Override
    public void mute() {

    }

    @Override
    public void unmute() {

    }

    @Override
    public void close() {

    }

    @Override
    public void closeLinear() {

    }

    @Override
    public void skip() {

    }

    @Override
    public void rewind() {

    }

    @Override
    public void touch() {

    }

    @Override
    public void orientationChanged(int orientation) {

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

    }
}
