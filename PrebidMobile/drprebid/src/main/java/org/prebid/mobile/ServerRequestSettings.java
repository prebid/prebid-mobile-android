package org.prebid.mobile;

import android.content.Context;

public final class ServerRequestSettings {
    public static String getUserAgent() {
        return PrebidServerSettings.userAgent;
    }

    public static void update(Context context) {
        PrebidServerSettings.update(context);
    }
}
