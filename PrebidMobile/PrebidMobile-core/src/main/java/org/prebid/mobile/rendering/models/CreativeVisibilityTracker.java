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

package org.prebid.mobile.rendering.models;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerResult;
import org.prebid.mobile.rendering.utils.exposure.ViewExposure;
import org.prebid.mobile.rendering.utils.exposure.ViewExposureChecker;
import org.prebid.mobile.rendering.utils.helpers.VisibilityChecker;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CreativeVisibilityTracker {

    private static final String TAG = CreativeVisibilityTracker.class.getSimpleName();

    // Time interval to use for throttling visibility checks.
    private static final int VISIBILITY_THROTTLE_MILLIS = 200;

    public interface VisibilityTrackerListener {

        void onVisibilityChanged(VisibilityTrackerResult result);

    }

    private ViewTreeObserver.OnPreDrawListener onPreDrawListener;
    private WeakReference<ViewTreeObserver> weakViewTreeObserver;

    private WeakReference<View> trackedView;
    private final List<VisibilityChecker> visibilityCheckerList = new ArrayList<>();
    @VisibleForTesting protected Runnable visibilityRunnable;
    private Handler visibilityHandler;
    private VisibilityTrackerListener visibilityTrackerListener;
    private boolean proceedAfterImpTracking;
    private boolean isVisibilityScheduled;

    public CreativeVisibilityTracker(
            @NonNull final View trackedView,
            final Set<VisibilityTrackerOption> visibilityTrackerOptionSet
    ) {
        if (trackedView == null) {
            LogUtil.debug(TAG, "Tracked view can't be null");
            return;
        }

        this.trackedView = new WeakReference<>(trackedView);
        final ViewExposureChecker viewExposureChecker = new ViewExposureChecker();

        for (VisibilityTrackerOption trackingOption : visibilityTrackerOptionSet) {
            visibilityCheckerList.add(new VisibilityChecker(trackingOption, viewExposureChecker));
        }

        visibilityHandler = new Handler(Looper.getMainLooper());
        visibilityRunnable = createVisibilityRunnable();

        onPreDrawListener = () -> {
            scheduleVisibilityCheck();
            return true;
        };

        weakViewTreeObserver = new WeakReference<>(null);
    }

    public CreativeVisibilityTracker(
        @NonNull
        final View trackedView,
        final Set<VisibilityTrackerOption> visibilityTrackerOptionSet,
        boolean proceedAfterImpTracking) {
        this(trackedView, visibilityTrackerOptionSet);
        this.proceedAfterImpTracking = proceedAfterImpTracking;
    }

    public CreativeVisibilityTracker(
        @NonNull
        final View trackedView,
        final VisibilityTrackerOption visibilityTrackerOption) {
        this(trackedView, Collections.singleton(visibilityTrackerOption));
    }

    public CreativeVisibilityTracker(
        @NonNull
        final View trackedView,
        final VisibilityTrackerOption visibilityTrackerOption,
        boolean proceedAfterImpTracking) {
        this(trackedView, Collections.singleton(visibilityTrackerOption), proceedAfterImpTracking);
    }

    private void setViewTreeObserver(
        @Nullable
        final Context context,
        @Nullable
        final View view) {
        final ViewTreeObserver originalViewTreeObserver = weakViewTreeObserver.get();
        if (originalViewTreeObserver != null && originalViewTreeObserver.isAlive()) {
            LogUtil.debug(TAG, "Original ViewTreeObserver is still alive.");
            return;
        }

        final View rootView = Views.getTopmostView(context, view);
        if (rootView == null) {
            LogUtil.debug(TAG, "Unable to set Visibility Tracker due to no available root view.");
            return;
        }

        final ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
        if (!viewTreeObserver.isAlive()) {
            LogUtil.debug(
                    TAG,
                    "Visibility Tracker was unable to track views because the" + " root view tree observer was not alive"
            );
            return;
        }

        weakViewTreeObserver = new WeakReference<>(viewTreeObserver);
        viewTreeObserver.addOnPreDrawListener(onPreDrawListener);
    }

    public void setVisibilityTrackerListener(
        @Nullable
        final VisibilityTrackerListener visibilityTrackerListener) {
        this.visibilityTrackerListener = visibilityTrackerListener;
    }

    public void startVisibilityCheck(Context context) {
        if (trackedView == null || trackedView.get() == null) {
            LogUtil.error(TAG, "Couldn't start visibility check. Target view is null");
            return;
        }
        setViewTreeObserver(context, trackedView.get());
    }

    public void stopVisibilityCheck() {
        visibilityHandler.removeCallbacksAndMessages(null);
        isVisibilityScheduled = false;
        final ViewTreeObserver viewTreeObserver = weakViewTreeObserver.get();
        if (viewTreeObserver != null && viewTreeObserver.isAlive()) {
            viewTreeObserver.removeOnPreDrawListener(onPreDrawListener);
        }
        weakViewTreeObserver.clear();
    }

    void scheduleVisibilityCheck() {
        // Tracking this directly instead of calling hasMessages directly because we measured that
        // this led to slightly better performance.
        if (isVisibilityScheduled) {
            return;
        }

        isVisibilityScheduled = true;
        visibilityHandler.postDelayed(visibilityRunnable, VISIBILITY_THROTTLE_MILLIS);
    }

    private Runnable createVisibilityRunnable() {
        return () -> {
            View trackedView = this.trackedView.get();
            if (trackedView == null) {
                stopVisibilityCheck();
                return;
            }

            if (allImpressionsFired() && !proceedAfterImpTracking) {
                return;
            }

            for (VisibilityChecker visibilityChecker : visibilityCheckerList) {
                isVisibilityScheduled = false;
                ViewExposure viewExposure = visibilityChecker.checkViewExposure(trackedView);
                boolean shouldFireImpression = false;
                boolean isVisible = visibilityChecker.isVisible(trackedView, viewExposure);

                // If the view meets the dips count requirement for visibility, then also check the
                // duration requirement for visibility.
                VisibilityTrackerOption visibilityTrackerOption = visibilityChecker.getVisibilityTrackerOption();

                if (isVisible) {
                    if (!visibilityChecker.hasBeenVisible()) {
                        visibilityChecker.setStartTimeMillis();
                    }

                    if (visibilityChecker.hasRequiredTimeElapsed()) {
                        shouldFireImpression = !visibilityTrackerOption.isImpressionTracked();
                        visibilityTrackerOption.setImpressionTracked(true);
                    }
                }

                VisibilityTrackerResult visibilityTrackerResult = new VisibilityTrackerResult(
                    visibilityTrackerOption.getEventType(),
                    viewExposure,
                    isVisible,
                    shouldFireImpression
                );
                notifyListener(visibilityTrackerResult);
            }

            // If visibility requirements are not met or the target is MRAID, check again later.
            if (!allImpressionsFired() || proceedAfterImpTracking) {
                scheduleVisibilityCheck();
            }
        };
    }

    private void notifyListener(VisibilityTrackerResult visibilityTrackerResult) {
        if (visibilityTrackerListener != null) {
            visibilityTrackerListener.onVisibilityChanged(visibilityTrackerResult);
        }
    }

    private boolean allImpressionsFired() {
        for (VisibilityChecker visibilityChecker : visibilityCheckerList) {
            final VisibilityTrackerOption visibilityTrackerOption = visibilityChecker.getVisibilityTrackerOption();
            if (!visibilityTrackerOption.isImpressionTracked()) {
                return false;
            }
        }
        return true;
    }
}
