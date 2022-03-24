package org.prebid.mobile.admob;

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.LogUtil;

import java.util.HashMap;
import java.util.Iterator;

public class ParametersMatcher {

    private static final String TAG = "ParametersMatcher";

    public static boolean doParametersMatch(String serverParameters, HashMap<String, String> prebidParameters) {
        if (isBlank(serverParameters) || prebidParameters == null) {
            return false;
        }

        try {
            JSONObject root = new JSONObject(serverParameters);
            HashMap<String, String> adMobParameters = new HashMap<>();
            Iterator<String> keys = root.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                adMobParameters.put(key, root.getString(key));
            }

            for (String key : adMobParameters.keySet()) {
                if (prebidParameters.containsKey(key)) {
                    String prebidValue = prebidParameters.get(key);
                    String adMobValue = adMobParameters.get(key);
                    if (prebidValue == null || !prebidValue.equals(adMobValue)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        } catch (JSONException e) {
            LogUtil.error(TAG, "Can't parse AdMob parameters");
        } catch (NullPointerException e) {
            LogUtil.error(TAG, "Null pointer exception");
        }

        return false;
    }

    private static boolean isBlank(String string) {
        return string == null || string.trim().length() == 0;
    }

}
