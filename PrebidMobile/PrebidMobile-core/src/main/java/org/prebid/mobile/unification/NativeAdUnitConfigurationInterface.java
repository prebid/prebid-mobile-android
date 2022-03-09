package org.prebid.mobile.unification;

import org.json.JSONObject;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.NativeAsset;
import org.prebid.mobile.NativeEventTracker;

import java.util.ArrayList;

public interface NativeAdUnitConfigurationInterface extends BaseAdUnitConfigurationInterface {

    public void addEventTracker(NativeEventTracker tracker);

    public ArrayList<NativeEventTracker> getEventTrackers();

    public void clearEventTrackers();


    public void addAsset(NativeAsset nativeAsset);

    public ArrayList<NativeAsset> getAssets();

    public void clearAssets();


    public void setContextType(NativeAdUnit.CONTEXT_TYPE contextType);

    public NativeAdUnit.CONTEXT_TYPE getContextType();

    public void setContextSubtype(NativeAdUnit.CONTEXTSUBTYPE contextSubType);

    public NativeAdUnit.CONTEXTSUBTYPE getContextSubtype();

    public void setPlacementType(NativeAdUnit.PLACEMENTTYPE placementType);

    public NativeAdUnit.PLACEMENTTYPE getPlacementType();

    public void setPlacementCount(int placementCount);

    public int getPlacementCount();

    public void setSeq(int seq);

    public int getSeq();

    public void setAUrlSupport(boolean support);

    public boolean getAUrlSupport();

    public void setDUrlSupport(boolean support);

    public boolean getDUrlSupport();

    public void setPrivacy(boolean privacy);

    public boolean getPrivacy();

    public void setExt(JSONObject jsonObject);

    public JSONObject getExt();

}
