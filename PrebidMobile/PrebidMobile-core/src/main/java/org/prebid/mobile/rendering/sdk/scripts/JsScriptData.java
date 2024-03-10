package org.prebid.mobile.rendering.sdk.scripts;

import androidx.annotation.NonNull;

public class JsScriptData {

    public static final JsScriptData openMeasurementData = new JsScriptData(
            "PBMJSLibraries/omsdk.js",
            "https://cdn.jsdelivr.net/gh/prebid/prebid-mobile-android@master/scripts/js/omsdk_v1.js"
    );
    public static final JsScriptData mraidData = new JsScriptData(
            "PBMJSLibraries/mraid.js",
            "https://cdn.jsdelivr.net/gh/prebid/prebid-mobile-android@master/scripts/js/mraid.js"
    );

    @NonNull
    private final String path;
    @NonNull
    private final String url;

    private JsScriptData(@NonNull String path, @NonNull String url) {
        this.path = path;
        this.url = url;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

}
