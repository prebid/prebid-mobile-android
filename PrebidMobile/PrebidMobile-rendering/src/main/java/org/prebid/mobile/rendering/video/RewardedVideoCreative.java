package org.prebid.mobile.rendering.video;

import android.content.Context;

import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

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
