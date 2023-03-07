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
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.listeners.AdIdFetchListener;

import java.lang.ref.WeakReference;

public class AdIdManager {
    private static final String TAG = AdIdManager.class.getSimpleName();

    /**
     * Timeout for getting adId & lmt values from google play services in an asynctask
     * Default - 3000
     */
    private static final long AD_ID_TIMEOUT_MS = 3000;

    private static volatile String sAdId = null;
    private static boolean sLimitAdTrackingEnabled;

    private AdIdManager() {

    }

    // Wrap method execution in try / catch to avoid crashes in runtime if publisher didn't include identifier dependencies
    public static void initAdId(final Context context, final AdIdFetchListener listener) {
        try {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
            if (resultCode == ConnectionResult.SUCCESS) {
                final FetchAdIdInfoTask getAdIdInfoTask = new FetchAdIdInfoTask(context, listener);
                getAdIdInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                //wait for a max of 3 secs and cancel the task if it's still running.
                //continue with adIdFetchFailure() where we will just log this as warning
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (getAdIdInfoTask.getStatus() != AsyncTask.Status.FINISHED) {
                        LogUtil.debug(TAG, "Canceling advertising id fetching");
                        getAdIdInfoTask.cancel(true);
                        listener.adIdFetchFailure();
                    }
                }, AD_ID_TIMEOUT_MS);
            }
            else {
                listener.adIdFetchCompletion();
            }
        }
        catch (Throwable throwable) {
            LogUtil.error(TAG, "Failed to initAdId: " + Log.getStackTraceString(throwable) + "\nDid you add necessary dependencies?");
        }
    }

    /**
     * @return Advertiser id, from gms-getAdvertisingIdInfo
     */
    public static String getAdId() {
        return sAdId;
    }

    public static boolean isLimitAdTrackingEnabled() {
        return sLimitAdTrackingEnabled;
    }

    @VisibleForTesting
    public static void setLimitAdTrackingEnabled(boolean limitAdTrackingEnabled) {
        sLimitAdTrackingEnabled = limitAdTrackingEnabled;
    }

    public static void setAdId(String adId) {
        sAdId = adId;
    }

    private static class FetchAdIdInfoTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<Context> contextWeakReference;
        private final AdIdFetchListener adIdFetchListener;

        public FetchAdIdInfoTask(
                Context context,
                AdIdFetchListener listener
        ) {
            contextWeakReference = new WeakReference<>(context);

            // All listeners provided are created as local method variables; If these listeners
            // are ever moved to a class member variable, this needs to be changed to a WeakReference
            adIdFetchListener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = contextWeakReference.get();

            if (isCancelled()) {
                return null;
            }

            if (context == null) {
                return null;
            }

            try {
                AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                sAdId = adInfo.getId();
                sLimitAdTrackingEnabled = adInfo.isLimitAdTrackingEnabled();
            }
            catch (Throwable e) {
                LogUtil.error(TAG, "Failed to get advertising id and LMT: " + Log.getStackTraceString(e));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (adIdFetchListener != null) {
                adIdFetchListener.adIdFetchCompletion();
            }
        }
    }
}
