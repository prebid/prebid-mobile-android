package com.openx.apollo.video;

import android.content.Context;

import com.openx.apollo.session.manager.OmAdSessionManager;
import com.openx.apollo.views.interstitial.InterstitialManager;

public class RewardedVideoCreative extends VideoCreative {

    private static final String TAG = RewardedVideoCreative.class.getSimpleName();

    public RewardedVideoCreative(Context context, VideoCreativeModel model,
                                 OmAdSessionManager omAdSessionManager,
                                 InterstitialManager interstitialManager) throws Exception {
        super(context, model, omAdSessionManager, interstitialManager);
    }

    @Override
    public void complete() {
        super.complete();
    }
}
