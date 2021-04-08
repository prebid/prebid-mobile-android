package org.prebid.mobile.rendering.bidding.data.ntv;

import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;

public class NativeAdEventTracker {
    private final NativeEventTracker.EventType mEventType;
    private final NativeEventTracker.EventTrackingMethod mEventTrackingMethod;
    private final String mUrl;
    private final String mCustomData; // ?
    private final Ext mExt;

    NativeAdEventTracker(NativeEventTracker.EventType eventType,
                         NativeEventTracker.EventTrackingMethod eventTrackingMethod,
                         String url,
                         String customData,
                         Ext ext) {
        mEventType = eventType;
        mEventTrackingMethod = eventTrackingMethod;
        mUrl = url;
        mCustomData = customData;
        mExt = ext;
    }

    public NativeEventTracker.EventType getEventType() {
        return mEventType;
    }

    public NativeEventTracker.EventTrackingMethod getEventTrackingMethod() {
        return mEventTrackingMethod;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getCustomData() {
        return mCustomData;
    }

    public Ext getExt() {
        return mExt;
    }

    public boolean isSupportedEventMethod() {
        return NativeEventTracker.EventTrackingMethod.IMAGE.equals(mEventTrackingMethod)
               || mEventType == NativeEventTracker.EventType.OMID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NativeAdEventTracker that = (NativeAdEventTracker) o;

        if (mEventType != that.mEventType) {
            return false;
        }
        if (mEventTrackingMethod != that.mEventTrackingMethod) {
            return false;
        }
        if (mUrl != null ? !mUrl.equals(that.mUrl) : that.mUrl != null) {
            return false;
        }
        if (mCustomData != null
            ? !mCustomData.equals(that.mCustomData)
            : that.mCustomData != null) {
            return false;
        }
        return mExt != null ? mExt.equals(that.mExt) : that.mExt == null;
    }

    @Override
    public int hashCode() {
        int result = mEventType != null ? mEventType.hashCode() : 0;
        result = 31 * result + (mEventTrackingMethod != null ? mEventTrackingMethod.hashCode() : 0);
        result = 31 * result + (mUrl != null ? mUrl.hashCode() : 0);
        result = 31 * result + (mCustomData != null ? mCustomData.hashCode() : 0);
        result = 31 * result + (mExt != null ? mExt.hashCode() : 0);
        return result;
    }
}
