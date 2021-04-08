package org.prebid.mobile.rendering.video;

import android.content.Context;

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AbstractCreative;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.video.vast.VASTInterface;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

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
