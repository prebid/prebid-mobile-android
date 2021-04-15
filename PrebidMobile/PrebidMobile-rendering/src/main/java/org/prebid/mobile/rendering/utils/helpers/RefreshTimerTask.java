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

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.rendering.utils.logger.OXLog;

public class RefreshTimerTask {
    private static final String TAG = RefreshTimerTask.class.getSimpleName();
    private Handler mRefreshHandler;

    //for unit testing only
    private boolean mRefreshExecuted;

    private RefreshTriggered mRefreshTriggerListener;

    private final Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRefreshTriggerListener == null) {
                OXLog.error(TAG, "Failed to notify mRefreshTriggerListener. mRefreshTriggerListener instance is null");
                return;
            }

            mRefreshTriggerListener.handleRefresh();
            mRefreshExecuted = true;
        }
    };

    public RefreshTimerTask(RefreshTriggered refreshTriggered) {
        mRefreshHandler = new Handler(Looper.getMainLooper());
        mRefreshTriggerListener = refreshTriggered;
    }

    /**
     * Cancels previous timer (if any) and creates a new one with the given interval in ms
     *
     * @param interval value in milliseconds
     */
    public void scheduleRefreshTask(int interval) {
        cancelRefreshTimer();

        if (interval > 0) {
            queueUIThreadTask(interval);
        }
    }

    public void cancelRefreshTimer() {
        if (mRefreshHandler != null) {
            mRefreshHandler.removeCallbacksAndMessages(null);
        }
    }

    public void destroy() {
        cancelRefreshTimer();
        mRefreshHandler = null;
        mRefreshExecuted = false;
    }

    /**
     * Queue new task that should be performed in UI thread.
     */
    private void queueUIThreadTask(long interval) {
        if (mRefreshHandler != null) {
            mRefreshHandler.postDelayed(mRefreshRunnable, interval);
        }
    }

    @VisibleForTesting
    boolean isRefreshExecuted() {
        return mRefreshExecuted;
    }
}