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

    // constants

    static String VERSION = "ver";
    static String SUPPORTED_VERSION = "1.2";
    static String CONTEXT = "context";
    static String CONTEXT_SUB_TYPE = "contextsubtype";
    static String PLACEMENT_TYPE = "plcmttype";
    static String PLACEMENT_COUNT = "plcmtcnt";
    static String SEQ = "seq";
    static String ASSETS = "assets";
    static String A_URL_SUPPORT = "aurlsupport";
    static String D_URL_SUPPORT = "durlsupport";
    static String EVENT_TRACKERS = "eventtrackers";
    static String PRIVACY = "privacy";
    static String EXT = "ext";
    static String EVENT = "event";
    static String METHODS = "methods";
    static String LENGTH = "len";
    static String REQUIRED = "required";
    static String ASSETS_EXT = "assetExt";
    static String WIDTH_MIN = "wmin";
    static String HEIGHT_MIN = "hmin";
    static String WIDTH = "W";
    static String HEIGHT = "h";
    static String TYPE = "type";
    static String MIMES = "mimes";
    static String TITLE = "title";
    static String IMAGE = "img";
    static String DATA = "data";
    static String NATIVE = "native";
    static String REQUEST = "request";


    public NativeAdUnit(@NonNull String configId) {
        super(configId, AdType.NATIVE);
    }

    HashMap<String, Object> requsetConfig = new HashMap<>();

    public void setContext(Integer contextId) {
        requsetConfig.put(CONTEXT, contextId);
    }

    public void setContextSubType(Integer subTypeId) {
        requsetConfig.put(CONTEXT_SUB_TYPE, subTypeId);
    }

    public void setPlacementType(Integer placementType) {
        requsetConfig.put(PLACEMENT_TYPE, placementType);
    }

    public void setPlacementCount(Integer placementCount) {
        requsetConfig.put(PLACEMENT_COUNT, placementCount);
    }

    public void setSeq(Integer seq) {
        requsetConfig.put(SEQ, seq);
    }

    public void setAUrlSupport(int support) {
        requsetConfig.put(A_URL_SUPPORT, support);
    }

    public void setDUrlsSupport(int support) {
        requsetConfig.put(D_URL_SUPPORT, support);
    }

    public void setPrivacy(int privacy) {
        requsetConfig.put(PRIVACY, privacy);
    }

    public void setExt(Object jsonObject) {
        if (jsonObject instanceof JSONObject || jsonObject instanceof JSONArray) {
            requsetConfig.put(EXT, jsonObject);
        }
    }

    public void addEventTracker(Integer event, ArrayList<Integer> methods, Object extObject) throws Exception {
        if (methods == null || methods.isEmpty()) {
            throw new Exception("Methods are required");
        }
        JSONArray eventTrackers = (JSONArray) requsetConfig.get(EVENT_TRACKERS);
        if (eventTrackers == null) {
            eventTrackers = new JSONArray();
        }
        JSONObject eventTracker = new JSONObject();
        eventTracker.put(EVENT, event);
        JSONArray methodsJSONArray = new JSONArray();
        for (Integer method : methods) {
            methodsJSONArray.put(method);
        }
        eventTracker.put(METHODS, methodsJSONArray);
        if (extObject instanceof JSONArray || extObject instanceof JSONObject) {
            eventTracker.put(EXT, extObject);
        }
        eventTrackers.put(eventTracker);
        requsetConfig.put(EVENT_TRACKERS, eventTrackers);
    }

    public void addTitle(Integer len, Boolean required, Object assetExt, Object titleExt) {
        HashMap<NATIVE_REQUEST_ASSET, HashMap<String, Object>> assets = (HashMap<NATIVE_REQUEST_ASSET, HashMap<String, Object>>) requsetConfig.get("assets");
        if (assets == null) {
            assets = new HashMap<>();
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put(LENGTH, len);

        params.put(REQUIRED, required);
        if (assetExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put(ASSETS_EXT, assetExt);
        }
        if (titleExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put(EXT, titleExt);
        }
        assets.put(NATIVE_REQUEST_ASSET.TITLE, params);
        requsetConfig.put(ASSETS, assets);
    }

    public void addImage(Integer type, Integer wmin, Integer hmin, Integer w, Integer h, ArrayList<String> mimes, Boolean required, Object assetExt, Object imageExt) throws Exception {
        HashMap<NATIVE_REQUEST_ASSET, HashMap<String, Object>> assets = (HashMap<NATIVE_REQUEST_ASSET, HashMap<String, Object>>) requsetConfig.get("assets");
        if (assets == null) {
            assets = new HashMap<>();
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put(WIDTH_MIN, wmin);
        params.put(HEIGHT_MIN, hmin);
        params.put(HEIGHT, h);
        params.put(WIDTH, w);
        params.put(REQUIRED, required);
        if (type == 1 || type == 3) {
            params.put(TYPE, type);
        } else {
            throw new Exception("Unsupported type " + type + " for image");
        }
        if (mimes != null) {
            String mimesString = "";
            for (String mime : mimes) {
                mimesString += mime;
                mimesString += ",";
            }
            params.put(MIMES, mimesString);
        }
        if (assetExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put(ASSETS_EXT, assetExt);
        }
        if (imageExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put(EXT, imageExt);
        }
        assets.put(NATIVE_REQUEST_ASSET.IMAGE, params);
        requsetConfig.put(ASSETS, assets);
    }

    public void addData(Integer type, Integer len, Boolean required, Object assetExt, Object dataExt) throws Exception {
        HashMap<NATIVE_REQUEST_ASSET, HashMap<String, Object>> assets = (HashMap<NATIVE_REQUEST_ASSET, HashMap<String, Object>>) requsetConfig.get("assets");
        if (assets == null) {
            assets = new HashMap<>();
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put(LENGTH, len);
        params.put(REQUIRED, required);
        if (type >= 1 && type <= 12) {
            params.put(TYPE, type);
        } else {
            throw new Exception("Unsupported type for data");
        }

        if (assetExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put(ASSETS_EXT, assetExt);
        }
        if (dataExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put(EXT, dataExt);
        }
        assets.put(NATIVE_REQUEST_ASSET.DATA, params);
        requsetConfig.put(ASSETS, assets);
    }
}
