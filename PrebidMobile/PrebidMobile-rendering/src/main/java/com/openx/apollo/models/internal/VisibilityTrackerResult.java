package com.openx.apollo.models.internal;

import com.openx.apollo.models.ntv.NativeEventTracker;
import com.openx.apollo.utils.exposure.ViewExposure;

public class VisibilityTrackerResult {
    private final NativeEventTracker.EventType mEventType;
    private final ViewExposure mViewExposure;
    private final boolean mIsVisible;
    private final boolean mShouldFireImpression;

    public VisibilityTrackerResult(NativeEventTracker.EventType eventType,
                                   ViewExposure viewExposure,
                                   boolean isVisible,
                                   boolean shouldFireImpression) {
        mEventType = eventType;
        mViewExposure = viewExposure;
        mIsVisible = isVisible;
        mShouldFireImpression = shouldFireImpression;
    }

    public NativeEventTracker.EventType getEventType() {
        return mEventType;
    }

    public ViewExposure getViewExposure() {
        return mViewExposure;
    }

    public boolean isVisible() {
        return mIsVisible;
    }

    public boolean shouldFireImpression() {
        return mShouldFireImpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VisibilityTrackerResult result = (VisibilityTrackerResult) o;

        if (mIsVisible != result.mIsVisible) {
            return false;
        }
        if (mShouldFireImpression != result.mShouldFireImpression) {
            return false;
        }
        if (mEventType != result.mEventType) {
            return false;
        }
        return mViewExposure != null
               ? mViewExposure.equals(result.mViewExposure)
               : result.mViewExposure == null;
    }

    @Override
    public int hashCode() {
        int result = mEventType != null ? mEventType.hashCode() : 0;
        result = 31 * result + (mViewExposure != null ? mViewExposure.hashCode() : 0);
        result = 31 * result + (mIsVisible ? 1 : 0);
        result = 31 * result + (mShouldFireImpression ? 1 : 0);
        return result;
    }
}
