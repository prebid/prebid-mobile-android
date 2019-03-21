/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class AdvertisingIDUtil {
    private static String aaid = null;
    private static boolean limitAdTracking = false;

    static synchronized String getAAID() {
        return aaid;
    }

    static synchronized boolean isLimitAdTracking() {
        return limitAdTracking;
    }

    private static synchronized void setAAID(String aaid) {
        AdvertisingIDUtil.aaid = aaid;
    }

    private static synchronized void setLimitAdTracking(boolean limitAdTracking) {
        AdvertisingIDUtil.limitAdTracking = limitAdTracking;
    }

    private enum STATE {
        NOT_FETCHED,
        FETCHING,
        FETCHED_BUT_LIMIT_TARGETING,
        FETCHED
    }

    private static STATE state = STATE.NOT_FETCHED;

    /**
     * Starts an AsyncTask to retrieve and set the AAID.
     * Does nothing if PrebidServerSettings.aaid is already set for the SDK.
     *
     * @param context context to retrieve the AAID on.
     */
    @SuppressLint("ObsoleteSdkInt")
    static void retrieveAndSetAAID(Context context) {
        if (STATE.FETCHED.equals(state) || STATE.FETCHING.equals(state) || context == null) {
            return;
        }

        AAIDTask getAAIDTask = new AAIDTask(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getAAIDTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            getAAIDTask.execute();
        }
    }

    /**
     * Retrieves AAID from GooglePlayServices via reflection
     * Sets the SDK's aaid value to the result if successful,
     * or null if failed.
     */
    private static class AAIDTask extends AsyncTask<Void, Void, Void> {
        private static final String cAdvertisingIdClientName
                = "com.google.android.gms.ads.identifier.AdvertisingIdClient";
        private static final String cAdvertisingIdClientInfoName
                = "com.google.android.gms.ads.identifier.AdvertisingIdClient$Info";

        private WeakReference<Context> context;

        private AAIDTask(Context context) {
            this.context = new WeakReference<Context>(context);
        }

        @Override
        protected Void doInBackground(Void... params) {
            state = STATE.FETCHING;
            // attempt to retrieve AAID from GooglePlayServices via reflection
            // Setting aaid in the backend thread
            // Can potentially avoid long processing time on main thread
            try {
                Context callcontext = context.get();
                if (callcontext != null) {
                    // NPE catches null objects
                    Class<?> cInfo = Class.forName(cAdvertisingIdClientInfoName);
                    Class<?> cClient = Class.forName(cAdvertisingIdClientName);

                    Method mGetAdvertisingIdInfo = cClient.getMethod("getAdvertisingIdInfo", Context.class);
                    Method mGetId = cInfo.getMethod("getId");
                    Method mIsLimitAdTrackingEnabled = cInfo.getMethod("isLimitAdTrackingEnabled");

                    Object adInfoObject = cInfo.cast(mGetAdvertisingIdInfo.invoke(null, callcontext));

                    aaid = (String) mGetId.invoke(adInfoObject);
                    limitAdTracking = (Boolean) mIsLimitAdTrackingEnabled.invoke(adInfoObject);
                }
            } catch (ClassNotFoundException ignored) {
            } catch (InvocationTargetException ignored) {
            } catch (NoSuchMethodException ignored) {
            } catch (IllegalAccessException ignored) {
            } catch (ClassCastException ignored) {
            } catch (NullPointerException ignored) {
            } catch (Exception ignored) {
                // catches GooglePlayServicesRepairableException, GooglePlayServicesNotAvailableException
            }

            if (limitAdTracking || TextUtils.isEmpty(aaid)) {
                state = STATE.FETCHED_BUT_LIMIT_TARGETING;
            } else {
                state = STATE.FETCHED;
            }
            return null;
        }
    }
}
