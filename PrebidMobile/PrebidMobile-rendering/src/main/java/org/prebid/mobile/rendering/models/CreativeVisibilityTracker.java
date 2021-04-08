package org.prebid.mobile.rendering.models;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerResult;
import org.prebid.mobile.rendering.utils.exposure.ViewExposure;
import org.prebid.mobile.rendering.utils.exposure.ViewExposureChecker;
import org.prebid.mobile.rendering.utils.helpers.VisibilityChecker;
import org.prebid.mobile.rendering.utils.logger.OXLog;
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

    private ViewTreeObserver.OnPreDrawListener mOnPreDrawListener;
    private WeakReference<ViewTreeObserver> mWeakViewTreeObserver;

    private WeakReference<View> mTrackedView;
    private final List<VisibilityChecker> mVisibilityCheckerList = new ArrayList<>();
    @VisibleForTesting
    protected Runnable mVisibilityRunnable;
    private Handler mVisibilityHandler;
    private VisibilityTrackerListener mVisibilityTrackerListener;
    private boolean mProceedAfterImpTracking;
    private boolean mIsVisibilityScheduled;

    public CreativeVisibilityTracker(
        @NonNull
        final View trackedView,
        final Set<VisibilityTrackerOption> visibilityTrackerOptionSet) {
        if (trackedView == null) {
            OXLog.debug(TAG, "Tracked view can't be null");
            return;
        }

        mTrackedView = new WeakReference<>(trackedView);
        final ViewExposureChecker viewExposureChecker = new ViewExposureChecker();

        for (VisibilityTrackerOption trackingOption : visibilityTrackerOptionSet) {
            mVisibilityCheckerList.add(new VisibilityChecker(trackingOption, viewExposureChecker));
        }

        mVisibilityHandler = new Handler(Looper.getMainLooper());
        mVisibilityRunnable = createVisibilityRunnable();

        mOnPreDrawListener = () -> {
            scheduleVisibilityCheck();
            return true;
        };

        mWeakViewTreeObserver = new WeakReference<>(null);
    }

    public CreativeVisibilityTracker(
        @NonNull
        final View trackedView,
        final Set<VisibilityTrackerOption> visibilityTrackerOptionSet,
        boolean proceedAfterImpTracking) {
        this(trackedView, visibilityTrackerOptionSet);
        mProceedAfterImpTracking = proceedAfterImpTracking;
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
        final ViewTreeObserver originalViewTreeObserver = mWeakViewTreeObserver.get();
        if (originalViewTreeObserver != null && originalViewTreeObserver.isAlive()) {
            OXLog.debug(TAG, "Original ViewTreeObserver is still alive.");
            return;
        }

        final View rootView = Views.getTopmostView(context, view);
        if (rootView == null) {
            OXLog.debug(TAG, "Unable to set Visibility Tracker due to no available root view.");
            return;
        }

        final ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
        if (!viewTreeObserver.isAlive()) {
            OXLog.debug(TAG, "Visibility Tracker was unable to track views because the"
                             + " root view tree observer was not alive");
            return;
        }

        mWeakViewTreeObserver = new WeakReference<>(viewTreeObserver);
        viewTreeObserver.addOnPreDrawListener(mOnPreDrawListener);
    }

    public void setVisibilityTrackerListener(
        @Nullable
        final VisibilityTrackerListener visibilityTrackerListener) {
        mVisibilityTrackerListener = visibilityTrackerListener;
    }

    public void startVisibilityCheck(Context context) {
        if (mTrackedView == null || mTrackedView.get() == null) {
            OXLog.error(TAG, "Couldn't start visibility check. Target view is null");
            return;
        }
        setViewTreeObserver(context, mTrackedView.get());
    }

    public void stopVisibilityCheck() {
        mVisibilityHandler.removeCallbacksAndMessages(null);
        mIsVisibilityScheduled = false;
        final ViewTreeObserver viewTreeObserver = mWeakViewTreeObserver.get();
        if (viewTreeObserver != null && viewTreeObserver.isAlive()) {
            viewTreeObserver.removeOnPreDrawListener(mOnPreDrawListener);
        }
        mWeakViewTreeObserver.clear();
    }

    void scheduleVisibilityCheck() {
        // Tracking this directly instead of calling hasMessages directly because we measured that
        // this led to slightly better performance.
        if (mIsVisibilityScheduled) {
            return;
        }

        mIsVisibilityScheduled = true;
        mVisibilityHandler.postDelayed(mVisibilityRunnable, VISIBILITY_THROTTLE_MILLIS);
    }

    private Runnable createVisibilityRunnable() {
        return () -> {
            View trackedView = mTrackedView.get();
            if (trackedView == null) {
                stopVisibilityCheck();
                return;
            }

            if (allImpressionsFired() && !mProceedAfterImpTracking) {
                return;
            }

            for (VisibilityChecker visibilityChecker : mVisibilityCheckerList) {
                mIsVisibilityScheduled = false;
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
            if (!allImpressionsFired() || mProceedAfterImpTracking) {
                scheduleVisibilityCheck();
            }
        };
    }

    private void notifyListener(VisibilityTrackerResult visibilityTrackerResult) {
        if (mVisibilityTrackerListener != null) {
             mVisibilityTrackerListener.onVisibilityChanged(visibilityTrackerResult);
        }
    }

    private boolean allImpressionsFired() {
        for (VisibilityChecker visibilityChecker : mVisibilityCheckerList) {
            final VisibilityTrackerOption visibilityTrackerOption = visibilityChecker.getVisibilityTrackerOption();
            if (!visibilityTrackerOption.isImpressionTracked()) {
                return false;
            }
        }
        return true;
    }
}
