package org.prebid.mobile.rendering.sdk.scripts;

public class JsScriptData {

    public static final JsScriptData openMeasurementData = new JsScriptData(
            "PBMJSLibraries/omsdk.js",
            "https://cdn.jsdelivr.net/gh/prebid/prebid-mobile-android@master/scripts/js/omsdk_v1.js"
    );
    public static final JsScriptData mraidData = new JsScriptData(
            "PBMJSLibraries/mraid.js",
            "https://cdn.jsdelivr.net/gh/prebid/prebid-mobile-android@master/scripts/js/mraid.js"
    );

    private String path;
    private String url;

    private JsScriptData(String path, String url) {
        this.path = path;
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public String getUrl() {
        return url;
    }

}
