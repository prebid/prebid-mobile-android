package org.prebid.mobile;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Internal class for comparing parameters in adapters.
 */
public class ParametersMatcher {

    private static final String TAG = "ParametersMatcher";

    public static boolean doParametersMatch(
            @Nullable String serverParametersJsonString,
            @Nullable HashMap<String, String> prebidParameters
    ) {
        HashMap<String, String> serverParameters = jsonStringToHashMap(serverParametersJsonString);
        return doParametersMatch(serverParameters, prebidParameters);
    }

    public static boolean doParametersMatch(
            @Nullable Bundle serverParametersBundle,
            @Nullable HashMap<String, String> prebidParameters
    ) {
        HashMap<String, String> serverParameters = bundleToHashMap(serverParametersBundle);
        return doParametersMatch(serverParameters, prebidParameters);
    }

    public static boolean doParametersMatch(
            @Nullable HashMap<String, String> serverParameters,
            @Nullable HashMap<String, String> prebidParameters
    ) {
        if (serverParameters == null || prebidParameters == null) {
            return false;
        }

        try {
            for (String serverKey : serverParameters.keySet()) {
                if (prebidParameters.containsKey(serverKey)) {
                    String prebidValue = prebidParameters.get(serverKey);
                    String serverValue = serverParameters.get(serverKey);
                    if (prebidValue == null || !prebidValue.equals(serverValue)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        } catch (NullPointerException e) {
            LogUtil.error(TAG, "Null pointer exception");
        }
        return false;
    }

    @Nullable
    private static HashMap<String, String> bundleToHashMap(@Nullable Bundle bundle) {
        if (bundle == null) {
            return null;
        }

        HashMap<String, String> parameters = new HashMap<>();
        if (bundle.size() > 0) {
            for (String bundleKey : bundle.keySet()) {
                String bundleValue = bundle.getString(bundleKey);
                if (bundleValue != null) {
                    parameters.put(bundleKey, bundleValue);
                }
            }
            return parameters;
        }
        return null;
    }

    @Nullable
    private static HashMap<String, String> jsonStringToHashMap(@Nullable String jsonString) {
        if (jsonString == null || jsonString.trim().length() == 0) {
            return null;
        }

        try {
            JSONObject root = new JSONObject(jsonString);
            HashMap<String, String> parameters = new HashMap<>();
            Iterator<String> keys = root.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                parameters.put(key, root.getString(key));
            }
            return parameters;
        } catch (JSONException e) {
            LogUtil.error(TAG, "Can't parse parameters");
            return null;
        }
    }

}
