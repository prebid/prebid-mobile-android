package org.prebid.mobile.api.mediation;

import androidx.annotation.NonNull;

import org.prebid.mobile.ContentObject;
import org.prebid.mobile.DataObject;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.NativeAsset;
import org.prebid.mobile.NativeEventTracker;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.api.data.FetchDemandResult;
import org.prebid.mobile.api.mediation.listeners.OnFetchCompleteListener;

import java.util.ArrayList;
import java.util.Set;

public class MediationNativeAdUnit {

    private static final String TAG = "MediationNativeAdUnit";

    private final Object adObject;
    private final NativeAdUnit nativeAdUnit;

    public MediationNativeAdUnit(
        @NonNull String configId,
        @NonNull Object adObject
    ) {
        this.adObject = adObject;
        this.nativeAdUnit = new NativeAdUnit(configId);
    }

    public void fetchDemand(
        @NonNull OnFetchCompleteListener listener
    ) {
        nativeAdUnit.fetchDemand(adObject, resultCode ->
            listener.onComplete(convertResultCode(resultCode))
        );
    }

    public void destroy() {
        nativeAdUnit.stopAutoRefresh();
    }

    public void addAsset(NativeAsset asset) {
        nativeAdUnit.addAsset(asset);
    }

    public void addEventTracker(NativeEventTracker tracker) {
        nativeAdUnit.addEventTracker(tracker);
    }

    public void setContextType(NativeAdUnit.CONTEXT_TYPE type) {
        nativeAdUnit.setContextType(type);
    }

    public void setContextSubType(NativeAdUnit.CONTEXTSUBTYPE subType) {
        nativeAdUnit.setContextSubType(subType);
    }

    public void setExt(Object jsonObject) {
        nativeAdUnit.setExt(jsonObject);
    }

    public void setSeq(int seq) {
        nativeAdUnit.setSeq(seq);
    }

    public void setPrivacy(boolean privacy) {
        nativeAdUnit.setPrivacy(privacy);
    }

    public void setPlacementType(NativeAdUnit.PLACEMENTTYPE type) {
        nativeAdUnit.setPlacementType(type);
    }

    public void setPlacementCount(int implementCount) {
        nativeAdUnit.setPlacementCount(implementCount);
    }

    public void setAUrlSupport(boolean support) {
        nativeAdUnit.setAUrlSupport(support);
    }

    public void setDUrlSupport(boolean support) {
        nativeAdUnit.setDUrlSupport(support);
    }

    public void setAppContent(ContentObject content) {
        nativeAdUnit.setAppContent(content);
    }

    public ContentObject getAppContent() {
        return nativeAdUnit.getAppContent();
    }

    public void addUserData(DataObject dataObject) {
        nativeAdUnit.addUserData(dataObject);
    }

    public void clearUserData() {
        nativeAdUnit.clearUserData();
    }

    public ArrayList<DataObject> getUserData() {
        return nativeAdUnit.getUserData();
    }

    public void addExtData(
        String key,
        String value
    ) {
        nativeAdUnit.addExtData(key, value);
    }

    public void updateExtData(
        String key,
        Set<String> value
    ) {
        nativeAdUnit.updateExtData(key, value);
    }

    public void removeExtData(String key) {
        nativeAdUnit.removeExtData(key);
    }

    public void clearExtData() {
        nativeAdUnit.clearExtData();
    }

    public void addExtKeyword(String keyword) {
        nativeAdUnit.addExtKeyword(keyword);
    }

    public void addExtKeywords(Set<String> keywords) {
        nativeAdUnit.addExtKeywords(keywords);
    }

    public void removeExtKeyword(String keyword) {
        nativeAdUnit.removeExtKeyword(keyword);
    }

    public void clearExtKeywords() {
        nativeAdUnit.clearExtKeywords();
    }

    private FetchDemandResult convertResultCode(ResultCode originalResult) {
        switch (originalResult) {
            case SUCCESS:
                return FetchDemandResult.SUCCESS;
            case INVALID_ACCOUNT_ID:
                return FetchDemandResult.INVALID_ACCOUNT_ID;
            case INVALID_CONFIG_ID:
                return FetchDemandResult.INVALID_CONFIG_ID;
            case INVALID_CONTEXT:
                return FetchDemandResult.INVALID_CONTEXT;
            case INVALID_HOST_URL:
                return FetchDemandResult.INVALID_HOST_URL;
            case INVALID_SIZE:
                return FetchDemandResult.INVALID_SIZE;
            case INVALID_AD_OBJECT:
                return FetchDemandResult.INVALID_AD_OBJECT;
            case NO_BIDS:
                return FetchDemandResult.NO_BIDS;
            case PREBID_SERVER_ERROR:
                return FetchDemandResult.SERVER_ERROR;
            case TIMEOUT:
                return FetchDemandResult.TIMEOUT;
            case NETWORK_ERROR:
                return FetchDemandResult.NETWORK_ERROR;
            case INVALID_NATIVE_REQUEST:
                LogUtil.error(TAG, "Invalid native request!");
                return FetchDemandResult.NETWORK_ERROR;
            default:
                LogUtil.error(TAG, "Something went wrong!");
                return FetchDemandResult.NETWORK_ERROR;
        }
    }

}
