package org.prebid.mobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;
import org.prebid.mobile.configuration.NativeAdUnitConfiguration;

import java.util.List;

/**
 * Native parameters.
 * For details of the configuration of native parameters, please check this documentation:
 * https://www.iab.com/wp-content/uploads/2018/03/OpenRTB-Native-Ads-Specification-Final-1.2.pdf
 */
public class NativeParameters {

    private final NativeAdUnitConfiguration nativeConfiguration = new NativeAdUnitConfiguration();

    public NativeParameters(
            @NonNull List<NativeAsset> assets
    ) {
        for (NativeAsset asset : assets) {
            nativeConfiguration.addAsset(asset);
        }
    }

    @Nullable
    public NativeAdUnitConfiguration getNativeConfiguration() {
        return nativeConfiguration;
    }

    public void addEventTracker(NativeEventTracker tracker) {
        nativeConfiguration.addEventTracker(tracker);
    }

    public void setContextType(NativeAdUnit.CONTEXT_TYPE type) {
        nativeConfiguration.setContextType(type);
    }

    public void setContextSubType(NativeAdUnit.CONTEXTSUBTYPE type) {
        nativeConfiguration.setContextSubtype(type);
    }

    public void setPlacementType(NativeAdUnit.PLACEMENTTYPE placementType) {
        nativeConfiguration.setPlacementType(placementType);
    }

    public void setPlacementCount(int placementCount) {
        nativeConfiguration.setPlacementCount(placementCount);
    }

    public void setSeq(int seq) {
        nativeConfiguration.setSeq(seq);
    }

    public void setAUrlSupport(boolean support) {
        nativeConfiguration.setAUrlSupport(support);
    }

    public void setDUrlSupport(boolean support) {
        nativeConfiguration.setDUrlSupport(support);
    }

    public void setPrivacy(boolean privacy) {
        nativeConfiguration.setPrivacy(privacy);
    }

    public void setExt(Object jsonObject) {
        if (jsonObject instanceof JSONObject) {
            nativeConfiguration.setExt((JSONObject) jsonObject);
        }
    }

}
