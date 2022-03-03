package org.prebid.mobile.unification;

import org.json.JSONObject;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.NativeAsset;
import org.prebid.mobile.NativeEventTracker;

import java.util.ArrayList;

public interface NativeAdUnitConfigurationInterface extends BaseAdUnitConfigurationInterface {

    public void addEventTracker(NativeEventTracker tracker);

    public ArrayList<NativeEventTracker> getNativeEventTrackers();

    public void clearNativeEventTrackers();


    public void addAsset(NativeAsset nativeAsset);

    public ArrayList<NativeAsset> getNativeAssets();

    public void clearNativeAssets();


    public void setContextType(NativeAdUnit.CONTEXT_TYPE contextType);

    public NativeAdUnit.CONTEXT_TYPE getContextType();

    public void setContextSubtype(NativeAdUnit.CONTEXTSUBTYPE contextSubType);

    public NativeAdUnit.CONTEXTSUBTYPE getContextSubtype();

    public void setPlacementType(NativeAdUnit.PLACEMENTTYPE placementType);

    public NativeAdUnit.PLACEMENTTYPE getPlacementType();

    public void setPlacementCount(int placementCount);

    public Integer getPlacementCount();

    public void setSeq(int seq);

    public Integer getSeq();

    public void setAUrlSupport(boolean support);

    public Boolean getAUrlSupport();

    public void setDUrlSupport(boolean support);

    public Boolean getDUrlSupport();

    public void setPrivacy(boolean privacy);

    public Boolean getPrivacy();

    public void setExt(JSONObject jsonObject);

    public JSONObject getExt();

}
