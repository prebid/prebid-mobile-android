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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.sdk.PrebidContextHolder;
import org.prebid.mobile.rendering.sdk.deviceData.managers.UserConsentManager;

import java.lang.ref.WeakReference;

public class AdvertisingIdManager {

    private static final String TAG = AdvertisingIdManager.class.getSimpleName();
    private static final long FETCH_TIMEOUT_MS = 3_000;
    private static final long RESTART_TIMEOUT_MS = 60_000;
    private static long lastStartTime = 0;

    private static volatile AdvertisingId advertisingId = null;

    private AdvertisingIdManager() {}

    /**
     * Run in the current thread.
     */
    public static void initAdvertisingId() {
        if (didFetchingRecently()) return;

        try {
            lastStartTime = System.currentTimeMillis();
            final FetchTask fetchTask = new FetchTask();
            fetchTask.fetch();
        } catch (Throwable throwable) {
            LogUtil.error(TAG, "Failed to init Google advertising id: " + Log.getStackTraceString(throwable) + "\nDid you add necessary dependencies?");
        }
    }

    /**
     * Run in the new background thread.
     */
    public static void updateAdvertisingId() {
        if (didFetchingRecently()) return;

        try {
            lastStartTime = System.currentTimeMillis();
            final FetchTask fetchTask = new FetchTask();
            fetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            runCancelAfterTimeoutTask(fetchTask);
        } catch (Throwable throwable) {
            LogUtil.error(TAG, "Failed to init Google advertising id: " + Log.getStackTraceString(throwable) + "\nDid you add necessary dependencies?");
        }
    }

    @Nullable
    public static String getAdvertisingId(UserConsentManager userConsentManager) {
        if (userConsentManager == null) {
            LogUtil.warning(TAG, "Can't get advertising id. UserConsentManager is null.");
            return null;
        }

        if (userConsentManager.canAccessDeviceData()) {
            return advertisingId != null ? advertisingId.getId() : null;
        }
        return null;
    }

    public static boolean isLimitedAdTrackingEnabled() {
        return advertisingId != null && advertisingId.isLimitAdTrackingEnabled();
    }

    private static boolean didFetchingRecently() {
        long timeSinceLastLaunch = System.currentTimeMillis() - lastStartTime;
        if (timeSinceLastLaunch < RESTART_TIMEOUT_MS) {
            LogUtil.debug(TAG, "Skipping advertising id fetching.");
            return true;
        }
        return false;
    }

    private static void runCancelAfterTimeoutTask(FetchTask fetchTask) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (fetchTask.getStatus() != AsyncTask.Status.FINISHED) {
                LogUtil.debug(TAG, "Canceling advertising id fetching due to timeout.");
                fetchTask.cancel(true);
                advertisingId = null;
            }
        }, FETCH_TIMEOUT_MS);
    }

    public static class FetchTask extends AsyncTask<Void, Void, AdvertisingId> {

        private final WeakReference<Context> contextWeakReference;

        public FetchTask() {
            contextWeakReference = new WeakReference<>(PrebidContextHolder.getContext());
        }

        @Override
        protected AdvertisingId doInBackground(Void... voids) {
            Context context = contextWeakReference.get();
            if (isCancelled() || context == null) {
                return null;
            }

            try {
                long start = System.currentTimeMillis();
                GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
                int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
                if (resultCode == ConnectionResult.SUCCESS) {
                    AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                    LogUtil.debug(TAG, "Advertising id fetching took " + (System.currentTimeMillis() - start) + " ms");
                    return new AdvertisingId(adInfo.getId(), adInfo.isLimitAdTrackingEnabled());
                }
            } catch (Throwable e) {
                LogUtil.error(TAG, "Failed to get advertising id: " + Log.getStackTraceString(e));
            }
            return null;
        }

        @Override
        protected void onPostExecute(@Nullable AdvertisingId id) {
            advertisingId = id;
        }

        /**
         * Additional method executing this task without seperated thread.
         */
        public void fetch() {
            AdvertisingId id = doInBackground();
            onPostExecute(id);
        }

    }

    public static class AdvertisingId {

        private String id;
        private boolean limitAdTrackingEnabled;

        public AdvertisingId(String id, boolean limitAdTrackingEnabled) {
            this.id = id;
            this.limitAdTrackingEnabled = limitAdTrackingEnabled;
        }

        public String getId() {
            return id;
        }

        public boolean isLimitAdTrackingEnabled() {
            return limitAdTrackingEnabled;
        }
    }

}
