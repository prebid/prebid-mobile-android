package org.prebid.mobile.configuration;

import org.json.JSONObject;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.NativeAsset;
import org.prebid.mobile.NativeEventTracker;

import java.util.ArrayList;
import java.util.List;

public class NativeAdUnitConfiguration {

    private final ArrayList<NativeAsset> nativeAssets = new ArrayList<>();
    private final ArrayList<NativeEventTracker> nativeEventTrackers = new ArrayList<>();
    private NativeAdUnit.CONTEXT_TYPE contextType;
    private NativeAdUnit.CONTEXTSUBTYPE contextSubtype;
    private NativeAdUnit.PLACEMENTTYPE placementType;
    private int placementCount = 1;
    private int sequence = 0;
    private boolean aUrlSupport = false;
    private boolean dUrlSupport = false;
    private boolean privacy = false;
    private JSONObject ext;

    public void addEventTracker(NativeEventTracker tracker) {
        nativeEventTrackers.add(tracker);
    }

    public List<NativeEventTracker> getEventTrackers() {
        return nativeEventTrackers;
    }

    public void clearEventTrackers() {
        nativeEventTrackers.clear();
    }

    public void addAsset(NativeAsset nativeAsset) {
        nativeAssets.add(nativeAsset);
    }

    public ArrayList<NativeAsset> getAssets() {
        return nativeAssets;
    }

    public void clearAssets() {
        nativeAssets.clear();
    }

    public void setContextType(NativeAdUnit.CONTEXT_TYPE contextType) {
        this.contextType = contextType;
    }

    public NativeAdUnit.CONTEXT_TYPE getContextType() {
        return contextType;
    }

    public void setContextSubtype(NativeAdUnit.CONTEXTSUBTYPE contextSubtype) {
        this.contextSubtype = contextSubtype;
    }

    public NativeAdUnit.CONTEXTSUBTYPE getContextSubtype() {
        return contextSubtype;
    }

    public void setPlacementType(NativeAdUnit.PLACEMENTTYPE placementType) {
        this.placementType = placementType;
    }

    public NativeAdUnit.PLACEMENTTYPE getPlacementType() {
        return placementType;
    }

    public void setPlacementCount(int placementCount) {
        this.placementCount = placementCount;
    }

    public int getPlacementCount() {
        return placementCount;
    }

    public void setSeq(int seq) {
        this.sequence = seq;
    }

    public int getSeq() {
        return sequence;
    }

    public void setAUrlSupport(boolean support) {
        aUrlSupport = support;
    }

    public boolean getAUrlSupport() {
        return aUrlSupport;
    }

    public void setDUrlSupport(boolean support) {
        dUrlSupport = support;
    }

    public boolean getDUrlSupport() {
        return dUrlSupport;
    }

    public void setPrivacy(boolean privacy) {
        this.privacy = privacy;
    }

    public boolean getPrivacy() {
        return privacy;
    }

    public void setExt(JSONObject ext) {
        this.ext = ext;
    }

    public JSONObject getExt() {
        return ext;
    }

}
