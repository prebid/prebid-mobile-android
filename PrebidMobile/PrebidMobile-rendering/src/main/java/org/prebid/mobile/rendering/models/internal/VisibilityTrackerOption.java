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

public class VisibilityTrackerOption {
    private NativeEventTracker.EventType mEventType;
    private int mMinimumVisibleMillis;
    private int mMinVisibilityPercentage;
    private boolean mIsImpressionTracked;
    private long mStartTimeMillis = Long.MIN_VALUE;

    public VisibilityTrackerOption(NativeEventTracker.EventType eventType, int minimumVisibleMillis, int minVisibilityPercentage) {
        mEventType = eventType;
        mMinimumVisibleMillis = minimumVisibleMillis;
        mMinVisibilityPercentage = minVisibilityPercentage;
    }

    public VisibilityTrackerOption(NativeEventTracker.EventType eventType) {
        mEventType = eventType;
        mMinimumVisibleMillis = getMinimumVisibleMillis(eventType);
        mMinVisibilityPercentage = getMinimumVisiblePercents(eventType);
    }

    public static int getMinimumVisibleMillis(NativeEventTracker.EventType eventType) {
        switch (eventType) {
            case IMPRESSION:
            case OMID:
                return 0;
            case VIEWABLE_MRC50:
            case VIEWABLE_MRC100:
                return 1000;
            case VIEWABLE_VIDEO50:
                return 2000;
        }
        return 0;
    }

    public static int getMinimumVisiblePercents(NativeEventTracker.EventType eventType) {
        switch (eventType) {
            case IMPRESSION:
            case OMID:
                return 1;
            case VIEWABLE_MRC50:
            case VIEWABLE_VIDEO50:
                return 50;
            case VIEWABLE_MRC100:
                return 100;
        }
        return 0;
    }

    public boolean isType(NativeEventTracker.EventType eventType) {
        return mEventType.equals(eventType);
    }

    public void setStartTimeMillis(long startTimeMillis) {
        mStartTimeMillis = startTimeMillis;
    }

    public long getStartTimeMillis() {
        return mStartTimeMillis;
    }

    public int getMinimumVisibleMillis() {
        return mMinimumVisibleMillis;
    }

    public void setMinimumVisibleMillis(int minimumVisibleMillis) {
        mMinimumVisibleMillis = minimumVisibleMillis;
    }

    public int getMinVisibilityPercentage() {
        return mMinVisibilityPercentage;
    }

    public void setMinVisibilityPercentage(int minVisibilityPercentage) {
        mMinVisibilityPercentage = minVisibilityPercentage;
    }

    public boolean isImpressionTracked() {
        return mIsImpressionTracked;
    }

    public void setImpressionTracked(boolean impressionTracked) {
        mIsImpressionTracked = impressionTracked;
    }

    public NativeEventTracker.EventType getEventType() {
        return mEventType;
    }

    public void setEventType(NativeEventTracker.EventType eventType) {
        mEventType = eventType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VisibilityTrackerOption that = (VisibilityTrackerOption) o;

        if (mMinimumVisibleMillis != that.mMinimumVisibleMillis) {
            return false;
        }
        if (mMinVisibilityPercentage != that.mMinVisibilityPercentage) {
            return false;
        }
        if (mIsImpressionTracked != that.mIsImpressionTracked) {
            return false;
        }
        if (mStartTimeMillis != that.mStartTimeMillis) {
            return false;
        }
        return mEventType == that.mEventType;
    }

    @Override
    public int hashCode() {
        int result = mEventType != null ? mEventType.hashCode() : 0;
        result = 31 * result + mMinimumVisibleMillis;
        result = 31 * result + mMinVisibilityPercentage;
        result = 31 * result + (mIsImpressionTracked ? 1 : 0);
        result = 31 * result + (int) (mStartTimeMillis ^ (mStartTimeMillis >>> 32));
        return result;
    }
}
