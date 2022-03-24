/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.rendering.models.internal;

import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.utils.exposure.ViewExposure;

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
