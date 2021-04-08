package org.prebid.mobile.rendering.mraid.methods;

import android.text.TextUtils;

import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.utils.logger.OXLog;

public class MraidPlayVideo {

    private static final String TAG = MraidPlayVideo.class.getSimpleName();

    public void playVideo(String url) {

        if (TextUtils.isEmpty(url)) {
            OXLog.error(TAG, "playVideo(): Failed. Provided url is empty or null");
            return;
        }
        ManagersResolver.getInstance().getDeviceManager().playVideo(url);
    }
}
