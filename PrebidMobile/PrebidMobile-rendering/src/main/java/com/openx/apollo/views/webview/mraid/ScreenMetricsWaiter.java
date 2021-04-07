package com.openx.apollo.views.webview.mraid;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.views.webview.OpenXWebViewBase;

import java.util.LinkedList;

public class ScreenMetricsWaiter {

    private final static String TAG = ScreenMetricsWaiter.class.getSimpleName();

    @NonNull
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private LinkedList<WaitRequest> mWaitRequestQueue = new LinkedList<>();

    void queueMetricsRequest(
        @NonNull
            Runnable successRunnable, boolean isAnswerRequired,
        @NonNull
            View... views) {
        WaitRequest newWaitRequest = new WaitRequest(mHandler, successRunnable, isAnswerRequired, views);
        if (mWaitRequestQueue.isEmpty()) {
            newWaitRequest.start();
        }
        mWaitRequestQueue.addLast(newWaitRequest);
        OXLog.debug(TAG, "New request queued. Queue size: " + mWaitRequestQueue.size());
    }

    void finishAndStartNextRequest() {
        mWaitRequestQueue.removeFirst();
        WaitRequest firstInQueueRequest = mWaitRequestQueue.peekFirst();
        OXLog.debug(TAG, "Request finished. Queue size: " + mWaitRequestQueue.size());
        if (firstInQueueRequest != null) {
            firstInQueueRequest.start();
        }
    }

    void cancelPendingRequests() {
        WaitRequest waitRequest = mWaitRequestQueue.pollFirst();
        while (waitRequest != null) {
            waitRequest.cancel();
            waitRequest = mWaitRequestQueue.pollFirst();
        }
    }

    static class WaitRequest {
        @NonNull
        private final View[] mViews;
        @NonNull
        private final Handler mHandler;
        @Nullable
        private Runnable mSuccessRunnable;
        private boolean mIsAnswerRequired;
        int mWaitCount;

        private WaitRequest(
            @NonNull
                Handler handler,
            @NonNull
                Runnable successRunnable,
            boolean isAnswerRequired,
            @NonNull
            final View[] views) {
            mIsAnswerRequired = isAnswerRequired;
            mHandler = handler;
            mSuccessRunnable = successRunnable;
            mViews = views;
        }

        private void countDown() {
            mWaitCount--;
            if (mWaitCount == 0 && mSuccessRunnable != null) {
                mSuccessRunnable.run();
                mSuccessRunnable = null;
            }
        }

        private final Runnable mWaitingRunnable = new Runnable() {
            @Override
            public void run() {

                for (final View view : mViews) {
                    boolean isTwoPart = false;
                    if (view instanceof OpenXWebViewBase && ((OpenXWebViewBase) view).getMraidWebView() != null) {
                        String jsName = ((OpenXWebViewBase) view).getMraidWebView().getJSName();
                        isTwoPart = "twopart".equals(jsName);
                    }

                    // Immediately count down for any views that already have a size
                    if (view.getHeight() > 0 || view.getWidth() > 0 || mIsAnswerRequired || isTwoPart) {
                        countDown();
                        OXLog.debug(TAG, "Get known metrics for: " + view.getClass().getSimpleName() + ", h: " + view.getHeight() + ", w: " + view.getWidth());
                        continue;
                    }

                    // For views that didn't have a size, listen (once) for a preDraw. Note
                    // that this doesn't leak because the ViewTreeObserver gets detached when
                    // the view is no longer part of the view hierarchy.

                    OXLog.debug(TAG, "Create listener for: " + view.getClass().getSimpleName());
                    view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            OXLog.debug(TAG, "Get metrics from listener for: " + view.getClass().getSimpleName() + ", h: " + view.getHeight() + ", w: " + view.getWidth());
                            view.getViewTreeObserver().removeOnPreDrawListener(this);
                            countDown();
                            return true;
                        }
                    });
                }
            }
        };

        void start() {
            mWaitCount = mViews.length;
            mHandler.post(mWaitingRunnable);
        }

        void cancel() {
            mHandler.removeCallbacks(mWaitingRunnable);
            mSuccessRunnable = null;
        }
    }
}
