package org.prebid.mobile;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * For details of the configuration of native imps, please check this documentation:
 * https://www.iab.com/wp-content/uploads/2018/03/OpenRTB-Native-Ads-Specification-Final-1.2.pdf
 */
public class NativeAdUnit extends AdUnit {
    enum NATIVE_REQUEST_ASSET {
        TITLE,
        IMAGE,
        DATA
    }

    public NativeAdUnit(@NonNull String configId) {
        super(configId, AdType.NATIVE);
    }

    HashMap<String, Object> requsetConfig = new HashMap<>();

    public void setContext(Integer contextId) {
        requsetConfig.put("context", contextId);
    }

    public void setContextSubType(Integer subTypeId) {
        requsetConfig.put("contextsubtype", subTypeId);
    }

    public void setPlacementType(Integer placementType) {
        requsetConfig.put("plcmttype", placementType);
    }

    public void setPlacementCount(Integer placementCount) {
        requsetConfig.put("plcmtcnt", placementCount);
    }

    public void setSeq(Integer seq) {
        requsetConfig.put("seq", seq);
    }

    public void setAUrlSupport(int support) {
        requsetConfig.put("aurlsupport", support);
    }

    public void setDUrlsSupport(int support) {
        requsetConfig.put("durlsupport", support);
    }

    public void setPrivacy(int privacy) {
        requsetConfig.put("privacy", privacy);
    }

    public void setExt(Object jsonObject) {
        if (jsonObject instanceof JSONObject || jsonObject instanceof JSONArray) {
            requsetConfig.put("ext", jsonObject);
        }
    }

    public void addEventTracker(Integer event, ArrayList<Integer> methods, Object extObject) throws Exception {
        if (methods == null || methods.isEmpty()) {
            throw new Exception("Methods are required");
        }
        JSONArray eventTrackers = (JSONArray) requsetConfig.get("eventtrackers");
        if (eventTrackers == null) {
            eventTrackers = new JSONArray();
        }
        JSONObject eventTracker = new JSONObject();
        eventTracker.put("event", event);
        JSONArray methodsJSONArray = new JSONArray();
        for (Integer method : methods) {
            methodsJSONArray.put(method);
        }
        eventTracker.put("methods", methodsJSONArray);
        if (extObject instanceof JSONArray || extObject instanceof JSONObject) {
            eventTracker.put("ext", extObject);
        }
        eventTrackers.put(eventTracker);
        requsetConfig.put("eventtrackers", eventTrackers);
    }

    public void addTitle(Integer len, Boolean required, Object assetExt, Object titleExt) {
        HashMap<NATIVE_REQUEST_ASSET, HashMap<String, Object>> assets = (HashMap<NATIVE_REQUEST_ASSET, HashMap<String, Object>>) requsetConfig.get("assets");
        if (assets == null) {
            assets = new HashMap<>();
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("len", len);
        params.put("required", required);
        if (assetExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put("assetExt", assetExt);
        }
        if (titleExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put("ext", titleExt);
        }
        assets.put(NATIVE_REQUEST_ASSET.TITLE, params);
        requsetConfig.put("assets", assets);
    }

    public void addImage(Integer type, Integer wmin, Integer hmin, Integer w, Integer h, ArrayList<String> mimes, Boolean required, Object assetExt, Object imageExt) throws Exception {
        HashMap<NATIVE_REQUEST_ASSET, HashMap<String, Object>> assets = (HashMap<NATIVE_REQUEST_ASSET, HashMap<String, Object>>) requsetConfig.get("assets");
        if (assets == null) {
            assets = new HashMap<>();
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("wmin", wmin);
        params.put("hmin", hmin);
        params.put("h", h);
        params.put("w", w);
        params.put("required", required);
        if (type == 1 || type == 3) {
            params.put("type", type);
        } else {
            throw new Exception("Unsupported type " + type + " for image");
        }
        if (mimes != null) {
            String mimesString = "";
            for (String mime : mimes) {
                mimesString += mime;
                mimesString += ",";
            }
            params.put("mimes", mimesString);
        }
        if (assetExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put("assetExt", assetExt);
        }
        if (imageExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put("ext", imageExt);
        }
        assets.put(NATIVE_REQUEST_ASSET.IMAGE, params);
        requsetConfig.put("assets", assets);
    }

    public void addData(Integer type, Integer len, Boolean required, Object assetExt, Object dataExt) throws Exception {
        HashMap<NATIVE_REQUEST_ASSET, HashMap<String, Object>> assets = (HashMap<NATIVE_REQUEST_ASSET, HashMap<String, Object>>) requsetConfig.get("assets");
        if (assets == null) {
            assets = new HashMap<>();
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("len", len);
        params.put("required", required);
        if (type >= 1 && type <= 12) {
            params.put("type", type);
        } else {
            throw new Exception("Unsupported type for data");
        }

        if (assetExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put("assetExt", assetExt);
        }
        if (dataExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put("ext", dataExt);
        }
        assets.put(NATIVE_REQUEST_ASSET.DATA, params);
        requsetConfig.put("assets", assets);
    }
}
