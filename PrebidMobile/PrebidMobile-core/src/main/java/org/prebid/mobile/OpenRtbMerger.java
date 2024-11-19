package org.prebid.mobile;

import androidx.annotation.NonNull;

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
            removeImpData(openRtbJson);
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

            requestJsonRoot.put(openRtbKey, openRtbValue);
        }
        return requestJsonRoot;
    }

    private static void removeSensitiveData(@NonNull JSONObject openRtbJson) {
        if (openRtbJson.has("regs")) {
            openRtbJson.remove("regs");
        }
        if (openRtbJson.has("geo")) {
            openRtbJson.remove("geo");
        }
        if (openRtbJson.has("device")) {
            openRtbJson.remove("device");
        }

        JSONObject userJson = openRtbJson.optJSONObject("user");
        JSONObject extJson = userJson != null ? userJson.optJSONObject("ext") : null;
        if (extJson != null && extJson.has("consent")) {
            extJson.remove("consent");
        }
    }

    private static void removeImpData(@NonNull JSONObject openRtbJson) {
        if (openRtbJson.has("imp")) {
            openRtbJson.remove("imp");
        }
    }

}
