package org.prebid.mobile;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * The OpenRtbMerger class provides functionality to merge a given JSON request object
 * with an OpenRTB string representation.
 */
public class OpenRtbMerger {

    private static final String TAG = "OpenRtbMerger";

    private OpenRtbMerger() {
    }

    /**
     * It merges the SDK originalRequest and OpenRTB string.
     *
     * @param originalRequest original request
     * @param openRtb         OpenRTB string
     * @return merged JSON object
     */
    @NonNull
    public static JSONObject globalMerge(@NonNull JSONObject originalRequest, String openRtb) {
        if (openRtb == null || openRtb.isEmpty()) {
            return originalRequest;
        }

        try {
            JSONObject openRtbJson = new JSONObject(openRtb);
            removeSensitiveData(openRtbJson);
            return merge(originalRequest, openRtbJson);
        } catch (Exception e) {
            LogUtil.error(TAG, "Can't merge OpenRTB config: " + e.getMessage());
        }

        return originalRequest;
    }

    @NonNull
    private static JSONObject merge(JSONObject requestJsonRoot, JSONObject openRtbJsonRoot) throws JSONException {
        for (Iterator<String> it = openRtbJsonRoot.keys(); it.hasNext(); ) {
            String openRtbKey = it.next();
            Object openRtbValue = openRtbJsonRoot.opt(openRtbKey);

            boolean newFieldForRequest = !requestJsonRoot.has(openRtbKey);
            if (newFieldForRequest) {
                requestJsonRoot.put(openRtbKey, openRtbValue);
                continue;
            }

            Object requestValue = requestJsonRoot.opt(openRtbKey);
            if (openRtbValue instanceof JSONObject openRtbJson && requestValue instanceof JSONObject requestJson) {
                merge(requestJson, openRtbJson);
                continue;
            }

            if (openRtbValue instanceof JSONArray openRtbJsonArray && requestValue instanceof JSONArray requestJsonArray) {
                merge(requestJsonArray, openRtbJsonArray, openRtbKey, requestJsonRoot);
                continue;
            }

            requestJsonRoot.put(openRtbKey, openRtbValue);
        }
        return requestJsonRoot;
    }

    private static void merge(JSONArray requestJsonArray, JSONArray openRtbJsonArray, String key, JSONObject parentJson) throws JSONException {
        if (openRtbJsonArray.length() == 0) {
            return;
        }

        if (requestJsonArray.length() == 0) {
            for (int i = 0; i < openRtbJsonArray.length(); i++) {
                requestJsonArray.put(i, openRtbJsonArray.get(i));
            }
            return;
        }

        boolean differentItemTypes = requestJsonArray.opt(0).getClass() != openRtbJsonArray.opt(0).getClass();
        if (differentItemTypes) {
            LogUtil.verbose(TAG, "JSON arrays of different types. Rewriting with OpenRTB values...");
            parentJson.put(key, openRtbJsonArray);
            return;
        }

        for (int i = 0; i < openRtbJsonArray.length(); i++) {
            Object openRtbValue = openRtbJsonArray.get(i);
            requestJsonArray.put(openRtbValue);
        }
    }

    private static void removeSensitiveData(@NonNull JSONObject openRtbJson) {
        JSONObject userJson = openRtbJson.optJSONObject("user");
        JSONObject extJson = userJson != null ? userJson.optJSONObject("ext") : null;
        removeFields(extJson, FIELDS_USER_EXT);

        removeFields(openRtbJson.optJSONObject("regs"), FIELDS_REGS);
        removeFields(openRtbJson.optJSONObject("geo"), FIELDS_GEO);
        removeFields(openRtbJson.optJSONObject("device"), FIELDS_DEVICE);
    }

    private static void removeFields(@Nullable JSONObject json, String... fields) {
        if (json == null) return;

        for (String field : fields) {
            json.remove(field);
        }
    }


    private static final String[] FIELDS_USER_EXT = {
            "consent"
    };

    private static final String[] FIELDS_REGS = {
            "gdpr",
            "us_privacy",
            "coppa"
    };

    private static final String[] FIELDS_GEO = {
            "lat",
            "lon",
            "type",
            "accuracy",
            "lastfix",
            "country",
            "region",
            "regionfips104",
            "metro",
            "city",
            "zip",
            "utcoffset"
    };

    private static final String[] FIELDS_DEVICE = {
            "ua",
            "dnt",
            "lmt",
            "ip",
            "ipv6",
            "devicetype",
            "make",
            "model",
            "os",
            "osv",
            "hwv",
            "flashver",
            "language",
            "carrier",
            "mccmnc",
            "ifa",
            "didsha1",
            "didmd5",
            "dpidsha1",
            "dpidmd5",
            "h",
            "w",
            "ppi",
            "js",
            "connectiontype",
            "pxratio",
            "geo",
            "ext"
    };

}
