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

package org.prebid.mobile.rendering.views.webview.mraid;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBase;

import java.util.LinkedList;

public class ScreenMetricsWaiter {

    private final static String TAG = ScreenMetricsWaiter.class.getSimpleName();

    @NonNull private final Handler handler = new Handler(Looper.getMainLooper());

    private LinkedList<WaitRequest> waitRequestQueue = new LinkedList<>();

    void queueMetricsRequest(
        @NonNull
            Runnable successRunnable, boolean isAnswerRequired,
        @NonNull
            View... views) {
        WaitRequest newWaitRequest = new WaitRequest(handler, successRunnable, isAnswerRequired, views);
        if (waitRequestQueue.isEmpty()) {
            newWaitRequest.start();
        }
        waitRequestQueue.addLast(newWaitRequest);
        LogUtil.debug(TAG, "New request queued. Queue size: " + waitRequestQueue.size());
    }

    void finishAndStartNextRequest() {
        waitRequestQueue.removeFirst();
        WaitRequest firstInQueueRequest = waitRequestQueue.peekFirst();
        LogUtil.debug(TAG, "Request finished. Queue size: " + waitRequestQueue.size());
        if (firstInQueueRequest != null) {
            firstInQueueRequest.start();
        }
    }

    void cancelPendingRequests() {
        WaitRequest waitRequest = waitRequestQueue.pollFirst();
        while (waitRequest != null) {
            waitRequest.cancel();
            waitRequest = waitRequestQueue.pollFirst();
        }
    }

    static class WaitRequest {

        @NonNull private final View[] views;
        @NonNull private final Handler handler;
        @Nullable private Runnable successRunnable;
        private boolean isAnswerRequired;
        int waitCount;

        private WaitRequest(
                @NonNull Handler handler,
                @NonNull Runnable successRunnable,
                boolean isAnswerRequired,
                @NonNull final View[] views
        ) {
            this.isAnswerRequired = isAnswerRequired;
            this.handler = handler;
            this.successRunnable = successRunnable;
            this.views = views;
        }

        private void countDown() {
            waitCount--;
            if (waitCount == 0 && successRunnable != null) {
                successRunnable.run();
                successRunnable = null;
            }
        }

        private final Runnable waitingRunnable = new Runnable() {
            @Override
            public void run() {

                for (final View view : views) {
                    boolean isTwoPart = false;
                    if (view instanceof PrebidWebViewBase && ((PrebidWebViewBase) view).getMraidWebView() != null) {
                        String jsName = ((PrebidWebViewBase) view).getMraidWebView().getJSName();
                        isTwoPart = "twopart".equals(jsName);
                    }

                    // Immediately count down for any views that already have a size
                    if (view.getHeight() > 0 || view.getWidth() > 0 || isAnswerRequired || isTwoPart) {
                        countDown();
                        LogUtil.debug(TAG,
                                "Get known metrics for: " + view.getClass()
                                                                .getSimpleName() + ", h: " + view.getHeight() + ", w: " + view.getWidth()
                        );
                        continue;
                    }

                    // For views that didn't have a size, listen (once) for a preDraw. Note
                    // that this doesn't leak because the ViewTreeObserver gets detached when
                    // the view is no longer part of the view hierarchy.

                    LogUtil.debug(TAG, "Create listener for: " + view.getClass().getSimpleName());
                    view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            LogUtil.debug(TAG, "Get metrics from listener for: " + view.getClass().getSimpleName() + ", h: " + view.getHeight() + ", w: " + view.getWidth());
                            view.getViewTreeObserver().removeOnPreDrawListener(this);
                            countDown();
                            return true;
                        }
                    });
                }
            }
        };

        void start() {
            waitCount = views.length;
            handler.post(waitingRunnable);
        }

        void cancel() {
            handler.removeCallbacks(waitingRunnable);
            successRunnable = null;
        }
    }
}
