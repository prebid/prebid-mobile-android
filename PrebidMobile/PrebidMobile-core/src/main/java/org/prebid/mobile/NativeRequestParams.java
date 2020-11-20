package org.prebid.mobile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

class NativeRequestParams {
    //    // constants
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

    private NativeAdUnit.CONTEXT_TYPE contextType;

    void setContextType(NativeAdUnit.CONTEXT_TYPE type) {
        this.contextType = type;
    }

    NativeAdUnit.CONTEXT_TYPE getContextType() {
        return contextType;
    }


    private NativeAdUnit.CONTEXTSUBTYPE contextsubtype;

    void setContextSubType(NativeAdUnit.CONTEXTSUBTYPE type) {
        this.contextsubtype = type;
    }

    NativeAdUnit.CONTEXTSUBTYPE getContextsubtype() {
        return contextsubtype;
    }

    private NativeAdUnit.PLACEMENTTYPE placementtype;

    void setPlacementType(NativeAdUnit.PLACEMENTTYPE placementType) {
        this.placementtype = placementType;
    }

    NativeAdUnit.PLACEMENTTYPE getPlacementType() {
        return placementtype;
    }

    private int placementCount = 1;

    void setPlacementCount(int placementCount) {
        this.placementCount = placementCount;
    }

    int getPlacementCount() {
        return placementCount;
    }

    private int seq = 0;

    void setSeq(int seq) {
        this.seq = seq;
    }

    int getSeq() {
        return seq;
    }

    private boolean aUrlSupport = false;

    void setAUrlSupport(boolean support) {
        this.aUrlSupport = support;
    }

    boolean isAUrlSupport() {
        return aUrlSupport;
    }

    private boolean dUrlSupport = false;

    void setDUrlSupport(boolean support) {
        this.dUrlSupport = support;
    }

    boolean isDUrlSupport() {
        return dUrlSupport;
    }

    private boolean privacy = false;

    void setPrivacy(boolean privacy) {
        this.privacy = privacy;
    }

    boolean isPrivacy() {
        return privacy;
    }

    private Object ext = null;

    void setExt(Object jsonObject) {
        if (jsonObject instanceof JSONObject || jsonObject instanceof JSONArray) {
            this.ext = jsonObject;
        }
    }

    Object getExt() {
        return ext;
    }

    private ArrayList<NativeEventTracker> trackers = new ArrayList<>();

    void addEventTracker(NativeEventTracker tracker) {
        trackers.add(tracker);
    }

    public ArrayList<NativeEventTracker> getEventTrackers() {
        return trackers;
    }

    private ArrayList<NativeAsset> assets = new ArrayList<>();

    void addAsset(NativeAsset asset) {
        assets.add(asset);
    }

    ArrayList<NativeAsset> getAssets() {
        return assets;
    }
}
