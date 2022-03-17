package org.prebid.mobile.units.configuration;

import org.json.JSONObject;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.NativeAsset;
import org.prebid.mobile.NativeEventTracker;
import org.prebid.mobile.unification.NativeAdUnitConfigurationInterface;

import java.util.ArrayList;

public class NativeAdUnitConfiguration extends BaseAdUnitConfiguration implements NativeAdUnitConfigurationInterface {

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

    @Override
    public void addEventTracker(NativeEventTracker tracker) {
        nativeEventTrackers.add(tracker);
    }

    @Override
    public ArrayList<NativeEventTracker> getEventTrackers() {
        return nativeEventTrackers;
    }

    @Override
    public void clearEventTrackers() {
        nativeEventTrackers.clear();
    }

    @Override
    public void addAsset(NativeAsset nativeAsset) {
        nativeAssets.add(nativeAsset);
    }

    @Override
    public ArrayList<NativeAsset> getAssets() {
        return nativeAssets;
    }

    @Override
    public void clearAssets() {
        nativeAssets.clear();
    }

    @Override
    public void setContextType(NativeAdUnit.CONTEXT_TYPE contextType) {
        this.contextType = contextType;
    }

    @Override
    public NativeAdUnit.CONTEXT_TYPE getContextType() {
        return contextType;
    }

    @Override
    public void setContextSubtype(NativeAdUnit.CONTEXTSUBTYPE contextSubtype) {
        this.contextSubtype = contextSubtype;
    }

    @Override
    public NativeAdUnit.CONTEXTSUBTYPE getContextSubtype() {
        return contextSubtype;
    }

    @Override
    public void setPlacementType(NativeAdUnit.PLACEMENTTYPE placementType) {
        this.placementType = placementType;
    }

    @Override
    public NativeAdUnit.PLACEMENTTYPE getPlacementType() {
        return placementType;
    }

    @Override
    public void setPlacementCount(int placementCount) {
        this.placementCount = placementCount;
    }

    @Override
    public int getPlacementCount() {
        return placementCount;
    }

    @Override
    public void setSeq(int seq) {
        this.sequence = seq;
    }

    @Override
    public int getSeq() {
        return sequence;
    }

    @Override
    public void setAUrlSupport(boolean support) {
        aUrlSupport = support;
    }

    @Override
    public boolean getAUrlSupport() {
        return aUrlSupport;
    }

    @Override
    public void setDUrlSupport(boolean support) {
        dUrlSupport = support;
    }

    @Override
    public boolean getDUrlSupport() {
        return dUrlSupport;
    }

    @Override
    public void setPrivacy(boolean privacy) {
        this.privacy = privacy;
    }

    @Override
    public boolean getPrivacy() {
        return privacy;
    }

    @Override
    public void setExt(JSONObject ext) {
        this.ext = ext;
    }

    @Override
    public JSONObject getExt() {
        return ext;
    }

}
