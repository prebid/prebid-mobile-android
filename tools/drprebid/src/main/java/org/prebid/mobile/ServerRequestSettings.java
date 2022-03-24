package org.prebid.mobile;

import android.content.Context;

public final class ServerRequestSettings {
    public static String getUserAgent() throws Exception {
        throw new Exception("Deprecated");
//        return PrebidServerSettings.userAgent;
    }

    public static void update(Context context) throws Exception {
        throw new Exception("Deprecated");
//        PrebidServerSettings.update(context);
    }
}
