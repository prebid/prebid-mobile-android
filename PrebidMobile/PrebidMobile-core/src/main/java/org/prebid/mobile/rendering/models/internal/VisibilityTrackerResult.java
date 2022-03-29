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

    private final NativeEventTracker.EventType eventType;
    private final ViewExposure viewExposure;
    private final boolean isVisible;
    private final boolean shouldFireImpression;

    public VisibilityTrackerResult(
            NativeEventTracker.EventType eventType,
            ViewExposure viewExposure,
            boolean isVisible,
            boolean shouldFireImpression
    ) {
        this.eventType = eventType;
        this.viewExposure = viewExposure;
        this.isVisible = isVisible;
        this.shouldFireImpression = shouldFireImpression;
    }

    public NativeEventTracker.EventType getEventType() {
        return eventType;
    }

    public ViewExposure getViewExposure() {
        return viewExposure;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean shouldFireImpression() {
        return shouldFireImpression;
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

        if (isVisible != result.isVisible) {
            return false;
        }
        if (shouldFireImpression != result.shouldFireImpression) {
            return false;
        }
        if (eventType != result.eventType) {
            return false;
        }
        return viewExposure != null ? viewExposure.equals(result.viewExposure) : result.viewExposure == null;
    }

    @Override
    public int hashCode() {
        int result = eventType != null ? eventType.hashCode() : 0;
        result = 31 * result + (viewExposure != null ? viewExposure.hashCode() : 0);
        result = 31 * result + (isVisible ? 1 : 0);
        result = 31 * result + (shouldFireImpression ? 1 : 0);
        return result;
    }
}
