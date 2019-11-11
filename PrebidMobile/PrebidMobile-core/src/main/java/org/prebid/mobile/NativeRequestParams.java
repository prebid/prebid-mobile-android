/*
 *    Copyright 2018-2019 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

class NativeRequestParams {
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
    static String WIDTH_MIN = "wmin";
    static String HEIGHT_MIN = "hmin";
    static String WIDTH = "w";
    static String HEIGHT = "h";
    static String TYPE = "type";
    static String MIMES = "mimes";
    static String TITLE = "title";
    static String IMAGE = "img";
    static String DATA = "data";
    static String NATIVE = "native";
    static String REQUEST = "request";

    private NativeAdUnit.CONTEXT_TYPE contextType;
    private NativeAdUnit.CONTEXTSUBTYPE contextsubtype;
    private NativeAdUnit.PLACEMENTTYPE placementtype;
    private int placementCount = 1;
    private int seq = 0;
    private boolean aUrlSupport = false;
    private boolean dUrlSupport = false;
    private boolean privacy = false;
    private Object ext = null;
    private ArrayList<NativeEventTracker> trackers = new ArrayList<>();
    private ArrayList<NativeAsset> assets = new ArrayList<>();

    void setContextType(NativeAdUnit.CONTEXT_TYPE type) {
        this.contextType = type;
    }

    NativeAdUnit.CONTEXT_TYPE getContextType() {
        return contextType;
    }

    void setContextSubType(NativeAdUnit.CONTEXTSUBTYPE type) {
        this.contextsubtype = type;
    }

    NativeAdUnit.CONTEXTSUBTYPE getContextsubtype() {
        return contextsubtype;
    }

    void setPlacementType(NativeAdUnit.PLACEMENTTYPE placementType) {
        this.placementtype = placementType;
    }

    NativeAdUnit.PLACEMENTTYPE getPlacementType() {
        return placementtype;
    }

    void setPlacementCount(int placementCount) {
        this.placementCount = placementCount;
    }

    int getPlacementCount() {
        return placementCount;
    }

    void setSeq(int seq) {
        this.seq = seq;
    }

    int getSeq() {
        return seq;
    }

    void setAUrlSupport(boolean support) {
        this.aUrlSupport = support;
    }

    boolean isAUrlSupport() {
        return aUrlSupport;
    }

    void setDUrlSupport(boolean support) {
        this.dUrlSupport = support;
    }

    boolean isDUrlSupport() {
        return dUrlSupport;
    }

    void setPrivacy(boolean privacy) {
        this.privacy = privacy;
    }

    boolean isPrivacy() {
        return privacy;
    }

    void setExt(Object jsonObject) {
        if (jsonObject instanceof JSONObject || jsonObject instanceof JSONArray) {
            this.ext = jsonObject;
        }
    }

    Object getExt() {
        return ext;
    }

    void addEventTracker(NativeEventTracker tracker) {
        trackers.add(tracker);
    }

    ArrayList<NativeEventTracker> getEventTrackers() {
        return trackers;
    }

    void addAsset(NativeAsset asset) {
        assets.add(asset);
    }

    ArrayList<NativeAsset> getAssets() {
        return assets;
    }
}
