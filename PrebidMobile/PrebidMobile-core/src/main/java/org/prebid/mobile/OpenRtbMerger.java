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

    /**
     * Removes the fields that the SDK computes itself, so that the publisher provided
     * global ORTB config can't override them.
     * <p>
     * Each list is applied at the level where the SDK actually writes the field, i.e.
     * {@code regs.ext.gdpr} is stripped from {@code regs.ext} and not from {@code regs}.
     */
    private static void removeSensitiveData(@NonNull JSONObject openRtbJson) {
        removeFieldsWithExt(openRtbJson.optJSONObject("regs"), FIELDS_REGS, FIELDS_REGS_EXT);
        removeFieldsWithExt(openRtbJson.optJSONObject("user"), FIELDS_USER, FIELDS_USER_EXT);

        // "device" has no separate ext list, FIELDS_DEVICE drops the whole "device.ext" object.
        removeFields(openRtbJson.optJSONObject("device"), FIELDS_DEVICE);
    }

    private static void removeFieldsWithExt(@Nullable JSONObject json, String[] fields, String[] extFields) {
        if (json == null) return;

        removeFields(json, fields);
        removeFields(json.optJSONObject("ext"), extFields);
    }

    private static void removeFields(@Nullable JSONObject json, String... fields) {
        if (json == null) return;

        for (String field : fields) {
            json.remove(field);
        }
    }


    private static final String[] FIELDS_USER = {
            "geo"
    };

    private static final String[] FIELDS_USER_EXT = {
            "consent"
    };

    private static final String[] FIELDS_REGS = {
            "coppa",
            "gpp",
            "gpp_sid"
    };

    /**
     * Note: "tfua" is intentionally not protected. The global ORTB config is the only way
     * for publishers to send it (see issue #997).
     */
    private static final String[] FIELDS_REGS_EXT = {
            "gdpr",
            "us_privacy"
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
