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

}
