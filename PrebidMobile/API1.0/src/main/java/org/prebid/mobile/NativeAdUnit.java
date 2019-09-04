package org.prebid.mobile;

import android.support.annotation.NonNull;
import android.text.TextUtils;

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

    HashMap<String, Object> requestConfig = new HashMap<>();

    public enum CONTEXT_TYPE {
        CONTENT_CENTRIC(1),
        SOCIAL_CENTRIC(2),
        PRODUCT(3),
        CUSTOM(500);
        private int id;

        CONTEXT_TYPE(final int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public void setID(int id) {
            if (this.equals(CUSTOM) && id >= 500) {
                this.id = id;
            }
        }
    }

    public void setContextType(CONTEXT_TYPE type) {
        requestConfig.put(CONTEXT, type.getID());
    }

    public enum CONTEXTSUBTYPE {
        GENERAL(10),
        ARTICAL(11),
        VIDEO(12),
        AUDIO(13),
        IMAGE(14),
        USER_GENERATED(15),
        GENERAL_SOCIAL(20),
        EMAIL(21),
        CHAT_IM(22),
        SELLING(30),
        APPLICATION_STORE(31),
        PRODUCT_REVIEW_SITES(32),
        CUSTOM(500);
        private int id;

        CONTEXTSUBTYPE(final int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public void setID(int id) {
            if (this.equals(CUSTOM) && id >= 500) {
                this.id = id;
            }
        }
    }

    public void setContextSubType(CONTEXTSUBTYPE type) {
        requestConfig.put(CONTEXT_SUB_TYPE, type.getID());
    }

    public enum PLACEMENTTYPE {
        CONTENT_FEED(1),
        CONTENT_ATOMIC_UNIT(2),
        OUTSIDE_CORE_CONTENT(3),
        RECOMMENDATION_WIDGET(4),
        CUSTOM(500);
        private int id;

        PLACEMENTTYPE(final int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public void setID(int id) {
            if (this.equals(CUSTOM) && id >= 500) {
                this.id = id;
            }
        }

    }

    public void setPlacementType(PLACEMENTTYPE placementType) {
        requestConfig.put(PLACEMENT_TYPE, placementType.getID());
    }

    public void setPlacementCount(Integer placementCount) {
        requestConfig.put(PLACEMENT_COUNT, placementCount);
    }

    public void setSeq(Integer seq) {
        requestConfig.put(SEQ, seq);
    }

    public void setAUrlSupport(boolean support) {
        if (support) {
            requestConfig.put(A_URL_SUPPORT, 1);
        } else {
            requestConfig.put(A_URL_SUPPORT, 0);
        }
    }

    public void setDUrlSupport(boolean support) {
        if (support) {
            requestConfig.put(D_URL_SUPPORT, 1);
        } else {
            requestConfig.put(D_URL_SUPPORT, 0);
        }
    }

    public void setPrivacy(boolean privacy) {
        if (privacy) {
            requestConfig.put(PRIVACY, 1);
        } else {
            requestConfig.put(PRIVACY, 0);
        }
    }

    public void setExt(Object jsonObject) {
        if (jsonObject instanceof JSONObject || jsonObject instanceof JSONArray) {
            requestConfig.put(EXT, jsonObject);
        }
    }

    public enum EVENT_TYPE {
        IMPRESSION(1),
        VIEWABLE_MRC50(2),
        VIEWABLE_MRC100(3),
        VIEWABLE_VIDEO50(4),
        CUSTOM(500);
        private int id;

        EVENT_TYPE(final int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public void setID(int id) {
            if (this.equals(CUSTOM) && id >= 500) {
                this.id = id;
            }
        }
    }

    public enum EVENT_TRACKING_METHOD {
        IMAGE(1),
        JS(2),
        CUSTOM(500);
        private int id;

        EVENT_TRACKING_METHOD(final int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public void setID(int id) {
            if (this.equals(CUSTOM) && id >= 500) {
                this.id = id;
            }
        }
    }

    public void addEventTracker(EVENT_TYPE event, ArrayList<EVENT_TRACKING_METHOD> methods, Object extObject) throws Exception {
        if (methods == null || methods.isEmpty()) {
            throw new Exception("Methods are required");
        }
        JSONArray eventTrackers = (JSONArray) requestConfig.get(EVENT_TRACKERS);
        if (eventTrackers == null) {
            eventTrackers = new JSONArray();
        }
        JSONObject eventTracker = new JSONObject();
        eventTracker.put(EVENT, event.getID());
        JSONArray methodsJSONArray = new JSONArray();
        for (EVENT_TRACKING_METHOD method : methods) {
            methodsJSONArray.put(method.getID());
        }
        eventTracker.put(METHODS, methodsJSONArray);
        if (extObject instanceof JSONArray || extObject instanceof JSONObject) {
            eventTracker.put(EXT, extObject);
        }
        eventTrackers.put(eventTracker);
        requestConfig.put(EVENT_TRACKERS, eventTrackers);
    }

    public void addTitle(Integer len, Boolean required, Object assetExt, Object titleExt) {
        HashMap<NATIVE_REQUEST_ASSET, ArrayList<HashMap<String, Object>>> assets = (HashMap<NATIVE_REQUEST_ASSET, ArrayList<HashMap<String, Object>>>) requestConfig.get("assets");
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
        ArrayList<HashMap<String, Object>> assetParams = assets.get(NATIVE_REQUEST_ASSET.DATA);
        if (assetParams == null) {
            assetParams = new ArrayList<>();
        }
        assetParams.clear();
        assetParams.add(params);
        assets.put(NATIVE_REQUEST_ASSET.TITLE, assetParams);
        requestConfig.put(ASSETS, assets);
    }

    public enum IMAGE_TYPE {
        ICON(1),
        MAIN(3),
        CUSTOM(500);
        private int id;

        IMAGE_TYPE(final int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public void setID(int id) {
            if (this.equals(CUSTOM) && id >= 500) {
                this.id = id;
            }
        }
    }

    public void addImage(IMAGE_TYPE type, Integer wmin, Integer hmin, Integer w, Integer h, ArrayList<String> mimes, Boolean required, Object assetExt, Object imageExt) {
        HashMap<NATIVE_REQUEST_ASSET, ArrayList<HashMap<String, Object>>> assets = (HashMap<NATIVE_REQUEST_ASSET, ArrayList<HashMap<String, Object>>>) requestConfig.get("assets");
        if (assets == null) {
            assets = new HashMap<>();
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put(WIDTH_MIN, wmin);
        params.put(HEIGHT_MIN, hmin);
        params.put(HEIGHT, h);
        params.put(WIDTH, w);
        params.put(REQUIRED, required);
        params.put(TYPE, type.getID());
        if (mimes != null) {
            String mimesString = TextUtils.join(",", mimes);
            params.put(MIMES, mimesString);
        }
        if (assetExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put(ASSETS_EXT, assetExt);
        }
        if (imageExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put(EXT, imageExt);
        }
        ArrayList<HashMap<String, Object>> assetParams = assets.get(NATIVE_REQUEST_ASSET.DATA);
        if (assetParams == null) {
            assetParams = new ArrayList<>();
        }
        assetParams.add(params);
        assets.put(NATIVE_REQUEST_ASSET.IMAGE, assetParams);
        requestConfig.put(ASSETS, assets);
    }

    public enum DATA_TYPE {
        SPONSORED(1),
        DESC(2),
        RATING(3),
        LIKES(4),
        DOWNLOADS(5),
        PRICE(6),
        SALEPRICE(7),
        PHONE(8),
        ADDRESS(9),
        DESC2(10),
        DESPLAYURL(11),
        CTATEXT(12),
        CUSTOM(500);
        private int id;

        DATA_TYPE(final int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public void setID(int id) {
            if (this.equals(CUSTOM) && id >= 500) {
                this.id = id;
            }
        }
    }

    public void addData(DATA_TYPE type, Integer len, Boolean required, Object assetExt, Object dataExt) {
        HashMap<NATIVE_REQUEST_ASSET, ArrayList<HashMap<String, Object>>> assets = (HashMap<NATIVE_REQUEST_ASSET, ArrayList<HashMap<String, Object>>>) requestConfig.get("assets");
        if (assets == null) {
            assets = new HashMap<>();
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put(LENGTH, len);
        params.put(REQUIRED, required);
        params.put(TYPE, type.getID());

        if (assetExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put(ASSETS_EXT, assetExt);
        }
        if (dataExt instanceof JSONArray || assetExt instanceof JSONObject) {
            params.put(EXT, dataExt);
        }
        ArrayList<HashMap<String, Object>> assetParams = assets.get(NATIVE_REQUEST_ASSET.DATA);
        if (assetParams == null) {
            assetParams = new ArrayList<>();
        }
        assetParams.add(params);
        assets.put(NATIVE_REQUEST_ASSET.DATA, assetParams);
        requestConfig.put(ASSETS, assets);
    }
}
