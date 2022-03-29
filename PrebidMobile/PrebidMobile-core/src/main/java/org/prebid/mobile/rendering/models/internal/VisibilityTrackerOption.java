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

    private NativeEventTracker.EventType eventType;
    private int minimumVisibleMillis;
    private int minVisibilityPercentage;
    private boolean isImpressionTracked;
    private long startTimeMillis = Long.MIN_VALUE;

    public VisibilityTrackerOption(
            NativeEventTracker.EventType eventType,
            int minimumVisibleMillis,
            int minVisibilityPercentage
    ) {
        this.eventType = eventType;
        this.minimumVisibleMillis = minimumVisibleMillis;
        this.minVisibilityPercentage = minVisibilityPercentage;
    }

    public VisibilityTrackerOption(NativeEventTracker.EventType eventType) {
        this.eventType = eventType;
        minimumVisibleMillis = getMinimumVisibleMillis(eventType);
        minVisibilityPercentage = getMinimumVisiblePercents(eventType);
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
        return this.eventType.equals(eventType);
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public int getMinimumVisibleMillis() {
        return minimumVisibleMillis;
    }

    public void setMinimumVisibleMillis(int minimumVisibleMillis) {
        this.minimumVisibleMillis = minimumVisibleMillis;
    }

    public int getMinVisibilityPercentage() {
        return minVisibilityPercentage;
    }

    public void setMinVisibilityPercentage(int minVisibilityPercentage) {
        this.minVisibilityPercentage = minVisibilityPercentage;
    }

    public boolean isImpressionTracked() {
        return isImpressionTracked;
    }

    public void setImpressionTracked(boolean impressionTracked) {
        isImpressionTracked = impressionTracked;
    }

    public NativeEventTracker.EventType getEventType() {
        return eventType;
    }

    public void setEventType(NativeEventTracker.EventType eventType) {
        this.eventType = eventType;
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

        if (minimumVisibleMillis != that.minimumVisibleMillis) {
            return false;
        }
        if (minVisibilityPercentage != that.minVisibilityPercentage) {
            return false;
        }
        if (isImpressionTracked != that.isImpressionTracked) {
            return false;
        }
        if (startTimeMillis != that.startTimeMillis) {
            return false;
        }
        return eventType == that.eventType;
    }

    @Override
    public int hashCode() {
        int result = eventType != null ? eventType.hashCode() : 0;
        result = 31 * result + minimumVisibleMillis;
        result = 31 * result + minVisibilityPercentage;
        result = 31 * result + (isImpressionTracked ? 1 : 0);
        result = 31 * result + (int) (startTimeMillis ^ (startTimeMillis >>> 32));
        return result;
    }
}
