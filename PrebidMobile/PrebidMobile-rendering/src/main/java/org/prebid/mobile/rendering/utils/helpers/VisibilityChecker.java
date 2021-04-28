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

package org.prebid.mobile.rendering.utils.helpers;

import android.graphics.Rect;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewParent;

import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.utils.exposure.ViewExposure;
import org.prebid.mobile.rendering.utils.exposure.ViewExposureChecker;
import org.prebid.mobile.rendering.utils.logger.LogUtil;

import androidx.annotation.Nullable;

import static org.prebid.mobile.rendering.models.ntv.NativeEventTracker.EventType.IMPRESSION;
import static org.prebid.mobile.rendering.models.ntv.NativeEventTracker.EventType.OMID;

public class VisibilityChecker {
    private final String TAG = VisibilityChecker.class.getSimpleName();

    private final VisibilityTrackerOption mVisibilityTrackerOption;
    private final ViewExposureChecker mViewExposureChecker;

    private final Rect mClipRect = new Rect();

    public VisibilityChecker(final VisibilityTrackerOption visibilityTrackerOption) {
        mVisibilityTrackerOption = visibilityTrackerOption;
        mViewExposureChecker = new ViewExposureChecker();
    }

    public VisibilityChecker(final VisibilityTrackerOption visibilityTrackerOption,
                             final ViewExposureChecker viewExposureChecker) {
        mVisibilityTrackerOption = visibilityTrackerOption;
        mViewExposureChecker = viewExposureChecker;
    }

    public boolean hasBeenVisible() {
        return mVisibilityTrackerOption.getStartTimeMillis() != Long.MIN_VALUE;
    }

    public void setStartTimeMillis() {
        mVisibilityTrackerOption.setStartTimeMillis(SystemClock.uptimeMillis());
    }

    public boolean hasRequiredTimeElapsed() {
        if (!hasBeenVisible()) {
            return false;
        }

        return SystemClock.uptimeMillis() - mVisibilityTrackerOption.getStartTimeMillis() >= mVisibilityTrackerOption.getMinimumVisibleMillis();
    }

    public boolean isVisible(View trackedView, ViewExposure viewExposure) {
        if (mVisibilityTrackerOption.isType(IMPRESSION)
            || mVisibilityTrackerOption.isType(OMID)) {
            return isVisible(trackedView);
        }

        if (viewExposure == null) {
            return false;
        }

        final float exposurePercentage = viewExposure.getExposurePercentage() * 100;
        return exposurePercentage >= mVisibilityTrackerOption.getMinVisibilityPercentage();
    }

    public boolean isVisible(
        @Nullable
        final View view) {

        if (view == null || !isVisibleForRefresh(view)) {
            return false;
        }

        // ListView & GridView both call detachFromParent() for views that can be recycled for
        // new data. This is one of the rare instances where a view will have a null parent for
        // an extended period of time and will not be the main window.
        // view.getGlobalVisibleRect() doesn't check that case, so if the view has visibility
        // of View.VISIBLE but its group has no parent it is likely in the recycle bin of a
        // ListView / GridView and not on screen.
        ViewParent rootView = view.getParent();
        if (rootView == null || rootView.getParent() == null) {
            return false;
        }

        // If either width or height is non-positive, the view cannot be visible.
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            return false;
        }

        // Calculate area of view not clipped by any of its parents
        final int widthInDips = Dips.pixelsToIntDips((float) mClipRect.width(),
                                                     view.getContext());
        final int heightInDips = Dips.pixelsToIntDips((float) mClipRect.height(),
                                                      view.getContext());
        final long visibleViewAreaInDips = (long) (widthInDips * heightInDips);

        return visibleViewAreaInDips >= mVisibilityTrackerOption.getMinVisibilityPercentage();
    }

    public boolean isVisibleForRefresh(
        @Nullable
        final View view) {

        if (view == null || !view.isShown() || !view.hasWindowFocus()) {
            return false;
        }

        // View completely clipped by its parents
        return view.getGlobalVisibleRect(mClipRect);
    }

    public ViewExposure checkViewExposure(
        @Nullable
        final View view) {

        if (view == null) {
            return null;
        }

        ViewExposure exposure = mViewExposureChecker.exposure(view);
        LogUtil.debug(TAG, exposure != null ? exposure.toString() : "null exposure");
        return exposure;
    }

    public VisibilityTrackerOption getVisibilityTrackerOption() {
        return mVisibilityTrackerOption;
    }
}
